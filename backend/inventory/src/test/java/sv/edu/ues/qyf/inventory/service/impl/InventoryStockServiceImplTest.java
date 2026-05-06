package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class InventoryStockServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @InjectMocks
    private InventoryStockServiceImpl inventoryStockService;

    @Test
    void getStock_byProductReturnsPersistedCurrentStock() {
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(new BigDecimal("75"))
                .minimumStock(new BigDecimal("20"))
                .active(Boolean.TRUE)
                .build();

        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));

        List<InventoryStockResponseDto> stock = inventoryStockService.getStock(9L, null, null);

        assertThat(stock).hasSize(1);
        assertThat(stock.get(0).getProductId()).isEqualTo(9L);
        assertThat(stock.get(0).getQuantityAvailable()).isEqualByComparingTo("75");
        assertThat(stock.get(0).getLowStock()).isFalse();
    }

    @Test
    void getStock_byProductFlagsLowStockWhenQuantityEqualsMinimum() {
        Product product = Product.builder()
                .id(10L)
                .code("PRD-10")
                .name("Reactivo minimo")
                .currentStock(new BigDecimal("20"))
                .minimumStock(new BigDecimal("20"))
                .active(Boolean.TRUE)
                .build();

        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));

        List<InventoryStockResponseDto> stock = inventoryStockService.getStock(10L, null, null);

        assertThat(stock).hasSize(1);
        assertThat(stock.get(0).getProductId()).isEqualTo(10L);
        assertThat(stock.get(0).getLowStock()).isTrue();
    }

    @Test
    void getStock_byLaboratoryAggregatesEntriesAndExits() {
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .minimumStock(new BigDecimal("20"))
                .build();
        Laboratory laboratory = Laboratory.builder()
                .id(3L)
                .code("LAB-3")
                .name("Laboratorio 3")
                .build();
        ProductBatch batch = ProductBatch.builder()
                .id(44L)
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-44")
                .expirationDate(LocalDate.now().plusDays(20))
                .build();
        InventoryMovement entry = InventoryMovement.builder()
                .id(100L)
                .movementType(MovementType.ENTRY)
                .laboratory(laboratory)
                .lines(List.of(InventoryMovementLine.builder()
                        .product(product)
                        .productBatch(batch)
                        .quantity(new BigDecimal("100"))
                        .build()))
                .build();
        InventoryMovement exit = InventoryMovement.builder()
                .id(101L)
                .movementType(MovementType.EXIT)
                .laboratory(laboratory)
                .lines(List.of(InventoryMovementLine.builder()
                        .product(product)
                        .productBatch(batch)
                        .quantity(new BigDecimal("25"))
                        .build()))
                .build();

        when(inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(3L))
                .thenReturn(List.of(entry, exit));

        List<InventoryStockResponseDto> stock = inventoryStockService.getStock(9L, 3L, null);

        assertThat(stock).hasSize(1);
        assertThat(stock.get(0).getProductId()).isEqualTo(9L);
        assertThat(stock.get(0).getLaboratoryId()).isEqualTo(3L);
        assertThat(stock.get(0).getProductBatchId()).isEqualTo(44L);
        assertThat(stock.get(0).getQuantityAvailable()).isEqualByComparingTo("75");

        InOrder inOrder = inOrder(laboratoryAccessService, inventoryMovementRepository);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(3L);
        inOrder.verify(inventoryMovementRepository).findByLaboratoryIdOrderByPerformedAtDescIdDesc(3L);
    }
}
