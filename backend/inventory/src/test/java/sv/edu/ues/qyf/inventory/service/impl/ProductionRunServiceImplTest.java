package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunResponseDto;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;
import sv.edu.ues.qyf.inventory.entity.ProductionRun;
import sv.edu.ues.qyf.inventory.entity.ProductionRunStatus;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.Recipe;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductionRunRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class ProductionRunServiceImplTest {

    @Mock
    private ProductionRunRepository productionRunRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private LaboratoryRepository laboratoryRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private InventoryStockService inventoryStockService;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    private ProductionRunServiceImpl productionRunService;

    @BeforeEach
    void setUp() {
        productionRunService = new ProductionRunServiceImpl(
                productionRunRepository,
                recipeRepository,
                laboratoryRepository,
                inventoryMovementRepository,
                inventoryMovementService,
                inventoryStockService,
                laboratoryAccessService,
                currentUserService,
                auditLogService,
                new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void create_buildsDraftPreviewUsingFefoAllocations() {
        User currentUser = buildUser();
        Laboratory laboratory = Laboratory.builder()
                .id(3L)
                .code("LAB-3")
                .name("Laboratorio 3")
                .active(Boolean.TRUE)
                .build();
        Recipe recipe = buildRecipe();

        when(recipeRepository.findByIdAndActiveTrue(8L)).thenReturn(Optional.of(recipe));
        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(productionRunRepository.save(any(ProductionRun.class))).thenAnswer(invocation -> {
            ProductionRun productionRun = invocation.getArgument(0);
            productionRun.setId(21L);
            return productionRun;
        });
        when(inventoryStockService.getStock(9L, 3L, null)).thenReturn(List.of(
                stockRow(9L, 71L, "LOT-002", LocalDate.of(2026, 5, 20), "3"),
                stockRow(9L, 70L, "LOT-001", LocalDate.of(2026, 5, 15), "4")));

        ProductionRunResponseDto response = productionRunService.create(
                new ProductionRunRequestDto(8L, 3L, "Grupo A", "Practica guiada"));

        assertThat(response.getId()).isEqualTo(21L);
        assertThat(response.getStatus()).isEqualTo(ProductionRunStatus.DRAFT);
        assertThat(response.getReadyToConfirm()).isTrue();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getSuggestedAllocations()).hasSize(2);
        assertThat(response.getItems().get(0).getSuggestedAllocations()).extracting("batchCode")
                .containsExactly("LOT-001", "LOT-002");
        assertThat(response.getItems().get(0).getSuggestedAllocations()).extracting("suggestedQuantity")
                .containsExactly(new BigDecimal("4"), new BigDecimal("1"));
    }

    @Test
    void confirm_withInsufficientStockFailsWithoutCreatingMovement() {
        ProductionRun productionRun = buildProductionRun();

        when(productionRunRepository.findById(21L)).thenReturn(Optional.of(productionRun));
        when(inventoryStockService.getStock(9L, 3L, null)).thenReturn(List.of(stockRow(
                9L, 70L, "LOT-001", LocalDate.of(2026, 5, 15), "2")));

        assertThatThrownBy(() -> productionRunService.confirm(21L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock to confirm production run");

        verify(inventoryMovementService, never()).create(any());
    }

    @Test
    void confirm_createsExitMovementAndLinksProductionRun() {
        ProductionRun productionRun = buildProductionRun();
        InventoryMovement movement = InventoryMovement.builder().id(88L).build();

        when(productionRunRepository.findById(21L)).thenReturn(Optional.of(productionRun));
        when(inventoryStockService.getStock(9L, 3L, null)).thenReturn(List.of(
                stockRow(9L, 70L, "LOT-001", LocalDate.of(2026, 5, 15), "4"),
                stockRow(9L, 71L, "LOT-002", LocalDate.of(2026, 5, 20), "3")));
        when(inventoryMovementService.create(any())).thenReturn(InventoryMovementResponseDto.builder()
                .id(88L)
                .build());
        when(inventoryMovementRepository.findById(88L)).thenReturn(Optional.of(movement));
        when(currentUserService.getAuthenticatedUser()).thenReturn(buildUser());
        when(productionRunRepository.save(any(ProductionRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductionRunResponseDto response = productionRunService.confirm(21L);

        assertThat(response.getStatus()).isEqualTo(ProductionRunStatus.CONFIRMED);
        assertThat(response.getInventoryMovementId()).isEqualTo(88L);
        verify(inventoryMovementService).create(argThat(request ->
                request.getLines().size() == 2
                        && request.getLines().get(0).getProductBatchId().equals(70L)
                        && request.getLines().get(0).getQuantity().compareTo(new BigDecimal("4")) == 0
                        && request.getLines().get(1).getProductBatchId().equals(71L)
                        && request.getLines().get(1).getQuantity().compareTo(new BigDecimal("1")) == 0));
    }

    private ProductionRun buildProductionRun() {
        Recipe recipe = buildRecipe();
        Laboratory laboratory = Laboratory.builder()
                .id(3L)
                .code("LAB-3")
                .name("Laboratorio 3")
                .active(Boolean.TRUE)
                .build();
        User createdBy = buildUser();
        return ProductionRun.builder()
                .id(21L)
                .recipe(recipe)
                .manufacturedProduct(recipe.getManufacturedProduct())
                .laboratory(laboratory)
                .createdBy(createdBy)
                .status(ProductionRunStatus.DRAFT)
                .groupName("Grupo A")
                .notes("Practica guiada")
                .build();
    }

    private Recipe buildRecipe() {
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(7L)
                .name("Mililitro")
                .symbol("ml")
                .type(UnitType.VOLUME)
                .active(Boolean.TRUE)
                .build();
        Product product = Product.builder()
                .id(9L)
                .code("INS-9")
                .name("Glicerina")
                .baseUnit(unit)
                .location(Location.builder().id(5L).name("Estante A").active(Boolean.TRUE).build())
                .active(Boolean.TRUE)
                .build();
        Recipe recipe = Recipe.builder()
                .id(8L)
                .code("REC-001")
                .name("Receta base")
                .manufacturedProduct(ManufacturedProduct.builder()
                        .id(4L)
                        .code("FAB-001")
                        .name("Jabon liquido")
                        .active(Boolean.TRUE)
                        .build())
                .active(Boolean.TRUE)
                .build();
        recipe.getItems().add(RecipeItem.builder()
                .id(11L)
                .recipe(recipe)
                .product(product)
                .unitOfMeasure(unit)
                .quantity(new BigDecimal("5"))
                .itemOrder(1)
                .build());
        return recipe;
    }

    private User buildUser() {
        return User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();
    }

    private InventoryStockResponseDto stockRow(
            Long productId,
            Long batchId,
            String batchCode,
            LocalDate expirationDate,
            String quantity) {
        return InventoryStockResponseDto.builder()
                .productId(productId)
                .productBatchId(batchId)
                .batchCode(batchCode)
                .expirationDate(expirationDate)
                .quantityAvailable(new BigDecimal(quantity))
                .build();
    }
}
