package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.RecipeItemRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipePrintItemResponseDto;
import sv.edu.ues.qyf.inventory.dto.RecipePrintResponseDto;
import sv.edu.ues.qyf.inventory.dto.RecipeRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipeResponseDto;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.Recipe;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.RecipeMapper;
import sv.edu.ues.qyf.inventory.repository.ManufacturedProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductionRunRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeItemRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.RecipeService;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private static final String TABLE_NAME = "recipes";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";

    private final RecipeRepository recipeRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final ManufacturedProductRepository manufacturedProductRepository;
    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductionRunRepository productionRunRepository;
    private final RecipeMapper recipeMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public RecipeServiceImpl(
            RecipeRepository recipeRepository,
            RecipeItemRepository recipeItemRepository,
            ManufacturedProductRepository manufacturedProductRepository,
            ProductRepository productRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            ProductionRunRepository productionRunRepository,
            RecipeMapper recipeMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.recipeItemRepository = recipeItemRepository;
        this.manufacturedProductRepository = manufacturedProductRepository;
        this.productRepository = productRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.productionRunRepository = productionRunRepository;
        this.recipeMapper = recipeMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public RecipeResponseDto create(RecipeRequestDto request) {
        String code = normalize(request.getCode());
        validateDuplicateCode(code, null);

        ManufacturedProduct manufacturedProduct =
                manufacturedProductRepository.findByIdAndActiveTrue(request.getManufacturedProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Manufactured product not found with id: " + request.getManufacturedProductId()));

        Recipe recipe = Recipe.builder()
                .manufacturedProduct(manufacturedProduct)
                .code(code)
                .name(normalize(request.getName()))
                .description(normalizeNullable(request.getDescription()))
                .active(resolveActive(request.getActive(), Boolean.TRUE))
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);
        auditLogService.logAction(
                TABLE_NAME,
                savedRecipe.getId(),
                ACTION_CREATE,
                null,
                null,
                serializeState(savedRecipe),
                "Recipe created");

        return recipeMapper.toResponseDto(savedRecipe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeResponseDto> getAll() {
        return recipeRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(recipeMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponseDto getById(Long id) {
        return recipeMapper.toResponseDto(getActiveRecipe(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RecipePrintResponseDto getPrintableById(Long id) {
        Recipe recipe = getActiveRecipe(id);
        return RecipePrintResponseDto.builder()
                .recipeId(recipe.getId())
                .recipeCode(recipe.getCode())
                .recipeName(recipe.getName())
                .manufacturedProductId(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getId()
                        : null)
                .manufacturedProductCode(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getCode()
                        : null)
                .manufacturedProductName(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getName()
                        : null)
                .groupCode(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getGroupCode()
                        : null)
                .cycle(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getCycle()
                        : null)
                .lotNumber(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getLotNumber()
                        : null)
                .generatedAt(LocalDateTime.now())
                .items(mapPrintableItems(recipe))
                .build();
    }

    @Override
    public RecipeResponseDto update(Long id, RecipeRequestDto request) {
        Recipe recipe = getActiveRecipe(id);
        String oldValues = serializeState(recipe);
        String code = normalize(request.getCode());
        validateDuplicateCode(code, id);

        ManufacturedProduct manufacturedProduct =
                manufacturedProductRepository.findByIdAndActiveTrue(request.getManufacturedProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Manufactured product not found with id: " + request.getManufacturedProductId()));

        validateUsedRecipeStructureChange(recipe, manufacturedProduct.getId());

        recipe.setManufacturedProduct(manufacturedProduct);
        recipe.setCode(code);
        recipe.setName(normalize(request.getName()));
        recipe.setDescription(normalizeNullable(request.getDescription()));
        recipe.setActive(resolveActive(request.getActive(), recipe.getActive()));

        if (Boolean.TRUE.equals(recipe.getActive())) {
            recipe.setDeletedAt(null);
            recipe.setDeletedBy(null);
        } else if (recipe.getDeletedAt() == null) {
            recipe.setDeletedAt(LocalDateTime.now());
            recipe.setDeletedBy(currentUserService.getAuthenticatedUser());
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        auditLogService.logAction(
                TABLE_NAME,
                savedRecipe.getId(),
                ACTION_UPDATE,
                null,
                oldValues,
                serializeState(savedRecipe),
                "Recipe updated");

        return recipeMapper.toResponseDto(savedRecipe);
    }

    @Override
    public RecipeResponseDto addItem(Long recipeId, RecipeItemRequestDto request) {
        Recipe recipe = getActiveRecipe(recipeId);
        validateRecipeStructureUnlocked(recipeId);
        String oldValues = serializeState(recipe);

        if (recipeItemRepository.existsByRecipeIdAndProductId(recipeId, request.getProductId())) {
            throw new DuplicateResourceException("Recipe already contains product id: " + request.getProductId());
        }

        Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        UnitOfMeasure unitOfMeasure = unitOfMeasureRepository.findById(request.getUnitOfMeasureId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unit of measure not found with id: " + request.getUnitOfMeasureId()));

        validateRecipeItemUnit(product, unitOfMeasure);

        int nextItemOrder = recipeItemRepository.findMaxItemOrderByRecipeId(recipeId) + 1;
        recipe.getItems().add(RecipeItem.builder()
                .recipe(recipe)
                .product(product)
                .unitOfMeasure(unitOfMeasure)
                .quantity(request.getQuantity())
                .itemOrder(nextItemOrder)
                .observations(normalizeNullable(request.getObservations()))
                .build());

        Recipe savedRecipe = recipeRepository.save(recipe);
        auditLogService.logAction(
                TABLE_NAME,
                savedRecipe.getId(),
                ACTION_UPDATE,
                null,
                oldValues,
                serializeState(savedRecipe),
                "Recipe item added");

        return recipeMapper.toResponseDto(savedRecipe);
    }

    @Override
    public RecipeResponseDto deleteItem(Long recipeId, Long itemId) {
        Recipe recipe = getActiveRecipe(recipeId);
        validateRecipeStructureUnlocked(recipeId);
        String oldValues = serializeState(recipe);

        RecipeItem item = recipeItemRepository.findByIdAndRecipeId(itemId, recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe item not found with id: " + itemId));
        recipe.getItems().removeIf(currentItem -> currentItem.getId().equals(item.getId()));
        reorderItems(recipe.getItems());

        Recipe savedRecipe = recipeRepository.save(recipe);
        auditLogService.logAction(
                TABLE_NAME,
                savedRecipe.getId(),
                ACTION_UPDATE,
                null,
                oldValues,
                serializeState(savedRecipe),
                "Recipe item removed");

        return recipeMapper.toResponseDto(savedRecipe);
    }

    private Recipe getActiveRecipe(Long id) {
        return recipeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
    }

    private void validateDuplicateCode(String code, Long currentId) {
        recipeRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Recipe code already exists: " + code);
                });
    }

    private void validateRecipeItemUnit(Product product, UnitOfMeasure unitOfMeasure) {
        if (product.getBaseUnit() == null || !product.getBaseUnit().getId().equals(unitOfMeasure.getId())) {
            throw new BadRequestException("Recipe item unit must match the product base unit");
        }
    }

    private List<RecipePrintItemResponseDto> mapPrintableItems(Recipe recipe) {
        if (recipe.getItems() == null) {
            return List.of();
        }

        return recipe.getItems().stream()
                .sorted(Comparator.comparing(RecipeItem::getItemOrder).thenComparing(RecipeItem::getId))
                .map(item -> RecipePrintItemResponseDto.builder()
                        .itemOrder(item.getItemOrder())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productCode(item.getProduct() != null ? item.getProduct().getCode() : null)
                        .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                        .unitOfMeasureName(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getName() : null)
                        .unitOfMeasureSymbol(
                                item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getSymbol() : null)
                        .theoreticalQuantity(item.getQuantity())
                        .observations(item.getObservations())
                        .build())
                .toList();
    }

    private void validateUsedRecipeStructureChange(Recipe recipe, Long requestedManufacturedProductId) {
        if (!hasProductionRuns(recipe.getId())) {
            return;
        }

        Long currentManufacturedProductId = recipe.getManufacturedProduct() != null
                ? recipe.getManufacturedProduct().getId()
                : null;
        if (currentManufacturedProductId != null && !currentManufacturedProductId.equals(requestedManufacturedProductId)) {
            throw new BadRequestException(
                    "Cannot change manufactured product for a recipe that has production runs");
        }
    }

    private void validateRecipeStructureUnlocked(Long recipeId) {
        if (hasProductionRuns(recipeId)) {
            throw new BadRequestException("Cannot modify recipe structure after it has been used in a production run");
        }
    }

    private boolean hasProductionRuns(Long recipeId) {
        return recipeId != null && productionRunRepository.existsByRecipeId(recipeId);
    }

    private void reorderItems(List<RecipeItem> items) {
        for (int index = 0; index < items.size(); index++) {
            items.get(index).setItemOrder(index + 1);
        }
    }

    private Boolean resolveActive(Boolean requestedActive, Boolean currentValue) {
        return requestedActive != null ? requestedActive : currentValue;
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

    private String serializeState(Recipe recipe) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", recipe.getId());
        state.put("manufacturedProductId", recipe.getManufacturedProduct() != null
                ? recipe.getManufacturedProduct().getId()
                : null);
        state.put("code", recipe.getCode());
        state.put("name", recipe.getName());
        state.put("description", recipe.getDescription());
        state.put("active", recipe.getActive());
        state.put("deletedAt", recipe.getDeletedAt());
        state.put("deletedById", recipe.getDeletedBy() != null ? recipe.getDeletedBy().getId() : null);
        state.put(
                "items",
                recipe.getItems().stream()
                        .map(item -> Map.of(
                                "id", item.getId(),
                                "productId", item.getProduct() != null ? item.getProduct().getId() : null,
                                "unitOfMeasureId", item.getUnitOfMeasure() != null
                                        ? item.getUnitOfMeasure().getId()
                                        : null,
                                "quantity", item.getQuantity(),
                                "itemOrder", item.getItemOrder()))
                        .toList());
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize recipe audit state", exception);
        }
    }
}
