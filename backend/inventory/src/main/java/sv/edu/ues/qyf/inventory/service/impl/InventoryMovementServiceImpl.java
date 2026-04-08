package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.InventoryMovementMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private static final String ACTION_CREATE = "CREATE";
    private static final String TABLE_NAME = "inventory_movements";

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final LaboratoryAccessService laboratoryAccessService;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public InventoryMovementServiceImpl(
            InventoryMovementRepository inventoryMovementRepository,
            ProductRepository productRepository,
            LaboratoryRepository laboratoryRepository,
            InventoryMovementMapper inventoryMovementMapper,
            LaboratoryAccessService laboratoryAccessService,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.productRepository = productRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.inventoryMovementMapper = inventoryMovementMapper;
        this.laboratoryAccessService = laboratoryAccessService;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public InventoryMovementResponseDto create(InventoryMovementRequestDto request) {
        laboratoryAccessService.validateAccessToLaboratory(request.getLaboratoryId());

        Laboratory laboratory = laboratoryRepository.findByIdAndActiveTrue(request.getLaboratoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Laboratory not found with id: " + request.getLaboratoryId()));
        User currentUser = currentUserService.getAuthenticatedUser();
        validateNoDuplicateProducts(request.getLines());

        InventoryMovement movement = InventoryMovement.builder()
                .movementType(request.getMovementType())
                .laboratory(laboratory)
                .performedBy(currentUser)
                .observation(normalizeNullable(request.getObservation()))
                .build();

        for (InventoryMovementLineRequestDto lineRequest : request.getLines()) {
            Product product = productRepository.findByIdAndActiveTrue(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + lineRequest.getProductId()));

            updateProductStock(product, request.getMovementType(), lineRequest.getQuantity());

            InventoryMovementLine line = InventoryMovementLine.builder()
                    .movement(movement)
                    .product(product)
                    .quantity(lineRequest.getQuantity())
                    .lineNotes(normalizeNullable(lineRequest.getLineNotes()))
                    .build();
            movement.getLines().add(line);
        }

        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);
        auditLogService.logAction(
                TABLE_NAME,
                savedMovement.getId(),
                ACTION_CREATE,
                savedMovement.getLaboratory() != null ? savedMovement.getLaboratory().getId() : null,
                null,
                serializeState(savedMovement),
                "Inventory movement registered");

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

    private List<InventoryMovement> findByAccessibleLaboratories() {
        List<Long> laboratoryIds = laboratoryAccessService.getAccessibleLaboratoryIds();
        if (laboratoryIds.isEmpty()) {
            return List.of();
        }
        return inventoryMovementRepository.findByLaboratoryIdInOrderByPerformedAtDescIdDesc(laboratoryIds);
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

    private void validateNoDuplicateProducts(List<InventoryMovementLineRequestDto> lines) {
        Set<Long> productIds = new HashSet<>();
        for (InventoryMovementLineRequestDto line : lines) {
            if (!productIds.add(line.getProductId())) {
                throw new BadRequestException("A product cannot be repeated in the same movement");
            }
        }
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
        state.put("quantity", line.getQuantity());
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
}
