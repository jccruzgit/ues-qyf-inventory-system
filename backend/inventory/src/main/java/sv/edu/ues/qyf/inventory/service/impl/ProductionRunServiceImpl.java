package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunAllocationResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunItemResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.ProductionRun;
import sv.edu.ues.qyf.inventory.entity.ProductionRunStatus;
import sv.edu.ues.qyf.inventory.entity.Recipe;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductionRunRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;
import sv.edu.ues.qyf.inventory.service.ProductionRunService;

@Service
@Transactional
public class ProductionRunServiceImpl implements ProductionRunService {

    private static final String TABLE_NAME = "production_runs";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_CONFIRM = "CONFIRM";

    private final ProductionRunRepository productionRunRepository;
    private final RecipeRepository recipeRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementService inventoryMovementService;
    private final InventoryStockService inventoryStockService;
    private final LaboratoryAccessService laboratoryAccessService;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ProductionRunServiceImpl(
            ProductionRunRepository productionRunRepository,
            RecipeRepository recipeRepository,
            LaboratoryRepository laboratoryRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMovementService inventoryMovementService,
            InventoryStockService inventoryStockService,
            LaboratoryAccessService laboratoryAccessService,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.productionRunRepository = productionRunRepository;
        this.recipeRepository = recipeRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMovementService = inventoryMovementService;
        this.inventoryStockService = inventoryStockService;
        this.laboratoryAccessService = laboratoryAccessService;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductionRunResponseDto create(ProductionRunRequestDto request) {
        laboratoryAccessService.validateAccessToLaboratory(request.getLaboratoryId());
        Recipe recipe = getActiveRecipe(request.getRecipeId());
        validateRecipeHasItems(recipe);
        Laboratory laboratory = getActiveLaboratory(request.getLaboratoryId());
        User currentUser = currentUserService.getAuthenticatedUser();

        ProductionRun productionRun = ProductionRun.builder()
                .recipe(recipe)
                .manufacturedProduct(recipe.getManufacturedProduct())
                .laboratory(laboratory)
                .createdBy(currentUser)
                .status(ProductionRunStatus.DRAFT)
                .groupName(normalizeNullable(request.getGroupName()))
                .notes(normalizeNullable(request.getNotes()))
                .build();

        ProductionRun savedProductionRun = productionRunRepository.save(productionRun);
        ProductionRunPreview preview = buildPreview(savedProductionRun);

        auditLogService.logAction(
                TABLE_NAME,
                savedProductionRun.getId(),
                ACTION_CREATE,
                laboratory.getId(),
                null,
                serializeState(savedProductionRun),
                "Production run draft created");

        return buildResponse(savedProductionRun, preview);
    }

    @Override
    public ProductionRunResponseDto confirm(Long id) {
        ProductionRun productionRun = getProductionRun(id);
        laboratoryAccessService.validateAccessToLaboratory(productionRun.getLaboratory().getId());

        if (productionRun.getStatus() == ProductionRunStatus.CONFIRMED) {
            throw new BadRequestException("Production run has already been confirmed");
        }

        validateRecipeHasItems(productionRun.getRecipe());
        String oldValues = serializeState(productionRun);
        ProductionRunPreview preview = buildPreview(productionRun);

        if (!preview.readyToConfirm()) {
            throw new BadRequestException(buildInsufficientStockMessage(preview.items()));
        }

        InventoryMovementRequestDto movementRequest = new InventoryMovementRequestDto(
                MovementType.EXIT,
                productionRun.getLaboratory().getId(),
                buildMovementObservation(productionRun),
                buildMovementLines(productionRun, preview.items()));
        InventoryMovementResponseDto movementResponse = inventoryMovementService.create(movementRequest);
        InventoryMovement movement = inventoryMovementRepository.findById(movementResponse.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory movement not found with id: " + movementResponse.getId()));

        User currentUser = currentUserService.getAuthenticatedUser();
        productionRun.setStatus(ProductionRunStatus.CONFIRMED);
        productionRun.setConfirmedAt(LocalDateTime.now());
        productionRun.setConfirmedBy(currentUser);
        productionRun.setInventoryMovement(movement);

        ProductionRun savedProductionRun = productionRunRepository.save(productionRun);
        auditLogService.logAction(
                TABLE_NAME,
                savedProductionRun.getId(),
                ACTION_CONFIRM,
                savedProductionRun.getLaboratory().getId(),
                oldValues,
                serializeState(savedProductionRun),
                "Production run confirmed");

        return buildResponse(savedProductionRun, preview);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionRunResponseDto getById(Long id) {
        ProductionRun productionRun = getProductionRun(id);
        laboratoryAccessService.validateAccessToLaboratory(productionRun.getLaboratory().getId());
        return buildResponse(productionRun, buildPreview(productionRun));
    }

    private Recipe getActiveRecipe(Long id) {
        return recipeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
    }

    private Laboratory getActiveLaboratory(Long id) {
        return laboratoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory not found with id: " + id));
    }

    private ProductionRun getProductionRun(Long id) {
        return productionRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Production run not found with id: " + id));
    }

    private void validateRecipeHasItems(Recipe recipe) {
        if (recipe.getItems() == null || recipe.getItems().isEmpty()) {
            throw new BadRequestException("Recipe must contain at least one item before creating a production run");
        }
    }

    private ProductionRunPreview buildPreview(ProductionRun productionRun) {
        List<ProductionRunItemSnapshot> items = productionRun.getRecipe().getItems().stream()
                .sorted(Comparator.comparing(RecipeItem::getItemOrder).thenComparing(RecipeItem::getId))
                .map(item -> buildItemSnapshot(productionRun.getLaboratory().getId(), item))
                .toList();
        boolean readyToConfirm = !items.isEmpty() && items.stream().allMatch(ProductionRunItemSnapshot::stockSufficient);
        return new ProductionRunPreview(items, readyToConfirm);
    }

    private ProductionRunItemSnapshot buildItemSnapshot(Long laboratoryId, RecipeItem item) {
        List<InventoryStockResponseDto> stockRows = inventoryStockService.getStock(item.getProduct().getId(), laboratoryId, null)
                .stream()
                .filter(stockItem -> stockItem.getQuantityAvailable() != null
                        && stockItem.getQuantityAvailable().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(
                                InventoryStockResponseDto::getExpirationDate,
                                Comparator.nullsLast(LocalDate::compareTo))
                        .thenComparing(InventoryStockResponseDto::getBatchCode, Comparator.nullsLast(String::compareTo))
                        .thenComparing(InventoryStockResponseDto::getProductBatchId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        BigDecimal requiredQuantity = item.getQuantity();
        BigDecimal totalAvailable = stockRows.stream()
                .map(InventoryStockResponseDto::getQuantityAvailable)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = requiredQuantity;
        List<ProductionRunAllocationSnapshot> allocations = new ArrayList<>();
        for (InventoryStockResponseDto stockRow : stockRows) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal availableQuantity = stockRow.getQuantityAvailable();
            BigDecimal suggestedQuantity = availableQuantity.min(remaining);
            allocations.add(new ProductionRunAllocationSnapshot(stockRow, suggestedQuantity));
            remaining = remaining.subtract(suggestedQuantity);
        }

        boolean stockSufficient = remaining.compareTo(BigDecimal.ZERO) <= 0;
        return new ProductionRunItemSnapshot(item, totalAvailable, stockSufficient, allocations);
    }

    private List<InventoryMovementLineRequestDto> buildMovementLines(
            ProductionRun productionRun, List<ProductionRunItemSnapshot> items) {
        List<InventoryMovementLineRequestDto> lines = new ArrayList<>();
        for (ProductionRunItemSnapshot itemSnapshot : items) {
            for (ProductionRunAllocationSnapshot allocation : itemSnapshot.allocations()) {
                lines.add(new InventoryMovementLineRequestDto(
                        itemSnapshot.item().getProduct().getId(),
                        allocation.stock().getProductBatchId(),
                        allocation.suggestedQuantity(),
                        buildLineNotes(productionRun, itemSnapshot.item())));
            }
        }
        return lines;
    }

    private String buildMovementObservation(ProductionRun productionRun) {
        String manufacturedProductName = productionRun.getManufacturedProduct().getName();
        String recipeCode = productionRun.getRecipe().getCode();
        StringBuilder observation = new StringBuilder("Elaboracion de ")
                .append(manufacturedProductName)
                .append(" con receta ")
                .append(recipeCode);

        if (productionRun.getGroupName() != null) {
            observation.append(" | Grupo: ").append(productionRun.getGroupName());
        }
        if (productionRun.getNotes() != null) {
            observation.append(" | ").append(productionRun.getNotes());
        }

        return observation.toString();
    }

    private String buildLineNotes(ProductionRun productionRun, RecipeItem recipeItem) {
        StringBuilder notes = new StringBuilder("Receta ")
                .append(productionRun.getRecipe().getCode())
                .append(" - ")
                .append(productionRun.getManufacturedProduct().getName());
        if (recipeItem.getObservations() != null) {
            notes.append(" | ").append(recipeItem.getObservations());
        }
        return notes.toString();
    }

    private String buildInsufficientStockMessage(List<ProductionRunItemSnapshot> items) {
        List<String> shortages = items.stream()
                .filter(item -> !item.stockSufficient())
                .map(item -> item.item().getProduct().getCode()
                        + " (required "
                        + item.item().getQuantity().stripTrailingZeros().toPlainString()
                        + ", available "
                        + item.totalAvailable().stripTrailingZeros().toPlainString()
                        + ")")
                .toList();

        return "Insufficient stock to confirm production run for: " + String.join(", ", shortages);
    }

    private ProductionRunResponseDto buildResponse(ProductionRun productionRun, ProductionRunPreview preview) {
        return ProductionRunResponseDto.builder()
                .id(productionRun.getId())
                .status(productionRun.getStatus())
                .recipeId(productionRun.getRecipe() != null ? productionRun.getRecipe().getId() : null)
                .recipeCode(productionRun.getRecipe() != null ? productionRun.getRecipe().getCode() : null)
                .recipeName(productionRun.getRecipe() != null ? productionRun.getRecipe().getName() : null)
                .manufacturedProductId(productionRun.getManufacturedProduct() != null
                        ? productionRun.getManufacturedProduct().getId()
                        : null)
                .manufacturedProductCode(productionRun.getManufacturedProduct() != null
                        ? productionRun.getManufacturedProduct().getCode()
                        : null)
                .manufacturedProductName(productionRun.getManufacturedProduct() != null
                        ? productionRun.getManufacturedProduct().getName()
                        : null)
                .laboratoryId(productionRun.getLaboratory() != null ? productionRun.getLaboratory().getId() : null)
                .laboratoryCode(productionRun.getLaboratory() != null ? productionRun.getLaboratory().getCode() : null)
                .laboratoryName(productionRun.getLaboratory() != null ? productionRun.getLaboratory().getName() : null)
                .createdById(productionRun.getCreatedBy() != null ? productionRun.getCreatedBy().getId() : null)
                .createdByUsername(productionRun.getCreatedBy() != null
                        ? productionRun.getCreatedBy().getUsername()
                        : null)
                .confirmedById(productionRun.getConfirmedBy() != null ? productionRun.getConfirmedBy().getId() : null)
                .confirmedByUsername(productionRun.getConfirmedBy() != null
                        ? productionRun.getConfirmedBy().getUsername()
                        : null)
                .createdAt(productionRun.getCreatedAt())
                .confirmedAt(productionRun.getConfirmedAt())
                .groupName(productionRun.getGroupName())
                .notes(productionRun.getNotes())
                .inventoryMovementId(productionRun.getInventoryMovement() != null
                        ? productionRun.getInventoryMovement().getId()
                        : null)
                .readyToConfirm(preview.readyToConfirm())
                .items(preview.items().stream().map(this::mapItemResponse).toList())
                .build();
    }

    private ProductionRunItemResponseDto mapItemResponse(ProductionRunItemSnapshot itemSnapshot) {
        RecipeItem item = itemSnapshot.item();
        return ProductionRunItemResponseDto.builder()
                .recipeItemId(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productCode(item.getProduct() != null ? item.getProduct().getCode() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .locationName(item.getProduct() != null && item.getProduct().getLocation() != null
                        ? item.getProduct().getLocation().getName()
                        : null)
                .unitOfMeasureId(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getId() : null)
                .unitOfMeasureName(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getName() : null)
                .unitOfMeasureSymbol(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getSymbol() : null)
                .requiredQuantity(item.getQuantity())
                .totalAvailableQuantity(itemSnapshot.totalAvailable())
                .stockSufficient(itemSnapshot.stockSufficient())
                .observations(item.getObservations())
                .suggestedAllocations(itemSnapshot.allocations().stream()
                        .map(this::mapAllocationResponse)
                        .toList())
                .build();
    }

    private ProductionRunAllocationResponseDto mapAllocationResponse(ProductionRunAllocationSnapshot allocation) {
        return ProductionRunAllocationResponseDto.builder()
                .productBatchId(allocation.stock().getProductBatchId())
                .batchCode(allocation.stock().getBatchCode())
                .expirationDate(allocation.stock().getExpirationDate())
                .availableQuantity(allocation.stock().getQuantityAvailable())
                .suggestedQuantity(allocation.suggestedQuantity())
                .build();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String serializeState(ProductionRun productionRun) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", productionRun.getId());
        state.put("recipeId", productionRun.getRecipe() != null ? productionRun.getRecipe().getId() : null);
        state.put(
                "manufacturedProductId",
                productionRun.getManufacturedProduct() != null ? productionRun.getManufacturedProduct().getId() : null);
        state.put("laboratoryId", productionRun.getLaboratory() != null ? productionRun.getLaboratory().getId() : null);
        state.put("status", productionRun.getStatus());
        state.put("groupName", productionRun.getGroupName());
        state.put("notes", productionRun.getNotes());
        state.put("createdById", productionRun.getCreatedBy() != null ? productionRun.getCreatedBy().getId() : null);
        state.put("confirmedById", productionRun.getConfirmedBy() != null ? productionRun.getConfirmedBy().getId() : null);
        state.put(
                "inventoryMovementId",
                productionRun.getInventoryMovement() != null ? productionRun.getInventoryMovement().getId() : null);
        state.put("createdAt", productionRun.getCreatedAt());
        state.put("confirmedAt", productionRun.getConfirmedAt());
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize production run audit state", exception);
        }
    }

    private record ProductionRunPreview(List<ProductionRunItemSnapshot> items, boolean readyToConfirm) {}

    private record ProductionRunItemSnapshot(
            RecipeItem item,
            BigDecimal totalAvailable,
            boolean stockSufficient,
            List<ProductionRunAllocationSnapshot> allocations) {}

    private record ProductionRunAllocationSnapshot(
            InventoryStockResponseDto stock, BigDecimal suggestedQuantity) {}
}
