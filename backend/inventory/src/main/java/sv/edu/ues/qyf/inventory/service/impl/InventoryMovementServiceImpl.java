package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementFilterDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.BatchStatus;
import sv.edu.ues.qyf.inventory.entity.CorrectionType;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.InventoryMovementMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementLineRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.InventoryAlertService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_REVERSE = "REVERSE";
    private static final String TABLE_NAME = "inventory_movements";

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementLineRepository inventoryMovementLineRepository;
    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final LaboratoryAccessService laboratoryAccessService;
    private final CurrentUserService currentUserService;
    private final InventoryAlertService inventoryAlertService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public InventoryMovementServiceImpl(
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMovementLineRepository inventoryMovementLineRepository,
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            LaboratoryRepository laboratoryRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            InventoryMovementMapper inventoryMovementMapper,
            LaboratoryAccessService laboratoryAccessService,
            CurrentUserService currentUserService,
            InventoryAlertService inventoryAlertService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMovementLineRepository = inventoryMovementLineRepository;
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.inventoryMovementMapper = inventoryMovementMapper;
        this.laboratoryAccessService = laboratoryAccessService;
        this.currentUserService = currentUserService;
        this.inventoryAlertService = inventoryAlertService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public InventoryMovementResponseDto create(InventoryMovementRequestDto request) {
        return createInternal(request, CorrectionType.NORMAL, null, null);
    }

    @Override
    public InventoryMovementResponseDto reverse(Long id, String reason) {
        String normalizedReason = normalizeRequiredReason(reason);
        InventoryMovement originalMovement = inventoryMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory movement not found with id: " + id));

        laboratoryAccessService.validateAccessToLaboratory(originalMovement.getLaboratory().getId());

        if (resolveCorrectionType(originalMovement) == CorrectionType.REVERSAL) {
            throw new BadRequestException("Reversal movements cannot be reversed again");
        }

        if (inventoryMovementRepository.existsByRelatedMovementIdAndCorrectionType(id, CorrectionType.REVERSAL)) {
            throw new BadRequestException("This movement has already been reversed");
        }

        return createInternal(
                buildReversalRequest(originalMovement),
                CorrectionType.REVERSAL,
                originalMovement,
                normalizedReason);
    }

    private InventoryMovementResponseDto createInternal(
            InventoryMovementRequestDto request,
            CorrectionType correctionType,
            InventoryMovement relatedMovement,
            String correctionReason) {
        laboratoryAccessService.validateAccessToLaboratory(request.getLaboratoryId());

        Laboratory laboratory = laboratoryRepository.findByIdAndActiveTrue(request.getLaboratoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Laboratory not found with id: " + request.getLaboratoryId()));
        User currentUser = currentUserService.getAuthenticatedUser();
        validateNoDuplicateProductBatchCombinations(request.getLines());

        InventoryMovement movement = InventoryMovement.builder()
                .movementType(request.getMovementType())
                .correctionType(correctionType)
                .laboratory(laboratory)
                .performedBy(currentUser)
                .relatedMovement(relatedMovement)
                .correctionReason(correctionType == CorrectionType.REVERSAL ? correctionReason : null)
                .observation(normalizeNullable(request.getObservation()))
                .build();

        List<Long> affectedProductIds = new ArrayList<>();
        List<Long> affectedBatchIds = new ArrayList<>();

        for (InventoryMovementLineRequestDto lineRequest : request.getLines()) {
            Product product = productRepository.findByIdAndActiveTrue(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + lineRequest.getProductId()));
            UnitOfMeasure priceUnit = resolvePriceUnit(lineRequest, product, request.getMovementType(), correctionType);
            ProductBatch productBatch =
                    resolveProductBatch(lineRequest, laboratory, product, request.getMovementType());

            updateProductStock(product, request.getMovementType(), lineRequest.getQuantity());

            InventoryMovementLine line = InventoryMovementLine.builder()
                    .movement(movement)
                    .product(product)
                    .productBatch(productBatch)
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .priceUnit(priceUnit)
                    .lineNotes(normalizeNullable(lineRequest.getLineNotes()))
                    .build();
            movement.getLines().add(line);

            affectedProductIds.add(product.getId());
            if (productBatch != null) {
                affectedBatchIds.add(productBatch.getId());
            }
        }

        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);
        inventoryAlertService.synchronizeAlerts(laboratory.getId(), affectedProductIds, affectedBatchIds);
        auditLogService.logAction(
                TABLE_NAME,
                savedMovement.getId(),
                correctionType == CorrectionType.REVERSAL ? ACTION_REVERSE : ACTION_CREATE,
                savedMovement.getLaboratory() != null ? savedMovement.getLaboratory().getId() : null,
                null,
                serializeState(savedMovement),
                correctionType == CorrectionType.REVERSAL
                        ? "Inventory movement reversed"
                        : "Inventory movement registered");

        return inventoryMovementMapper.toResponseDto(savedMovement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDto> getAll() {
        List<InventoryMovement> movements = laboratoryAccessService.hasAccessToAllLaboratories()
                ? inventoryMovementRepository.findAllByOrderByPerformedAtDescIdDesc()
                : findByAccessibleLaboratories();

        return movements.stream()
                .map(inventoryMovementMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDto> search(InventoryMovementFilterDto filter) {
        if (filter.getDateFrom() != null
                && filter.getDateTo() != null
                && filter.getDateFrom().isAfter(filter.getDateTo())) {
            throw new BadRequestException("dateFrom must be less than or equal to dateTo");
        }

        List<InventoryMovement> movements;
        if (filter.getLaboratoryId() != null) {
            laboratoryAccessService.validateAccessToLaboratory(filter.getLaboratoryId());
            movements = inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(filter.getLaboratoryId());
        } else if (laboratoryAccessService.hasAccessToAllLaboratories()) {
            movements = inventoryMovementRepository.findAllByOrderByPerformedAtDescIdDesc();
        } else {
            List<Long> laboratoryIds = laboratoryAccessService.getAccessibleLaboratoryIds();
            if (laboratoryIds.isEmpty()) {
                return List.of();
            }
            movements = inventoryMovementRepository.findByLaboratoryIdInOrderByPerformedAtDescIdDesc(laboratoryIds);
        }

        return movements.stream()
                .filter(movement -> matchesFilter(movement, filter))
                .map(inventoryMovementMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryMovementResponseDto getById(Long id) {
        InventoryMovement movement = inventoryMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory movement not found with id: " + id));
        laboratoryAccessService.validateAccessToLaboratory(movement.getLaboratory().getId());
        return inventoryMovementMapper.toResponseDto(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponseDto> getByLaboratory(Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        return inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(laboratoryId).stream()
                .map(inventoryMovementMapper::toResponseDto)
                .toList();
    }

    private ProductBatch resolveProductBatch(
            InventoryMovementLineRequestDto lineRequest,
            Laboratory laboratory,
            Product product,
            MovementType movementType) {
        boolean batchRequired = Boolean.TRUE.equals(product.getRequiresBatchControl())
                || Boolean.TRUE.equals(product.getRequiresExpiration());
        boolean hasBatchReference = lineRequest.getProductBatchId() != null || hasText(lineRequest.getBatchCode());

        if (!batchRequired && !hasBatchReference) {
            return null;
        }

        if (!hasBatchReference) {
            throw new BadRequestException("Batch is required for product " + product.getCode());
        }

        ProductBatch productBatch = lineRequest.getProductBatchId() != null
                ? productBatchRepository.findByIdAndActiveTrue(lineRequest.getProductBatchId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product batch not found with id: " + lineRequest.getProductBatchId()))
                : productBatchRepository.findByProductIdAndLaboratoryIdAndBatchCode(
                                product.getId(), laboratory.getId(), normalize(lineRequest.getBatchCode()))
                        .orElse(null);

        if (productBatch != null && !Boolean.TRUE.equals(productBatch.getActive())) {
            throw new ResourceNotFoundException("Product batch is inactive for product " + product.getCode());
        }

        if (productBatch == null && movementType == MovementType.EXIT) {
            throw new ResourceNotFoundException("Product batch not found for product " + product.getCode());
        }

        if (productBatch == null) {
            productBatch = ProductBatch.builder()
                    .product(product)
                    .laboratory(laboratory)
                    .batchCode(normalize(lineRequest.getBatchCode()))
                    .status(BatchStatus.ACTIVE)
                    .active(Boolean.TRUE)
                    .build();
        } else {
            validateBatchOwnership(productBatch, laboratory.getId(), product.getId());
        }

        applyExpirationRules(productBatch, lineRequest.getExpirationDate(), product);
        BigDecimal batchBalanceBefore = productBatch.getId() == null
                ? BigDecimal.ZERO
                : inventoryMovementLineRepository.calculateCurrentStockByBatchId(productBatch.getId(), MovementType.ENTRY);

        if (movementType == MovementType.EXIT && batchBalanceBefore.compareTo(lineRequest.getQuantity()) < 0) {
            throw new BadRequestException(
                    "Insufficient stock for batch " + productBatch.getBatchCode() + ". Available: "
                            + batchBalanceBefore.stripTrailingZeros().toPlainString()
                            + ", requested: " + lineRequest.getQuantity().stripTrailingZeros().toPlainString());
        }

        BigDecimal batchBalanceAfter = movementType == MovementType.ENTRY
                ? batchBalanceBefore.add(lineRequest.getQuantity())
                : batchBalanceBefore.subtract(lineRequest.getQuantity());
        updateBatchStatus(productBatch, batchBalanceAfter);

        return productBatchRepository.save(productBatch);
    }

    private UnitOfMeasure resolvePriceUnit(
            InventoryMovementLineRequestDto lineRequest,
            Product product,
            MovementType movementType,
            CorrectionType correctionType) {
        if (movementType == MovementType.ENTRY && correctionType != CorrectionType.REVERSAL) {
            if (lineRequest.getUnitPrice() == null) {
                throw new BadRequestException("Unit price is required for entry movements");
            }
            if (lineRequest.getPriceUnitId() == null) {
                throw new BadRequestException("Price unit id is required when unit price is informed");
            }
        }

        if (lineRequest.getUnitPrice() == null) {
            return null;
        }

        if (lineRequest.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Unit price must be greater than or equal to 0");
        }

        if (lineRequest.getPriceUnitId() == null) {
            throw new BadRequestException("Price unit id is required when unit price is informed");
        }

        UnitOfMeasure priceUnit = unitOfMeasureRepository.findById(lineRequest.getPriceUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unit of measure not found with id: " + lineRequest.getPriceUnitId()));

        if (product.getBaseUnit() != null && !product.getBaseUnit().getId().equals(priceUnit.getId())) {
            throw new BadRequestException("Price unit must match the product base unit");
        }

        return priceUnit;
    }

    private void validateBatchOwnership(ProductBatch productBatch, Long laboratoryId, Long productId) {
        if (!productBatch.getLaboratory().getId().equals(laboratoryId)) {
            throw new BadRequestException("Product batch does not belong to the selected laboratory");
        }
        if (!productBatch.getProduct().getId().equals(productId)) {
            throw new BadRequestException("Product batch does not belong to the selected product");
        }
    }

    private void applyExpirationRules(ProductBatch productBatch, LocalDate requestedExpirationDate, Product product) {
        if (requestedExpirationDate != null) {
            if (productBatch.getExpirationDate() != null
                    && !productBatch.getExpirationDate().equals(requestedExpirationDate)) {
                throw new BadRequestException("Expiration date does not match the existing batch data");
            }
            productBatch.setExpirationDate(requestedExpirationDate);
        }

        if (Boolean.TRUE.equals(product.getRequiresExpiration()) && productBatch.getExpirationDate() == null) {
            throw new BadRequestException("Expiration date is required for product " + product.getCode());
        }
    }

    private void updateBatchStatus(ProductBatch productBatch, BigDecimal batchBalanceAfter) {
        if (productBatch.getExpirationDate() != null && !productBatch.getExpirationDate().isAfter(LocalDate.now())) {
            productBatch.setStatus(BatchStatus.EXPIRED);
            return;
        }

        if (productBatch.getStatus() == BatchStatus.QUARANTINED) {
            return;
        }

        if (batchBalanceAfter.compareTo(BigDecimal.ZERO) == 0) {
            productBatch.setStatus(BatchStatus.EXHAUSTED);
            return;
        }

        productBatch.setStatus(BatchStatus.ACTIVE);
    }

    private List<InventoryMovement> findByAccessibleLaboratories() {
        List<Long> laboratoryIds = laboratoryAccessService.getAccessibleLaboratoryIds();
        if (laboratoryIds.isEmpty()) {
            return List.of();
        }
        return inventoryMovementRepository.findByLaboratoryIdInOrderByPerformedAtDescIdDesc(laboratoryIds);
    }

    private boolean matchesFilter(InventoryMovement movement, InventoryMovementFilterDto filter) {
        if (filter.getMovementType() != null && movement.getMovementType() != filter.getMovementType()) {
            return false;
        }
        if (filter.getDateFrom() != null && movement.getPerformedAt().toLocalDate().isBefore(filter.getDateFrom())) {
            return false;
        }
        if (filter.getDateTo() != null && movement.getPerformedAt().toLocalDate().isAfter(filter.getDateTo())) {
            return false;
        }
        if (filter.getProductId() == null) {
            return true;
        }
        return movement.getLines().stream()
                .anyMatch(line -> line.getProduct().getId().equals(filter.getProductId()));
    }

    private void updateProductStock(Product product, MovementType movementType, BigDecimal quantity) {
        if (movementType == MovementType.ENTRY) {
            product.setCurrentStock(product.getCurrentStock().add(quantity));
            return;
        }

        if (product.getCurrentStock().compareTo(quantity) < 0) {
            throw new BadRequestException(
                    "Insufficient stock for product " + product.getCode() + ". Available: "
                            + product.getCurrentStock().stripTrailingZeros().toPlainString()
                            + ", requested: " + quantity.stripTrailingZeros().toPlainString());
        }

        product.setCurrentStock(product.getCurrentStock().subtract(quantity));
    }

    private void validateNoDuplicateProductBatchCombinations(List<InventoryMovementLineRequestDto> lines) {
        Set<String> lineKeys = new HashSet<>();
        for (InventoryMovementLineRequestDto line : lines) {
            String batchKey = line.getProductBatchId() != null
                    ? "batch-id:" + line.getProductBatchId()
                    : hasText(line.getBatchCode()) ? "batch-code:" + normalize(line.getBatchCode()) : "no-batch";
            String lineKey = line.getProductId() + "|" + batchKey;
            if (!lineKeys.add(lineKey)) {
                throw new BadRequestException(
                        "A product batch combination cannot be repeated in the same movement");
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String serializeState(InventoryMovement movement) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", movement.getId());
        state.put("movementType", movement.getMovementType());
        state.put("correctionType", resolveCorrectionType(movement));
        state.put("relatedMovementId", movement.getRelatedMovement() != null ? movement.getRelatedMovement().getId() : null);
        state.put("correctionReason", movement.getCorrectionReason());
        state.put("laboratoryId", movement.getLaboratory() != null ? movement.getLaboratory().getId() : null);
        state.put("performedById", movement.getPerformedBy() != null ? movement.getPerformedBy().getId() : null);
        state.put("performedAt", movement.getPerformedAt());
        state.put("observation", movement.getObservation());
        state.put(
                "lines",
                movement.getLines().stream()
                        .map(this::serializeLineState)
                        .toList());
        return writeJson(state);
    }

    private Map<String, Object> serializeLineState(InventoryMovementLine line) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("productId", line.getProduct().getId());
        state.put("productBatchId", line.getProductBatch() != null ? line.getProductBatch().getId() : null);
        state.put("quantity", line.getQuantity());
        state.put("unitPrice", line.getUnitPrice());
        state.put("priceUnitId", line.getPriceUnit() != null ? line.getPriceUnit().getId() : null);
        state.put("lineNotes", line.getLineNotes());
        return state;
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize inventory movement audit state", exception);
        }
    }

    private InventoryMovementRequestDto buildReversalRequest(InventoryMovement originalMovement) {
        List<InventoryMovementLineRequestDto> reversedLines = originalMovement.getLines().stream()
                .map(line -> new InventoryMovementLineRequestDto(
                        line.getProduct() != null ? line.getProduct().getId() : null,
                        line.getProductBatch() != null ? line.getProductBatch().getId() : null,
                        line.getProductBatch() != null ? line.getProductBatch().getBatchCode() : null,
                        line.getProductBatch() != null ? line.getProductBatch().getExpirationDate() : null,
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getPriceUnit() != null ? line.getPriceUnit().getId() : null,
                        line.getLineNotes()))
                .toList();

        return new InventoryMovementRequestDto(
                originalMovement.getMovementType() == MovementType.ENTRY ? MovementType.EXIT : MovementType.ENTRY,
                originalMovement.getLaboratory().getId(),
                "Reversion del movimiento " + originalMovement.getId(),
                reversedLines);
    }

    private String normalizeRequiredReason(String reason) {
        String normalizedReason = normalizeNullable(reason);
        if (normalizedReason == null) {
            throw new BadRequestException("Reason is required");
        }
        return normalizedReason;
    }

    private CorrectionType resolveCorrectionType(InventoryMovement movement) {
        return movement.getCorrectionType() != null ? movement.getCorrectionType() : CorrectionType.NORMAL;
    }
}
