package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import sv.edu.ues.qyf.inventory.dto.DashboardSummaryResponseDto;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private LaboratoryRepository laboratoryRepository;

    @Mock
    private InventoryStockService inventoryStockService;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getSummary_aggregatesAccessibleInventoryMetrics() {
        Laboratory laboratory = Laboratory.builder()
                .id(10L)
                .code("LAB-10")
                .name("Laboratorio Instrumental")
                .active(Boolean.TRUE)
                .build();
        List<InventoryStockResponseDto> stockItems = List.of(
                InventoryStockResponseDto.builder()
                        .productId(1L)
                        .laboratoryId(10L)
                        .quantityAvailable(new BigDecimal("30"))
                        .lowStock(Boolean.FALSE)
                        .build(),
                InventoryStockResponseDto.builder()
                        .productId(2L)
                        .laboratoryId(10L)
                        .quantityAvailable(new BigDecimal("5"))
                        .lowStock(Boolean.TRUE)
                        .build());
        InventoryMovement recentEntry = InventoryMovement.builder()
                .id(100L)
                .movementType(MovementType.ENTRY)
                .laboratory(laboratory)
                .performedBy(User.builder().id(3L).username("demo_admin").build())
                .performedAt(LocalDateTime.now().minusHours(3))
                .lines(List.of(InventoryMovementLine.builder()
                        .id(1000L)
                        .product(Product.builder().id(1L).name("Etanol").build())
                        .quantity(new BigDecimal("12"))
                        .build()))
                .build();
        InventoryMovement recentExit = InventoryMovement.builder()
                .id(101L)
                .movementType(MovementType.EXIT)
                .laboratory(laboratory)
                .performedBy(User.builder().id(4L).username("tech_user").build())
                .performedAt(LocalDateTime.now().minusHours(1))
                .lines(List.of(
                        InventoryMovementLine.builder()
                                .id(1001L)
                                .product(Product.builder().id(2L).name("Acetona").build())
                                .quantity(new BigDecimal("2"))
                                .build(),
                        InventoryMovementLine.builder()
                                .id(1002L)
                                .product(Product.builder().id(3L).name("Metanol").build())
                                .quantity(new BigDecimal("1"))
                                .build()))
                .build();
        ProductBatch expiringBatch = ProductBatch.builder()
                .id(200L)
                .laboratory(laboratory)
                .expirationDate(LocalDate.now().plusDays(10))
                .active(Boolean.TRUE)
                .build();

        when(laboratoryAccessService.hasAccessToAllLaboratories()).thenReturn(true);
        when(laboratoryRepository.findByActiveTrue()).thenReturn(List.of(laboratory));
        when(inventoryStockService.getStock(null, 10L, null)).thenReturn(stockItems);
        when(productRepository.findByActiveTrue(any(Sort.class)))
                .thenReturn(List.of(
                        Product.builder().id(1L).build(),
                        Product.builder().id(2L).build(),
                        Product.builder().id(3L).build()));
        when(productBatchRepository.findByLaboratoryIdAndExpirationDateLessThanEqualAndActiveTrueOrderByExpirationDateAsc(
                        10L, LocalDate.now().plusDays(30)))
                .thenReturn(List.of(expiringBatch));
        when(inventoryMovementRepository.findAllByOrderByPerformedAtDescIdDesc())
                .thenReturn(List.of(recentExit, recentEntry));

        DashboardSummaryResponseDto summary = dashboardService.getSummary();

        assertThat(summary.getTotalActiveProducts()).isEqualTo(3L);
        assertThat(summary.getLowStockProducts()).isEqualTo(1L);
        assertThat(summary.getExpiringBatches()).isEqualTo(1L);
        assertThat(summary.getAccessibleLaboratories()).isEqualTo(1L);
        assertThat(summary.getMovementsLastSevenDays()).isEqualTo(2L);
        assertThat(summary.getMovementSeries()).hasSize(7);
        assertThat(summary.getRecentMovements()).hasSize(2);
        assertThat(summary.getRecentMovements().get(0).getId()).isEqualTo(101L);
        assertThat(summary.getRecentMovements().get(0).getTotalQuantity()).isEqualByComparingTo("3");
        assertThat(summary.getInventoryByLaboratory()).hasSize(1);
        assertThat(summary.getInventoryByLaboratory().get(0).getLaboratoryId()).isEqualTo(10L);
        assertThat(summary.getInventoryByLaboratory().get(0).getVisibleProducts()).isEqualTo(2L);
        assertThat(summary.getInventoryByLaboratory().get(0).getLowStockProducts()).isEqualTo(1L);
        assertThat(summary.getInventoryByLaboratory().get(0).getExpiringBatches()).isEqualTo(1L);
        assertThat(summary.getInventoryByLaboratory().get(0).getQuantityAvailable()).isEqualByComparingTo("35");
    }
}
