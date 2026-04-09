package sv.edu.ues.qyf.inventory.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.mapper.InventoryAlertMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementLineRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.service.InventoryAlertService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class InventoryAlertServiceImpl implements InventoryAlertService {

    private final InventoryAlertRepository inventoryAlertRepository;
    private final InventoryAlertMapper inventoryAlertMapper;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final InventoryMovementLineRepository inventoryMovementLineRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final LaboratoryAccessService laboratoryAccessService;

    public InventoryAlertServiceImpl(
            InventoryAlertRepository inventoryAlertRepository,
            InventoryAlertMapper inventoryAlertMapper,
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            InventoryMovementLineRepository inventoryMovementLineRepository,
            LaboratoryRepository laboratoryRepository,
            LaboratoryAccessService laboratoryAccessService) {
        this.inventoryAlertRepository = inventoryAlertRepository;
        this.inventoryAlertMapper = inventoryAlertMapper;
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.inventoryMovementLineRepository = inventoryMovementLineRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.laboratoryAccessService = laboratoryAccessService;
    }

    @Override
    public List<InventoryAlertResponseDto> getPendingByLaboratory(Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        synchronizeAlerts(laboratoryId, inventoryMovementLineRepository.findDistinctProductIdsByLaboratoryId(laboratoryId), List.of());
        return inventoryAlertRepository.findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(laboratoryId).stream()
                .map(inventoryAlertMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<InventoryAlertResponseDto> getAlerts(Long laboratoryId, InventoryAlertType alertType, Boolean pendingOnly) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        synchronizeAlerts(laboratoryId, inventoryMovementLineRepository.findDistinctProductIdsByLaboratoryId(laboratoryId), List.of());

        List<InventoryAlert> alerts;
        if (Boolean.TRUE.equals(pendingOnly)) {
            alerts = alertType == null
                    ? inventoryAlertRepository.findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(laboratoryId)
                    : inventoryAlertRepository.findByLaboratoryIdAndAlertTypeAndAcknowledgedAtIsNullOrderByIdDesc(
                            laboratoryId, alertType);
        } else {
            alerts = inventoryAlertRepository.findByLaboratoryIdOrderByTriggeredAtDescIdDesc(laboratoryId);
            if (alertType != null) {
                alerts = alerts.stream()
                        .filter(alert -> alert.getAlertType() == alertType)
                        .toList();
            }
        }

        return alerts.stream()
                .map(inventoryAlertMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void synchronizeAlerts(Long laboratoryId, List<Long> productIds, List<Long> productBatchIds) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);

        for (Long productId : productIds.stream().distinct().toList()) {
            synchronizeLowStockAlert(laboratoryId, productId);
        }

        List<Long> batchIdsToEvaluate = new ArrayList<>(productBatchIds);
        batchIdsToEvaluate.addAll(productBatchRepository.findByLaboratoryIdAndExpirationDateIsNotNullAndActiveTrueOrderByExpirationDateAsc(
                        laboratoryId)
                .stream()
                .map(ProductBatch::getId)
                .toList());

        for (Long batchId : batchIdsToEvaluate.stream().distinct().toList()) {
            synchronizeBatchAlert(laboratoryId, batchId);
        }
    }

    private void synchronizeLowStockAlert(Long laboratoryId, Long productId) {
        Product product = productRepository.findByIdAndActiveTrue(productId).orElse(null);
        if (product == null) {
            inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                    laboratoryId, InventoryAlertType.LOW_STOCK, productId);
            return;
        }

        if (product.getCurrentStock().compareTo(product.getMinimumStock()) < 0) {
            inventoryAlertRepository
                    .findFirstByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                            laboratoryId, InventoryAlertType.LOW_STOCK, productId)
                    .orElseGet(() -> inventoryAlertRepository.save(InventoryAlert.builder()
                            .laboratory(getLaboratory(laboratoryId))
                            .alertType(InventoryAlertType.LOW_STOCK)
                            .product(product)
                            .message("Product " + product.getCode() + " is below minimum stock")
                            .build()));
            return;
        }

        inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                laboratoryId, InventoryAlertType.LOW_STOCK, productId);
    }

    private void synchronizeBatchAlert(Long laboratoryId, Long productBatchId) {
        ProductBatch batch = productBatchRepository.findByIdAndActiveTrue(productBatchId).orElse(null);
        if (batch == null || batch.getExpirationDate() == null || !batch.getLaboratory().getId().equals(laboratoryId)) {
            inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeInAndProductBatchIdAndAcknowledgedAtIsNull(
                    laboratoryId,
                    List.of(InventoryAlertType.EXPIRING_BATCH, InventoryAlertType.EXPIRED_BATCH),
                    productBatchId);
            return;
        }

        LocalDate today = LocalDate.now();
        InventoryAlertType alertType = null;
        String message = null;
        if (batch.getExpirationDate().isBefore(today) || batch.getExpirationDate().isEqual(today)) {
            alertType = InventoryAlertType.EXPIRED_BATCH;
            message = "Batch " + batch.getBatchCode() + " is expired";
        } else if (!batch.getExpirationDate().isAfter(today.plusDays(30))) {
            alertType = InventoryAlertType.EXPIRING_BATCH;
            message = "Batch " + batch.getBatchCode() + " is close to expiration";
        }

        inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeInAndProductBatchIdAndAcknowledgedAtIsNull(
                laboratoryId,
                List.of(InventoryAlertType.EXPIRING_BATCH, InventoryAlertType.EXPIRED_BATCH),
                productBatchId);

        if (alertType == null) {
            return;
        }

        inventoryAlertRepository.save(InventoryAlert.builder()
                .laboratory(batch.getLaboratory())
                .alertType(alertType)
                .product(batch.getProduct())
                .productBatch(batch)
                .message(message)
                .build());
    }

    private sv.edu.ues.qyf.inventory.entity.Laboratory getLaboratory(Long laboratoryId) {
        return laboratoryRepository.findByIdAndActiveTrue(laboratoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory not found with id: " + laboratoryId));
    }
}
