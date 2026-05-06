package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.ProductBatchOverviewResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.ProductBatchMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;
import sv.edu.ues.qyf.inventory.service.ProductBatchService;

@Service
@Transactional
public class ProductBatchServiceImpl implements ProductBatchService {

    private static final String TABLE_NAME = "product_batches";

    private final ProductBatchRepository productBatchRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductBatchMapper productBatchMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final LaboratoryAccessService laboratoryAccessService;
    private final ObjectMapper objectMapper;

    public ProductBatchServiceImpl(
            ProductBatchRepository productBatchRepository,
            InventoryMovementRepository inventoryMovementRepository,
            ProductBatchMapper productBatchMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            LaboratoryAccessService laboratoryAccessService,
            ObjectMapper objectMapper) {
        this.productBatchRepository = productBatchRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.productBatchMapper = productBatchMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.laboratoryAccessService = laboratoryAccessService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchOverviewResponseDto> getOverview(Long productId, Long laboratoryId) {
        List<ProductBatch> batches = getAccessibleBatches(laboratoryId).stream()
                .filter(batch -> productId == null || batch.getProduct().getId().equals(productId))
                .toList();

        if (batches.isEmpty()) {
            return List.of();
        }

        Map<Long, ProductBatchOverviewAccumulator> overviewByBatchId = new LinkedHashMap<>();
        for (ProductBatch batch : batches) {
            overviewByBatchId.put(batch.getId(), ProductBatchOverviewAccumulator.fromBatch(batch));
        }

        for (InventoryMovement movement : getAccessibleMovements(laboratoryId)) {
            for (InventoryMovementLine line : movement.getLines()) {
                if (line.getProductBatch() == null) {
                    continue;
                }

                ProductBatchOverviewAccumulator overview = overviewByBatchId.get(line.getProductBatch().getId());
                if (overview == null) {
                    continue;
                }

                overview.applyMovement(movement.getMovementType(), line.getQuantity());
                overview.captureLatestEntryPrice(movement.getMovementType(), line);
            }
        }

        return overviewByBatchId.values().stream()
                .sorted(Comparator
                        .comparing(ProductBatchOverviewAccumulator::laboratoryName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(ProductBatchOverviewAccumulator::productName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(ProductBatchOverviewAccumulator::expirationDate, Comparator.nullsLast(java.time.LocalDate::compareTo))
                        .thenComparing(ProductBatchOverviewAccumulator::batchCode, Comparator.nullsLast(String::compareTo)))
                .map(ProductBatchOverviewAccumulator::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchResponseDto> getActiveByProductAndLaboratory(Long productId, Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        return productBatchRepository
                .findByProductIdAndLaboratoryIdAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(productId, laboratoryId)
                .stream()
                .map(productBatchMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchResponseDto getById(Long id) {
        ProductBatch productBatch = getActiveProductBatch(id);
        laboratoryAccessService.validateAccessToLaboratory(resolveLaboratoryId(productBatch));
        return productBatchMapper.toResponseDto(productBatch);
    }

    @Override
    public ProductBatchResponseDto deactivate(Long id) {
        ProductBatch productBatch = getActiveProductBatch(id);
        laboratoryAccessService.validateAccessToLaboratory(resolveLaboratoryId(productBatch));
        User currentUser = currentUserService.getAuthenticatedUser();
        String oldValues = serializeState(productBatch);

        productBatch.setActive(Boolean.FALSE);
        productBatch.setDeletedAt(LocalDateTime.now());
        productBatch.setDeletedBy(currentUser);

        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);
        auditLogService.logSoftDelete(
                TABLE_NAME,
                savedProductBatch.getId(),
                savedProductBatch.getLaboratory() != null ? savedProductBatch.getLaboratory().getId() : null,
                oldValues,
                serializeState(savedProductBatch),
                "Product batch soft deleted");

        return productBatchMapper.toResponseDto(savedProductBatch);
    }

    @Override
    public ProductBatchResponseDto restore(Long id) {
        ProductBatch productBatch = getProductBatch(id);
        laboratoryAccessService.validateAccessToLaboratory(resolveLaboratoryId(productBatch));
        String oldValues = serializeState(productBatch);

        productBatch.setActive(Boolean.TRUE);
        productBatch.setDeletedAt(null);
        productBatch.setDeletedBy(null);

        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);
        auditLogService.logRestore(
                TABLE_NAME,
                savedProductBatch.getId(),
                savedProductBatch.getLaboratory() != null ? savedProductBatch.getLaboratory().getId() : null,
                oldValues,
                serializeState(savedProductBatch),
                "Product batch restored");

        return productBatchMapper.toResponseDto(savedProductBatch);
    }

    private ProductBatch getActiveProductBatch(Long id) {
        return productBatchRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + id));
    }

    private ProductBatch getProductBatch(Long id) {
        return productBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + id));
    }

    private String serializeState(ProductBatch productBatch) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", productBatch.getId());
        state.put("productId", productBatch.getProduct() != null ? productBatch.getProduct().getId() : null);
        state.put("laboratoryId", productBatch.getLaboratory() != null ? productBatch.getLaboratory().getId() : null);
        state.put("batchCode", productBatch.getBatchCode());
        state.put("status", productBatch.getStatus());
        state.put("expirationDate", productBatch.getExpirationDate());
        state.put("createdAt", productBatch.getCreatedAt());
        state.put("updatedAt", productBatch.getUpdatedAt());
        state.put("active", productBatch.getActive());
        state.put("deletedAt", productBatch.getDeletedAt());
        state.put("deletedById", productBatch.getDeletedBy() != null ? productBatch.getDeletedBy().getId() : null);
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize product batch audit state", exception);
        }
    }

    private Long resolveLaboratoryId(ProductBatch productBatch) {
        return productBatch.getLaboratory() != null ? productBatch.getLaboratory().getId() : null;
    }

    private List<ProductBatch> getAccessibleBatches(Long laboratoryId) {
        if (laboratoryId != null) {
            laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
            return productBatchRepository.findByLaboratoryIdAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(laboratoryId);
        }

        if (laboratoryAccessService.hasAccessToAllLaboratories()) {
            return productBatchRepository.findByActiveTrueOrderByExpirationDateAscBatchCodeAsc();
        }

        List<Long> laboratoryIds = laboratoryAccessService.getAccessibleLaboratoryIds();
        if (laboratoryIds.isEmpty()) {
            return List.of();
        }

        return productBatchRepository.findByLaboratoryIdInAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(laboratoryIds);
    }

    private List<InventoryMovement> getAccessibleMovements(Long laboratoryId) {
        if (laboratoryId != null) {
            return inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(laboratoryId);
        }

        if (laboratoryAccessService.hasAccessToAllLaboratories()) {
            return inventoryMovementRepository.findAllByOrderByPerformedAtDescIdDesc();
        }

        List<Long> laboratoryIds = laboratoryAccessService.getAccessibleLaboratoryIds();
        if (laboratoryIds.isEmpty()) {
            return List.of();
        }

        return inventoryMovementRepository.findByLaboratoryIdInOrderByPerformedAtDescIdDesc(laboratoryIds);
    }

    private static final class ProductBatchOverviewAccumulator {

        private final Long id;
        private final Long productId;
        private final String productCode;
        private final String productName;
        private final Long laboratoryId;
        private final String laboratoryCode;
        private final String laboratoryName;
        private final String batchCode;
        private final String locationName;
        private final String unitName;
        private final String unitSymbol;
        private final java.time.LocalDate expirationDate;
        private final sv.edu.ues.qyf.inventory.entity.BatchStatus status;
        private final String notes;
        private BigDecimal quantityAvailable;
        private BigDecimal unitPrice;
        private String priceUnitName;
        private String priceUnitSymbol;

        private ProductBatchOverviewAccumulator(
                Long id,
                Long productId,
                String productCode,
                String productName,
                Long laboratoryId,
                String laboratoryCode,
                String laboratoryName,
                String batchCode,
                String locationName,
                String unitName,
                String unitSymbol,
                java.time.LocalDate expirationDate,
                sv.edu.ues.qyf.inventory.entity.BatchStatus status,
                String notes,
                BigDecimal quantityAvailable,
                BigDecimal unitPrice,
                String priceUnitName,
                String priceUnitSymbol) {
            this.id = id;
            this.productId = productId;
            this.productCode = productCode;
            this.productName = productName;
            this.laboratoryId = laboratoryId;
            this.laboratoryCode = laboratoryCode;
            this.laboratoryName = laboratoryName;
            this.batchCode = batchCode;
            this.locationName = locationName;
            this.unitName = unitName;
            this.unitSymbol = unitSymbol;
            this.expirationDate = expirationDate;
            this.status = status;
            this.notes = notes;
            this.quantityAvailable = quantityAvailable;
            this.unitPrice = unitPrice;
            this.priceUnitName = priceUnitName;
            this.priceUnitSymbol = priceUnitSymbol;
        }

        private static ProductBatchOverviewAccumulator fromBatch(ProductBatch batch) {
            return new ProductBatchOverviewAccumulator(
                    batch.getId(),
                    batch.getProduct().getId(),
                    batch.getProduct().getCode(),
                    batch.getProduct().getName(),
                    batch.getLaboratory().getId(),
                    batch.getLaboratory().getCode(),
                    batch.getLaboratory().getName(),
                    batch.getBatchCode(),
                    batch.getProduct().getLocation() != null ? batch.getProduct().getLocation().getName() : null,
                    batch.getProduct().getBaseUnit() != null ? batch.getProduct().getBaseUnit().getName() : null,
                    batch.getProduct().getBaseUnit() != null ? batch.getProduct().getBaseUnit().getSymbol() : null,
                    batch.getExpirationDate(),
                    batch.getStatus(),
                    batch.getNotes(),
                    BigDecimal.ZERO,
                    null,
                    null,
                    null);
        }

        private void applyMovement(MovementType movementType, BigDecimal quantity) {
            quantityAvailable = movementType == MovementType.ENTRY
                    ? quantityAvailable.add(quantity)
                    : quantityAvailable.subtract(quantity);
        }

        private void captureLatestEntryPrice(MovementType movementType, InventoryMovementLine line) {
            if (movementType != MovementType.ENTRY || unitPrice != null || line.getUnitPrice() == null) {
                return;
            }

            unitPrice = line.getUnitPrice();
            priceUnitName = line.getPriceUnit() != null ? line.getPriceUnit().getName() : unitName;
            priceUnitSymbol = line.getPriceUnit() != null ? line.getPriceUnit().getSymbol() : unitSymbol;
        }

        private ProductBatchOverviewResponseDto toResponseDto() {
            return ProductBatchOverviewResponseDto.builder()
                    .id(id)
                    .productId(productId)
                    .productCode(productCode)
                    .productName(productName)
                    .laboratoryId(laboratoryId)
                    .laboratoryCode(laboratoryCode)
                    .laboratoryName(laboratoryName)
                    .batchCode(batchCode)
                    .locationName(locationName)
                    .unitName(unitName)
                    .unitSymbol(unitSymbol)
                    .expirationDate(expirationDate)
                    .quantityAvailable(quantityAvailable)
                    .unitPrice(unitPrice)
                    .priceUnitName(priceUnitName)
                    .priceUnitSymbol(priceUnitSymbol)
                    .status(status)
                    .notes(notes)
                    .build();
        }

        private String laboratoryName() {
            return laboratoryName;
        }

        private String productName() {
            return productName;
        }

        private java.time.LocalDate expirationDate() {
            return expirationDate;
        }

        private String batchCode() {
            return batchCode;
        }
    }
}
