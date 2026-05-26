package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.CorrectionType;
import sv.edu.ues.qyf.inventory.entity.BatchStatus;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
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
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceImplTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryMovementLineRepository inventoryMovementLineRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private LaboratoryRepository laboratoryRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private InventoryAlertService inventoryAlertService;

    @Mock
    private AuditLogService auditLogService;

    private InventoryMovementServiceImpl inventoryMovementService;

    private InventoryMovementMapper inventoryMovementMapper;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        inventoryMovementMapper = new InventoryMovementMapper();
        objectMapper = new ObjectMapper();
        inventoryMovementService = new InventoryMovementServiceImpl(
                inventoryMovementRepository,
                inventoryMovementLineRepository,
                productRepository,
                productBatchRepository,
                laboratoryRepository,
                unitOfMeasureRepository,
                inventoryMovementMapper,
                laboratoryAccessService,
                currentUserService,
                inventoryAlertService,
                auditLogService,
                objectMapper);
    }

    @Test
    void create_entryAddsStockAndPersistsMovement() {
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(7L)
                .name("Mililitro")
                .symbol("ml")
                .type(UnitType.VOLUME)
                .active(Boolean.TRUE)
                .build();
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.ENTRY,
                3L,
                "Entrada inicial",
                List.of(new InventoryMovementLineRequestDto(
                        9L,
                        new BigDecimal("100"),
                        new BigDecimal("1.2500"),
                        7L,
                        "Carga inicial")));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(BigDecimal.ZERO)
                .baseUnit(unit)
                .requiresBatchControl(Boolean.FALSE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(unitOfMeasureRepository.findById(7L)).thenReturn(Optional.of(unit));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(15L);
            movement.getLines().get(0).setId(30L);
            return movement;
        });

        InventoryMovementResponseDto response = inventoryMovementService.create(request);

        assertThat(product.getCurrentStock()).isEqualByComparingTo("100");
        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().get(0).getQuantity()).isEqualByComparingTo("100");
        assertThat(response.getLines().get(0).getUnitPrice()).isEqualByComparingTo("1.2500");
        assertThat(response.getLines().get(0).getPriceUnitId()).isEqualTo(7L);
        verify(inventoryMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.ENTRY
                        && movement.getLaboratory().getId().equals(3L)
                        && movement.getPerformedBy().getId().equals(2L)
                        && movement.getLines().size() == 1
                        && movement.getLines().get(0).getProduct().getId().equals(9L)
                        && movement.getLines().get(0).getUnitPrice().compareTo(new BigDecimal("1.2500")) == 0
                        && movement.getLines().get(0).getPriceUnit().getId().equals(7L)));
        verify(auditLogService).logAction(
                org.mockito.ArgumentMatchers.eq("inventory_movements"),
                org.mockito.ArgumentMatchers.eq(15L),
                org.mockito.ArgumentMatchers.eq("CREATE"),
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.contains("\"movementType\":\"ENTRY\""),
                org.mockito.ArgumentMatchers.eq("Inventory movement registered"));
    }

    @Test
    void create_exitWithInsufficientStockThrowsControlledError() {
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.EXIT,
                3L,
                "Salida de laboratorio",
                List.of(new InventoryMovementLineRequestDto(9L, new BigDecimal("25"), null)));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .currentStock(new BigDecimal("10"))
                .requiresBatchControl(Boolean.FALSE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> inventoryMovementService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product PRD-9");
    }

    @Test
    void create_entryWithoutUnitPriceThrowsControlledError() {
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(7L)
                .name("Mililitro")
                .symbol("ml")
                .type(UnitType.VOLUME)
                .active(Boolean.TRUE)
                .build();
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.ENTRY,
                3L,
                "Entrada sin precio",
                List.of(new InventoryMovementLineRequestDto(9L, new BigDecimal("10"), null, 7L, "Carga")));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .currentStock(BigDecimal.ZERO)
                .baseUnit(unit)
                .requiresBatchControl(Boolean.FALSE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> inventoryMovementService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unit price is required for entry movements");
    }

    @Test
    void create_exitSubtractsStockAndPersistsMovement() {
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.EXIT,
                3L,
                "Salida controlada",
                List.of(new InventoryMovementLineRequestDto(9L, new BigDecimal("25"), "Consumo autorizado")));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(new BigDecimal("100"))
                .requiresBatchControl(Boolean.FALSE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(16L);
            movement.getLines().get(0).setId(31L);
            return movement;
        });

        InventoryMovementResponseDto response = inventoryMovementService.create(request);

        assertThat(product.getCurrentStock()).isEqualByComparingTo("75");
        assertThat(response.getId()).isEqualTo(16L);
        assertThat(response.getMovementType()).isEqualTo(MovementType.EXIT);
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().get(0).getQuantity()).isEqualByComparingTo("25");
        assertThat(response.getLines().get(0).getUnitPrice()).isNull();
        verify(inventoryAlertService).synchronizeAlerts(3L, List.of(9L), List.of());
        verify(inventoryMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.EXIT
                        && movement.getLaboratory().getId().equals(3L)
                        && movement.getPerformedBy().getId().equals(2L)
                        && movement.getLines().size() == 1
                        && movement.getLines().get(0).getProduct().getId().equals(9L)
                        && movement.getLines().get(0).getUnitPrice() == null
                        && movement.getLines().get(0).getPriceUnit() == null));
    }

    @Test
    void create_exitAllowsSameProductAcrossDifferentBatches() {
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.EXIT,
                3L,
                "Salida FEFO",
                List.of(
                        new InventoryMovementLineRequestDto(9L, 70L, new BigDecimal("10"), "Primer lote"),
                        new InventoryMovementLineRequestDto(9L, 71L, new BigDecimal("15"), "Segundo lote")));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(new BigDecimal("100"))
                .requiresBatchControl(Boolean.TRUE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        ProductBatch firstBatch = ProductBatch.builder()
                .id(70L)
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-001")
                .status(BatchStatus.ACTIVE)
                .active(Boolean.TRUE)
                .build();
        ProductBatch secondBatch = ProductBatch.builder()
                .id(71L)
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-002")
                .status(BatchStatus.ACTIVE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = buildUser();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(productBatchRepository.findByIdAndActiveTrue(70L)).thenReturn(Optional.of(firstBatch));
        when(productBatchRepository.findByIdAndActiveTrue(71L)).thenReturn(Optional.of(secondBatch));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(inventoryMovementLineRepository.calculateCurrentStockByBatchId(70L, MovementType.ENTRY))
                .thenReturn(new BigDecimal("10"));
        when(inventoryMovementLineRepository.calculateCurrentStockByBatchId(71L, MovementType.ENTRY))
                .thenReturn(new BigDecimal("20"));
        when(productBatchRepository.save(any(ProductBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(17L);
            movement.getLines().get(0).setId(40L);
            movement.getLines().get(1).setId(41L);
            return movement;
        });

        InventoryMovementResponseDto response = inventoryMovementService.create(request);

        assertThat(product.getCurrentStock()).isEqualByComparingTo("75");
        assertThat(response.getLines()).hasSize(2);
        assertThat(response.getLines()).extracting("productBatchId").containsExactly(70L, 71L);
    }

    @Test
    void getByLaboratory_validatesAccessBeforeLoadingMovements() {
        InventoryMovement movement = InventoryMovement.builder()
                .id(4L)
                .laboratory(Laboratory.builder().id(3L).build())
                .movementType(MovementType.ENTRY)
                .build();

        when(inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(3L))
                .thenReturn(List.of(movement));

        List<InventoryMovementResponseDto> result = inventoryMovementService.getByLaboratory(3L);

        assertThat(result).hasSize(1);
        InOrder inOrder = inOrder(laboratoryAccessService, inventoryMovementRepository);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(3L);
        inOrder.verify(inventoryMovementRepository).findByLaboratoryIdOrderByPerformedAtDescIdDesc(3L);
    }

    @Test
    void reverse_entryCreatesCompensatingExit() {
        UnitOfMeasure unit = buildUnit();
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(new BigDecimal("100"))
                .baseUnit(unit)
                .requiresBatchControl(Boolean.FALSE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = buildUser();
        InventoryMovement originalMovement = buildMovement(
                41L,
                MovementType.ENTRY,
                CorrectionType.NORMAL,
                laboratory,
                product,
                null,
                new BigDecimal("40"),
                new BigDecimal("1.2500"),
                unit);

        when(inventoryMovementRepository.findById(41L)).thenReturn(Optional.of(originalMovement));
        when(inventoryMovementRepository.existsByRelatedMovementIdAndCorrectionType(41L, CorrectionType.REVERSAL))
                .thenReturn(false);
        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(unitOfMeasureRepository.findById(7L)).thenReturn(Optional.of(unit));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(42L);
            movement.getLines().get(0).setId(88L);
            return movement;
        });

        InventoryMovementResponseDto response = inventoryMovementService.reverse(41L, "Registro duplicado");

        assertThat(product.getCurrentStock()).isEqualByComparingTo("60");
        assertThat(response.getMovementType()).isEqualTo(MovementType.EXIT);
        assertThat(response.getCorrectionType()).isEqualTo(CorrectionType.REVERSAL);
        assertThat(response.getRelatedMovementId()).isEqualTo(41L);
        assertThat(response.getCorrectionReason()).isEqualTo("Registro duplicado");
        verify(inventoryMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.EXIT
                        && movement.getCorrectionType() == CorrectionType.REVERSAL
                        && movement.getRelatedMovement() != null
                        && movement.getRelatedMovement().getId().equals(41L)
                        && movement.getLines().get(0).getUnitPrice().compareTo(new BigDecimal("1.2500")) == 0
                        && movement.getLines().get(0).getPriceUnit().getId().equals(7L)));
    }

    @Test
    void reverse_exitCreatesCompensatingEntry() {
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(new BigDecimal("75"))
                .requiresBatchControl(Boolean.FALSE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        User currentUser = buildUser();
        InventoryMovement originalMovement = buildMovement(
                51L,
                MovementType.EXIT,
                CorrectionType.NORMAL,
                laboratory,
                product,
                null,
                new BigDecimal("25"),
                null,
                null);

        when(inventoryMovementRepository.findById(51L)).thenReturn(Optional.of(originalMovement));
        when(inventoryMovementRepository.existsByRelatedMovementIdAndCorrectionType(51L, CorrectionType.REVERSAL))
                .thenReturn(false);
        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(52L);
            movement.getLines().get(0).setId(89L);
            return movement;
        });

        InventoryMovementResponseDto response = inventoryMovementService.reverse(51L, "Descargo erroneo");

        assertThat(product.getCurrentStock()).isEqualByComparingTo("100");
        assertThat(response.getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(response.getLines().get(0).getUnitPrice()).isNull();
        assertThat(response.getCorrectionType()).isEqualTo(CorrectionType.REVERSAL);
    }

    @Test
    void reverse_whenMovementAlreadyReversedThrowsControlledError() {
        InventoryMovement originalMovement = InventoryMovement.builder()
                .id(61L)
                .movementType(MovementType.ENTRY)
                .correctionType(CorrectionType.NORMAL)
                .laboratory(Laboratory.builder().id(3L).active(Boolean.TRUE).build())
                .lines(List.of())
                .build();

        when(inventoryMovementRepository.findById(61L)).thenReturn(Optional.of(originalMovement));
        when(inventoryMovementRepository.existsByRelatedMovementIdAndCorrectionType(61L, CorrectionType.REVERSAL))
                .thenReturn(true);

        assertThatThrownBy(() -> inventoryMovementService.reverse(61L, "Registro duplicado"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been reversed");
    }

    @Test
    void reverse_withoutReasonThrowsControlledError() {
        assertThatThrownBy(() -> inventoryMovementService.reverse(61L, "   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Reason is required");
    }

    @Test
    void reverse_entryThatWouldGenerateNegativeStockThrowsControlledError() {
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        ProductBatch batch = ProductBatch.builder()
                .id(70L)
                .batchCode("LOT-001")
                .laboratory(laboratory)
                .status(BatchStatus.ACTIVE)
                .active(Boolean.TRUE)
                .build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(new BigDecimal("10"))
                .requiresBatchControl(Boolean.TRUE)
                .requiresExpiration(Boolean.FALSE)
                .active(Boolean.TRUE)
                .build();
        batch.setProduct(product);
        InventoryMovement originalMovement = buildMovement(
                71L,
                MovementType.ENTRY,
                CorrectionType.NORMAL,
                laboratory,
                product,
                batch,
                new BigDecimal("25"),
                null,
                null);

        when(inventoryMovementRepository.findById(71L)).thenReturn(Optional.of(originalMovement));
        when(inventoryMovementRepository.existsByRelatedMovementIdAndCorrectionType(71L, CorrectionType.REVERSAL))
                .thenReturn(false);
        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(productBatchRepository.findByIdAndActiveTrue(70L)).thenReturn(Optional.of(batch));
        when(currentUserService.getAuthenticatedUser()).thenReturn(buildUser());
        when(inventoryMovementLineRepository.calculateCurrentStockByBatchId(70L, MovementType.ENTRY))
                .thenReturn(new BigDecimal("25"));

        assertThatThrownBy(() -> inventoryMovementService.reverse(71L, "Ingreso equivocado"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product PRD-9");
    }

    private UnitOfMeasure buildUnit() {
        return UnitOfMeasure.builder()
                .id(7L)
                .name("Mililitro")
                .symbol("ml")
                .type(UnitType.VOLUME)
                .active(Boolean.TRUE)
                .build();
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

    private InventoryMovement buildMovement(
            Long movementId,
            MovementType movementType,
            CorrectionType correctionType,
            Laboratory laboratory,
            Product product,
            ProductBatch batch,
            BigDecimal quantity,
            BigDecimal unitPrice,
            UnitOfMeasure priceUnit) {
        InventoryMovement movement = InventoryMovement.builder()
                .id(movementId)
                .movementType(movementType)
                .correctionType(correctionType)
                .laboratory(laboratory)
                .observation("Movimiento original")
                .lines(new java.util.ArrayList<>())
                .build();

        InventoryMovementLine line = InventoryMovementLine.builder()
                .id(movementId + 100)
                .movement(movement)
                .product(product)
                .productBatch(batch)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .priceUnit(priceUnit)
                .lineNotes("Linea original")
                .build();
        movement.getLines().add(line);
        return movement;
    }
}
