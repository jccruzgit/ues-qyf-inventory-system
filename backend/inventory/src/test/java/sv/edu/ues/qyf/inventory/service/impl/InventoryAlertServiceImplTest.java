package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
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
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.mapper.InventoryAlertMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementLineRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class InventoryAlertServiceImplTest {

    @Mock
    private InventoryAlertRepository inventoryAlertRepository;

    @Mock
    private InventoryAlertMapper inventoryAlertMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private InventoryMovementLineRepository inventoryMovementLineRepository;

    @Mock
    private LaboratoryRepository laboratoryRepository;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @InjectMocks
    private InventoryAlertServiceImpl inventoryAlertService;

    @Test
    void getPendingByLaboratory_validatesAccessBeforeLoadingAlerts() {
        InventoryAlert alert = InventoryAlert.builder().id(6L).build();
        InventoryAlertResponseDto response = InventoryAlertResponseDto.builder().id(6L).laboratoryId(12L).build();

        when(inventoryMovementLineRepository.findDistinctProductIdsByLaboratoryId(12L)).thenReturn(List.of());
        when(inventoryAlertRepository.findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(12L))
                .thenReturn(List.of(alert));
        when(inventoryAlertMapper.toResponseDto(alert)).thenReturn(response);

        List<InventoryAlertResponseDto> result = inventoryAlertService.getPendingByLaboratory(12L);

        assertThat(result).containsExactly(response);
        InOrder inOrder = inOrder(
                laboratoryAccessService,
                inventoryMovementLineRepository,
                inventoryAlertRepository,
                inventoryAlertMapper);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(12L);
        inOrder.verify(inventoryMovementLineRepository).findDistinctProductIdsByLaboratoryId(12L);
        inOrder.verify(inventoryAlertRepository).findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(12L);
        inOrder.verify(inventoryAlertMapper).toResponseDto(alert);
    }

    @Test
    void synchronizeAlerts_createsLowStockAlertWhenProductIsBelowMinimum() {
        Laboratory laboratory = Laboratory.builder().id(12L).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .currentStock(new BigDecimal("10"))
                .minimumStock(new BigDecimal("20"))
                .active(Boolean.TRUE)
                .build();

        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(laboratoryRepository.findByIdAndActiveTrue(12L)).thenReturn(Optional.of(laboratory));
        when(inventoryAlertRepository
                        .findFirstByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                                12L, InventoryAlertType.LOW_STOCK, 9L))
                .thenReturn(Optional.empty());
        when(productBatchRepository.findByLaboratoryIdAndExpirationDateIsNotNullAndActiveTrueOrderByExpirationDateAsc(12L))
                .thenReturn(List.of());

        inventoryAlertService.synchronizeAlerts(12L, List.of(9L), List.of());

        verify(inventoryAlertRepository)
                .save(argThat(alert -> alert.getLaboratory().getId().equals(12L)
                        && alert.getProduct().getId().equals(9L)
                        && alert.getAlertType() == InventoryAlertType.LOW_STOCK
                        && alert.getMessage().contains("PRD-9")));
    }

    @Test
    void synchronizeAlerts_createsExpiringBatchAlertWhenBatchExpiresWithinThirtyDays() {
        Laboratory laboratory = Laboratory.builder().id(12L).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .currentStock(new BigDecimal("50"))
                .minimumStock(new BigDecimal("10"))
                .active(Boolean.TRUE)
                .build();
        ProductBatch batch = ProductBatch.builder()
                .id(22L)
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-22")
                .expirationDate(LocalDate.now().plusDays(10))
                .active(Boolean.TRUE)
                .build();

        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(productBatchRepository.findByIdAndActiveTrue(22L)).thenReturn(Optional.of(batch));
        when(productBatchRepository.findByLaboratoryIdAndExpirationDateIsNotNullAndActiveTrueOrderByExpirationDateAsc(12L))
                .thenReturn(List.of());

        inventoryAlertService.synchronizeAlerts(12L, List.of(9L), List.of(22L));

        verify(inventoryAlertRepository)
                .deleteByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                        12L, InventoryAlertType.LOW_STOCK, 9L);
        verify(inventoryAlertRepository)
                .save(argThat(alert -> alert.getLaboratory().getId().equals(12L)
                        && alert.getProductBatch().getId().equals(22L)
                        && alert.getAlertType() == InventoryAlertType.EXPIRING_BATCH
                        && alert.getMessage().contains("LOT-22")));
    }
}
