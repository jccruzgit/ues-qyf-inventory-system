package sv.edu.ues.qyf.inventory.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.InventoryAlertMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementLineRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.InventoryAlertService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class InventoryAlertServiceImpl implements InventoryAlertService {

    private static final int EXPIRING_BATCH_WINDOW_DAYS = 30;

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
    @Transactional
    public List<InventoryAlertResponseDto> getPendingByLaboratory(Long laboratoryId) {
        return getAlerts(laboratoryId, null, true);
    }

    @Override
    @Transactional
    public List<InventoryAlertResponseDto> getAlerts(Long laboratoryId, InventoryAlertType alertType, Boolean pendingOnly) {
        List<Long> laboratoryIds = resolveAccessibleLaboratoryIds(laboratoryId);

        for (Long accessibleLaboratoryId : laboratoryIds) {
            synchronizeAlerts(
                    accessibleLaboratoryId,
                    inventoryMovementLineRepository.findDistinctProductIdsByLaboratoryId(accessibleLaboratoryId),
                    List.of());
        }

        List<InventoryAlert> alerts = loadAlerts(laboratoryIds, Boolean.TRUE.equals(pendingOnly));

        if (alertType != null) {
            alerts = alerts.stream()
                    .filter(alert -> alert.getAlertType() == alertType)
                    .toList();
        }

        return alerts.stream()
                .map(this::buildAlertResponse)
                .toList();
    }

    @Override
    @Transactional
    public void synchronizeAlerts(Long laboratoryId, List<Long> productIds, List<Long> productBatchIds) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);

        for (Long productId : productIds.stream().filter(Objects::nonNull).distinct().toList()) {
            synchronizeProductAlert(laboratoryId, productId);
        }

        List<Long> batchIdsToEvaluate = new ArrayList<>(productBatchIds);
        batchIdsToEvaluate.addAll(productBatchRepository.findByLaboratoryIdAndExpirationDateIsNotNullAndActiveTrueOrderByExpirationDateAsc(
                        laboratoryId)
                .stream()
                .map(ProductBatch::getId)
                .toList());

        for (Long batchId : batchIdsToEvaluate.stream().filter(Objects::nonNull).distinct().toList()) {
            synchronizeBatchAlert(laboratoryId, batchId);
        }
    }

    private List<Long> resolveAccessibleLaboratoryIds(Long laboratoryId) {
        if (laboratoryId != null) {
            laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
            return List.of(laboratoryId);
        }

        if (laboratoryAccessService.hasAccessToAllLaboratories()) {
            return laboratoryRepository.findByActiveTrue().stream()
                    .map(laboratory -> laboratory.getId())
                    .toList();
        }

        return laboratoryAccessService.getAccessibleLaboratoryIds();
    }

    private List<InventoryAlert> loadAlerts(List<Long> laboratoryIds, boolean pendingOnly) {
        if (laboratoryIds.isEmpty()) {
            return List.of();
        }

        if (laboratoryIds.size() == 1) {
            Long laboratoryId = laboratoryIds.get(0);
            return pendingOnly
                    ? inventoryAlertRepository.findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(laboratoryId)
                    : inventoryAlertRepository.findByLaboratoryIdOrderByTriggeredAtDescIdDesc(laboratoryId);
        }

        return pendingOnly
                ? inventoryAlertRepository.findByLaboratoryIdInAndAcknowledgedAtIsNullOrderByTriggeredAtDescIdDesc(laboratoryIds)
                : inventoryAlertRepository.findByLaboratoryIdInOrderByTriggeredAtDescIdDesc(laboratoryIds);
    }

    private void synchronizeProductAlert(Long laboratoryId, Long productId) {
        Product product = productRepository.findByIdAndActiveTrue(productId).orElse(null);
        List<InventoryAlertType> productAlertTypes = List.of(InventoryAlertType.LOW_STOCK, InventoryAlertType.OUT_OF_STOCK);

        if (product == null) {
            inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeInAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                    laboratoryId, productAlertTypes, productId);
            return;
        }

        BigDecimal currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal minimumStock = product.getMinimumStock() != null ? product.getMinimumStock() : BigDecimal.ZERO;

        inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeInAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                laboratoryId, productAlertTypes, productId);

        if (currentStock.compareTo(BigDecimal.ZERO) <= 0) {
            inventoryAlertRepository
                    .findFirstByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                            laboratoryId, InventoryAlertType.OUT_OF_STOCK, productId)
                    .orElseGet(() -> inventoryAlertRepository.save(InventoryAlert.builder()
                            .laboratory(getLaboratory(laboratoryId))
                            .alertType(InventoryAlertType.OUT_OF_STOCK)
                            .product(product)
                            .message("Product " + product.getCode() + " is out of stock")
                            .build()));
            return;
        }

        if (currentStock.compareTo(minimumStock) <= 0) {
            inventoryAlertRepository
                    .findFirstByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
                            laboratoryId, InventoryAlertType.LOW_STOCK, productId)
                    .orElseGet(() -> inventoryAlertRepository.save(InventoryAlert.builder()
                            .laboratory(getLaboratory(laboratoryId))
                            .alertType(InventoryAlertType.LOW_STOCK)
                            .product(product)
                            .message("Product " + product.getCode() + " is at or below minimum stock")
                            .build()));
        }
    }

    private void synchronizeBatchAlert(Long laboratoryId, Long productBatchId) {
        ProductBatch batch = productBatchRepository.findByIdAndActiveTrue(productBatchId).orElse(null);
        List<InventoryAlertType> batchAlertTypes = List.of(InventoryAlertType.EXPIRING_BATCH, InventoryAlertType.EXPIRED_BATCH);

        if (batch == null || batch.getExpirationDate() == null || !batch.getLaboratory().getId().equals(laboratoryId)) {
            inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeInAndProductBatchIdAndAcknowledgedAtIsNull(
                    laboratoryId, batchAlertTypes, productBatchId);
            return;
        }

        BigDecimal quantityAvailable = inventoryMovementLineRepository.calculateCurrentStockByBatchId(batch.getId(), MovementType.ENTRY);

        inventoryAlertRepository.deleteByLaboratoryIdAndAlertTypeInAndProductBatchIdAndAcknowledgedAtIsNull(
                laboratoryId, batchAlertTypes, productBatchId);

        if (quantityAvailable == null || quantityAvailable.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        InventoryAlertType alertType = null;
        String message = null;

        if (!batch.getExpirationDate().isAfter(today)) {
            alertType = InventoryAlertType.EXPIRED_BATCH;
            message = "Batch " + batch.getBatchCode() + " is expired";
        } else if (!batch.getExpirationDate().isAfter(today.plusDays(EXPIRING_BATCH_WINDOW_DAYS))) {
            alertType = InventoryAlertType.EXPIRING_BATCH;
            message = "Batch " + batch.getBatchCode() + " is close to expiration";
        }

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

    private InventoryAlertResponseDto buildAlertResponse(InventoryAlert alert) {
        InventoryAlertResponseDto baseResponse = inventoryAlertMapper.toResponseDto(alert);
        BigDecimal quantityAvailable = resolveQuantityAvailable(alert);

        return baseResponse.toBuilder()
                .quantityAvailable(quantityAvailable)
                .severity(resolveSeverity(alert.getAlertType(), baseResponse.getExpirationDate()))
                .status(alert.getAcknowledgedAt() == null ? "PENDIENTE" : "ATENDIDA")
                .build();
    }

    private BigDecimal resolveQuantityAvailable(InventoryAlert alert) {
        if (alert.getProductBatch() != null && alert.getProductBatch().getId() != null) {
            return inventoryMovementLineRepository.calculateCurrentStockByBatchId(
                    alert.getProductBatch().getId(), MovementType.ENTRY);
        }

        if (alert.getProduct() != null) {
            return alert.getProduct().getCurrentStock();
        }

        return null;
    }

    private String resolveSeverity(InventoryAlertType alertType, LocalDate expirationDate) {
        if (alertType == InventoryAlertType.OUT_OF_STOCK || alertType == InventoryAlertType.EXPIRED_BATCH) {
            return "CRITICA";
        }

        if (alertType == InventoryAlertType.LOW_STOCK) {
            return "ALTA";
        }

        if (expirationDate != null && !expirationDate.isAfter(LocalDate.now().plusDays(7))) {
            return "ALTA";
        }

        return "MEDIA";
    }

    private sv.edu.ues.qyf.inventory.entity.Laboratory getLaboratory(Long laboratoryId) {
        return laboratoryRepository.findByIdAndActiveTrue(laboratoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory not found with id: " + laboratoryId));
    }
}
