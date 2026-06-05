package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.RecipeItemRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipePrintResponseDto;
import sv.edu.ues.qyf.inventory.dto.RecipeRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipeResponseDto;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.Recipe;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.mapper.RecipeMapper;
import sv.edu.ues.qyf.inventory.repository.ManufacturedProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductionRunRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeItemRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeItemRepository recipeItemRepository;

    @Mock
    private ManufacturedProductRepository manufacturedProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @Mock
    private ProductionRunRepository productionRunRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    private RecipeServiceImpl recipeService;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeServiceImpl(
                recipeRepository,
                recipeItemRepository,
                manufacturedProductRepository,
                productRepository,
                unitOfMeasureRepository,
                productionRunRepository,
                new RecipeMapper(),
                currentUserService,
                auditLogService,
                new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void getById_returnsManufacturedProductAcademicTraceability() {
        Recipe recipe = buildRecipe(7L, "G01M", "2026-I", "LT-01");
        when(recipeRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(recipe));

        RecipeResponseDto response = recipeService.getById(9L);

        assertThat(response.getManufacturedProductGroupCode()).isEqualTo("G01M");
        assertThat(response.getManufacturedProductCycle()).isEqualTo("2026-I");
        assertThat(response.getManufacturedProductLotNumber()).isEqualTo("LT-01");
    }

    @Test
    void getPrintableById_returnsPrintableFormulaPayload() {
        Recipe recipe = buildRecipeWithItem();
        when(recipeRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(recipe));

        RecipePrintResponseDto response = recipeService.getPrintableById(9L);

        assertThat(response.getRecipeId()).isEqualTo(9L);
        assertThat(response.getManufacturedProductName()).isEqualTo("Jabon liquido");
        assertThat(response.getGroupCode()).isEqualTo("G01M");
        assertThat(response.getCycle()).isEqualTo("2026-I");
        assertThat(response.getLotNumber()).isEqualTo("LT-01");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getTheoreticalQuantity())
                .isEqualByComparingTo("5.5");
    }

    @Test
    void update_blocksChangingManufacturedProductWhenRecipeAlreadyHasProductionRuns() {
        Recipe recipe = buildRecipe(7L, "G01M", "2026-I", "LT-01");
        ManufacturedProduct replacementProduct = ManufacturedProduct.builder()
                .id(8L)
                .code("FAB-002")
                .name("Gel antibacterial")
                .groupCode("G02T")
                .cycle("2026-II")
                .lotNumber("LT-02")
                .active(Boolean.TRUE)
                .build();

        when(recipeRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.findByCode("REC-001")).thenReturn(Optional.of(recipe));
        when(manufacturedProductRepository.findByIdAndActiveTrue(8L)).thenReturn(Optional.of(replacementProduct));
        when(productionRunRepository.existsByRecipeId(9L)).thenReturn(true);

        assertThatThrownBy(() -> recipeService.update(
                        9L,
                        new RecipeRequestDto(8L, "REC-001", "Formula base", "Actualizada", Boolean.TRUE)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot change manufactured product for a recipe that has production runs");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void addItem_blocksStructuralChangesWhenRecipeAlreadyHasProductionRuns() {
        Recipe recipe = buildRecipe(7L, "G01M", "2026-I", "LT-01");
        when(recipeRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(recipe));
        when(productionRunRepository.existsByRecipeId(9L)).thenReturn(true);

        assertThatThrownBy(() -> recipeService.addItem(
                        9L,
                        new RecipeItemRequestDto(3L, 2L, java.math.BigDecimal.ONE, "Extra")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot modify recipe structure after it has been used in a production run");
    }

    private Recipe buildRecipe(Long manufacturedProductId, String groupCode, String cycle, String lotNumber) {
        ManufacturedProduct manufacturedProduct = ManufacturedProduct.builder()
                .id(manufacturedProductId)
                .code("FAB-001")
                .name("Jabon liquido")
                .groupCode(groupCode)
                .cycle(cycle)
                .lotNumber(lotNumber)
                .active(Boolean.TRUE)
                .build();

        return Recipe.builder()
                .id(9L)
                .manufacturedProduct(manufacturedProduct)
                .code("REC-001")
                .name("Formula base")
                .description("Demo")
                .active(Boolean.TRUE)
                .items(List.of())
                .build();
    }

    private Recipe buildRecipeWithItem() {
        Recipe recipe = buildRecipe(7L, "G01M", "2026-I", "LT-01");
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(4L)
                .name("Mililitro")
                .symbol("ml")
                .type(UnitType.VOLUME)
                .active(Boolean.TRUE)
                .build();
        Product product = Product.builder()
                .id(3L)
                .code("INS-001")
                .name("Glicerina")
                .active(Boolean.TRUE)
                .build();
        recipe.setItems(List.of(RecipeItem.builder()
                .id(12L)
                .recipe(recipe)
                .product(product)
                .unitOfMeasure(unit)
                .quantity(new java.math.BigDecimal("5.5"))
                .itemOrder(1)
                .observations("Agregar al final")
                .build()));
        return recipe;
    }
}
