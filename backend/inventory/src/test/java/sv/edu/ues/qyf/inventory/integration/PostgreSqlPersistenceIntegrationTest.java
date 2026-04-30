package sv.edu.ues.qyf.inventory.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.qyf.inventory.InventoryBackendApplication;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.BatchStatus;
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.entity.CorrectionType;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.ProductDocument;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductUpdateRequestDto;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.InventoryAlertService;
import sv.edu.ues.qyf.inventory.service.ProductService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;

@SpringBootTest(
        classes = InventoryBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.show-sql=false",
            "spring.flyway.clean-disabled=true"
        })
@Testcontainers
@Transactional
class PostgreSqlPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("qyf_inventory_test")
            .withUsername("qyf_user")
            .withPassword("qyf_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.flyway.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.flyway.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LaboratoryRepository laboratoryRepository;

    @Autowired
    private ProductBatchRepository productBatchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private InventoryAlertRepository inventoryAlertRepository;

    @Autowired
    private InventoryMovementService inventoryMovementService;

    @Autowired
    private InventoryStockService inventoryStockService;

    @Autowired
    private InventoryAlertService inventoryAlertService;

    @Autowired
    private ProductService productService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldApplyFlywayMigrationsAndCreateExpectedTables() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("10");

        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN (
                      'laboratories',
                      'product_batches',
                      'inventory_movements',
                      'inventory_movement_lines',
                      'inventory_alerts'
                  )
                """,
                Integer.class);

        assertThat(tableCount).isEqualTo(5);
    }

    @Test
    void shouldPersistLaboratory() {
        Laboratory laboratory = persist(Laboratory.builder().build());

        assertThat(laboratory.getId()).isNotNull();
        assertThat(laboratory.getActive()).isTrue();
        assertThat(laboratory.getCode()).isNotBlank();
        assertThat(laboratory.getName()).isNotBlank();
        assertThat(laboratory.getCreatedAt()).isNotNull();
        assertThat(laboratory.getUpdatedAt()).isNotNull();

        Laboratory persistedLaboratory = laboratoryRepository.findByIdAndActiveTrue(laboratory.getId()).orElseThrow();
        assertThat(persistedLaboratory.getId()).isEqualTo(laboratory.getId());
        assertThat(persistedLaboratory.getCode()).isEqualTo(laboratory.getCode());
    }

    @Test
    void shouldPersistProductBatchLinkedToProductAndLaboratory() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User uploadedBy = persistUser("batch-doc-user");
        Product product = persistProduct("BATCH-001", BigDecimal.TEN, false, true);
        ProductDocument certificateDocument = persist(ProductDocument.builder()
                .product(product)
                .fileName("batch-certificate.pdf")
                .originalName("batch-certificate.pdf")
                .fileType("PDF")
                .filePath("/logical/docs/batch-certificate.pdf")
                .uploadedBy(uploadedBy)
                .active(Boolean.TRUE)
                .build());

        ProductBatch productBatch = productBatchRepository.save(ProductBatch.builder()
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-2026-001")
                .certificateDocument(certificateDocument)
                .status(BatchStatus.ACTIVE)
                .expirationDate(LocalDate.of(2026, 12, 31))
                .active(Boolean.TRUE)
                .build());
        entityManager.flush();
        entityManager.clear();

        ProductBatch persistedBatch = productBatchRepository.findByIdAndActiveTrue(productBatch.getId()).orElseThrow();
        assertThat(persistedBatch.getProduct().getId()).isEqualTo(product.getId());
        assertThat(persistedBatch.getLaboratory().getId()).isEqualTo(laboratory.getId());
        assertThat(persistedBatch.getCertificateDocument().getId()).isEqualTo(certificateDocument.getId());
        assertThat(persistedBatch.getStatus()).isEqualTo(BatchStatus.ACTIVE);
        assertThat(persistedBatch.getExpirationDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void shouldRegisterEntryMovementAndPersistStockAtOneHundred() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("entry-user");
        Product product = persistProduct("MOVE-ENTRY", BigDecimal.ZERO);
        authenticate(user);

        InventoryMovementResponseDto response = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Entrada inicial",
                List.of(buildEntryLineRequest(product, new BigDecimal("100"), new BigDecimal("1.2500"), "Ingreso"))));
        entityManager.flush();
        entityManager.clear();

        Product persistedProduct = entityManager.find(Product.class, product.getId());
        InventoryMovement persistedMovement = inventoryMovementRepository.findById(response.getId()).orElseThrow();

        assertThat(persistedProduct.getCurrentStock()).isEqualByComparingTo("100");
        assertThat(persistedMovement.getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(persistedMovement.getPerformedBy().getId()).isEqualTo(user.getId());
        assertThat(persistedMovement.getLines()).hasSize(1);
        assertThat(persistedMovement.getLines().get(0).getQuantity()).isEqualByComparingTo("100");
    }

    @Test
    void shouldRegisterExitMovementAndPersistStockAtSeventyFive() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("exit-user");
        Product product = persistProduct("MOVE-EXIT", new BigDecimal("100"));
        authenticate(user);

        InventoryMovementResponseDto response = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.EXIT,
                laboratory.getId(),
                "Salida de reactivo",
                List.of(new InventoryMovementLineRequestDto(product.getId(), new BigDecimal("25"), "Consumo"))));
        entityManager.flush();
        entityManager.clear();

        Product persistedProduct = entityManager.find(Product.class, product.getId());
        InventoryMovement persistedMovement = inventoryMovementRepository.findById(response.getId()).orElseThrow();

        assertThat(persistedProduct.getCurrentStock()).isEqualByComparingTo("75");
        assertThat(persistedMovement.getMovementType()).isEqualTo(MovementType.EXIT);
        assertThat(persistedMovement.getLines()).hasSize(1);
        assertThat(persistedMovement.getLines().get(0).getProduct().getId()).isEqualTo(product.getId());
        assertThat(persistedMovement.getLines().get(0).getQuantity()).isEqualByComparingTo("25");
    }

    @Test
    void shouldKeepCurrentStockUnchangedWhenProductIsUpdated() {
        Product product = persistProduct("PROD-UPDATE", new BigDecimal("75"));

        productService.update(product.getId(), new ProductUpdateRequestDto(
                "PROD-UPDATE",
                "Producto actualizado",
                "Descripcion actualizada",
                product.getCategory().getId(),
                product.getBaseUnit().getId(),
                new BigDecimal("5"),
                product.getLocation().getId(),
                "Nueva observacion",
                "Seco",
                Boolean.FALSE,
                Boolean.TRUE,
                Boolean.TRUE));
        entityManager.flush();
        entityManager.clear();

        Product persistedProduct = entityManager.find(Product.class, product.getId());

        assertThat(persistedProduct.getCurrentStock()).isEqualByComparingTo("75");
        assertThat(persistedProduct.getName()).isEqualTo("Producto actualizado");
        assertThat(persistedProduct.getMinimumStock()).isEqualByComparingTo("5");
    }

    @Test
    void shouldRejectExitMovementWhenStockIsInsufficient() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("insufficient-user");
        Product product = persistProduct("MOVE-FAIL", new BigDecimal("10"));
        authenticate(user);

        assertThatThrownBy(() -> inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.EXIT,
                laboratory.getId(),
                "Salida invalida",
                List.of(new InventoryMovementLineRequestDto(product.getId(), new BigDecimal("25"), null)))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product");
    }

    @Test
    void shouldPersistInventoryMovementHeaderAndLinesWhenRegisteredThroughService() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("persistence-user");
        Product product = persistProduct("MOVE-PERSIST", BigDecimal.ZERO);
        ProductBatch productBatch = persist(ProductBatch.builder()
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-MOVE-001")
                .status(BatchStatus.ACTIVE)
                .expirationDate(LocalDate.of(2026, 10, 31))
                .active(Boolean.TRUE)
                .build());
        authenticate(user);

        InventoryMovementResponseDto response = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Movimiento con detalle",
                List.of(buildEntryBatchLineRequest(
                        product, productBatch.getId(), new BigDecimal("100"), new BigDecimal("1.2500"), "Linea 1"))));
        entityManager.flush();
        entityManager.clear();

        InventoryMovement persistedMovement = inventoryMovementRepository.findById(response.getId()).orElseThrow();

        assertThat(persistedMovement.getLaboratory().getId()).isEqualTo(laboratory.getId());
        assertThat(persistedMovement.getPerformedBy().getId()).isEqualTo(user.getId());
        assertThat(persistedMovement.getPerformedAt()).isNotNull();
        assertThat(persistedMovement.getObservation()).isEqualTo("Movimiento con detalle");
        assertThat(persistedMovement.getLines()).hasSize(1);
        InventoryMovementLine persistedLine = persistedMovement.getLines().get(0);
        assertThat(persistedLine.getProduct().getId()).isEqualTo(product.getId());
        assertThat(persistedLine.getProductBatch().getId()).isEqualTo(productBatch.getId());
        assertThat(persistedLine.getQuantity()).isEqualByComparingTo("100");
        assertThat(persistedLine.getLineNotes()).isEqualTo("Linea 1");
        assertThat(response.getLines().get(0).getProductBatchId()).isEqualTo(productBatch.getId());
        assertThat(response.getLines().get(0).getBatchCode()).isEqualTo("LOT-MOVE-001");
    }

    @Test
    void shouldPersistInventoryAlertLinkedToLaboratory() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        Product product = persistProduct("ALT-001", BigDecimal.TEN, false, true);
        ProductBatch productBatch = persist(ProductBatch.builder()
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-ALERT-001")
                .status(BatchStatus.ACTIVE)
                .expirationDate(LocalDate.of(2026, 9, 15))
                .active(Boolean.TRUE)
                .build());

        InventoryAlert inventoryAlert = inventoryAlertRepository.save(InventoryAlert.builder()
                .laboratory(laboratory)
                .alertType(InventoryAlertType.EXPIRING_BATCH)
                .product(product)
                .productBatch(productBatch)
                .message("Batch expires within 30 days")
                .build());
        entityManager.flush();
        entityManager.clear();

        InventoryAlert persistedAlert = inventoryAlertRepository
                .findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(laboratory.getId())
                .stream()
                .findFirst()
                .orElseThrow();

        assertThat(persistedAlert.getId()).isEqualTo(inventoryAlert.getId());
        assertThat(persistedAlert.getLaboratory().getId()).isEqualTo(laboratory.getId());
        assertThat(persistedAlert.getProduct().getId()).isEqualTo(product.getId());
        assertThat(persistedAlert.getProductBatch().getId()).isEqualTo(productBatch.getId());
        assertThat(persistedAlert.getAlertType()).isEqualTo(InventoryAlertType.EXPIRING_BATCH);
        assertThat(persistedAlert.getMessage()).isEqualTo("Batch expires within 30 days");
        assertThat(persistedAlert.getTriggeredAt()).isNotNull();
        assertThat(persistedAlert.getAcknowledgedAt()).isNull();
    }

    @Test
    void shouldEnforceReferentialIntegrityForProductBatchLaboratory() {
        Product product = persistProduct("FK-001", BigDecimal.TEN, false, true);

        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                INSERT INTO product_batches (product_id, lab_id, batch_code, status, is_active)
                VALUES (?, ?, ?, ?, ?)
                """,
                product.getId(),
                999999L,
                "LOT-FK-FAIL",
                "ACTIVE",
                true))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectNegativeCurrentStockAtDatabaseLevel() {
        Category category = persist(Category.builder()
                .name("Category-NEG")
                .description("Negative stock test category")
                .active(Boolean.TRUE)
                .build());
        UnitOfMeasure unit = persist(UnitOfMeasure.builder()
                .name("Unit-NEG")
                .symbol("uneg")
                .type(UnitType.COUNT)
                .active(Boolean.TRUE)
                .build());
        Location location = persist(Location.builder()
                .name("Location-NEG")
                .description("Negative stock test location")
                .active(Boolean.TRUE)
                .build());

        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                INSERT INTO products (
                    code,
                    name,
                    description,
                    category_id,
                    base_unit_id,
                    minimum_stock,
                    current_stock,
                    location_id,
                    active,
                    created_at,
                    updated_at,
                    requires_expiration,
                    requires_batch_control
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)
                """,
                "NEG-001",
                "Negative stock product",
                "Constraint validation test",
                category.getId(),
                unit.getId(),
                BigDecimal.ZERO,
                new BigDecimal("-1"),
                location.getId(),
                true,
                false,
                true))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldCreateBatchOnEntryAndExposeStockByLaboratoryAndBatch() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("batch-entry-user");
        Product product = persistProduct("BATCH-MOVE-001", BigDecimal.ZERO, true, true);
        authenticate(user);

        InventoryMovementResponseDto response = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Entrada por lote",
                List.of(new InventoryMovementLineRequestDto(
                        product.getId(),
                        null,
                        "LOT-ENTRY-001",
                        LocalDate.now().plusDays(20),
                        new BigDecimal("40"),
                        new BigDecimal("0.9500"),
                        product.getBaseUnit().getId(),
                        "Entrada lote"))));
        entityManager.flush();
        entityManager.clear();

        ProductBatch persistedBatch = productBatchRepository
                .findByProductIdAndLaboratoryIdAndBatchCode(product.getId(), laboratory.getId(), "LOT-ENTRY-001")
                .orElseThrow();
        List<InventoryStockResponseDto> stock = inventoryStockService.getStock(product.getId(), laboratory.getId(), persistedBatch.getId());

        assertThat(response.getLines().get(0).getBatchCode()).isEqualTo("LOT-ENTRY-001");
        assertThat(persistedBatch.getExpirationDate()).isEqualTo(LocalDate.now().plusDays(20));
        assertThat(stock).hasSize(1);
        assertThat(stock.get(0).getQuantityAvailable()).isEqualByComparingTo("40");
        assertThat(stock.get(0).getProductBatchId()).isEqualTo(persistedBatch.getId());
    }

    @Test
    void shouldConsultStockByProductAndLaboratory() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("stock-query-user");
        Product product = persistProduct("STOCK-QUERY-001", BigDecimal.ZERO);
        authenticate(user);

        inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Entrada para consulta",
                List.of(buildEntryLineRequest(product, new BigDecimal("100"), new BigDecimal("1.0500"), "Ingreso"))));
        inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.EXIT,
                laboratory.getId(),
                "Salida para consulta",
                List.of(new InventoryMovementLineRequestDto(product.getId(), new BigDecimal("25"), "Consumo"))));
        entityManager.flush();
        entityManager.clear();

        List<InventoryStockResponseDto> globalStock = inventoryStockService.getStock(product.getId(), null, null);
        List<InventoryStockResponseDto> laboratoryStock = inventoryStockService.getStock(product.getId(), laboratory.getId(), null);

        assertThat(globalStock).hasSize(1);
        assertThat(globalStock.get(0).getQuantityAvailable()).isEqualByComparingTo("75");
        assertThat(globalStock.get(0).getProductId()).isEqualTo(product.getId());

        assertThat(laboratoryStock).hasSize(1);
        assertThat(laboratoryStock.get(0).getLaboratoryId()).isEqualTo(laboratory.getId());
        assertThat(laboratoryStock.get(0).getQuantityAvailable()).isEqualByComparingTo("75");
        assertThat(laboratoryStock.get(0).getProductId()).isEqualTo(product.getId());
    }

    @Test
    void shouldReverseEntryMovementAsCompensatingExitAndKeepBatchStockConsistent() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("reverse-entry-user");
        Product product = persistProduct("REVERSE-ENTRY-001", BigDecimal.ZERO, true, true);
        authenticate(user);

        InventoryMovementResponseDto entryResponse = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Entrada a revertir",
                List.of(new InventoryMovementLineRequestDto(
                        product.getId(),
                        null,
                        "LOT-REV-001",
                        LocalDate.now().plusDays(15),
                        new BigDecimal("40"),
                        new BigDecimal("1.4000"),
                        product.getBaseUnit().getId(),
                        "Ingreso"))));
        ProductBatch batch = productBatchRepository
                .findByProductIdAndLaboratoryIdAndBatchCode(product.getId(), laboratory.getId(), "LOT-REV-001")
                .orElseThrow();

        InventoryMovementResponseDto reversalResponse =
                inventoryMovementService.reverse(entryResponse.getId(), "Ingreso equivocado");
        entityManager.flush();
        entityManager.clear();

        Product persistedProduct = entityManager.find(Product.class, product.getId());
        InventoryMovement reversedMovement = inventoryMovementRepository.findById(reversalResponse.getId()).orElseThrow();
        List<InventoryStockResponseDto> globalStock = inventoryStockService.getStock(product.getId(), null, null);
        List<InventoryStockResponseDto> laboratoryStock =
                inventoryStockService.getStock(product.getId(), laboratory.getId(), null);
        List<InventoryStockResponseDto> batchStock =
                inventoryStockService.getStock(product.getId(), laboratory.getId(), batch.getId());

        assertThat(persistedProduct.getCurrentStock()).isEqualByComparingTo("0");
        assertThat(reversedMovement.getMovementType()).isEqualTo(MovementType.EXIT);
        assertThat(reversedMovement.getCorrectionType()).isEqualTo(CorrectionType.REVERSAL);
        assertThat(reversedMovement.getRelatedMovement().getId()).isEqualTo(entryResponse.getId());
        assertThat(reversedMovement.getCorrectionReason()).isEqualTo("Ingreso equivocado");
        assertThat(globalStock).hasSize(1);
        assertThat(globalStock.get(0).getQuantityAvailable()).isEqualByComparingTo("0");
        assertThat(laboratoryStock).isEmpty();
        assertThat(batchStock).hasSize(1);
        assertThat(batchStock.get(0).getQuantityAvailable()).isEqualByComparingTo("0");
    }

    @Test
    void shouldReverseExitMovementAsCompensatingEntry() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("reverse-exit-user");
        Product product = persistProduct("REVERSE-EXIT-001", BigDecimal.ZERO);
        authenticate(user);

        inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Entrada base",
                List.of(buildEntryLineRequest(product, new BigDecimal("100"), new BigDecimal("1.1500"), "Ingreso base"))));

        InventoryMovementResponseDto exitResponse = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.EXIT,
                laboratory.getId(),
                "Salida a revertir",
                List.of(new InventoryMovementLineRequestDto(product.getId(), new BigDecimal("25"), "Consumo"))));

        InventoryMovementResponseDto reversalResponse =
                inventoryMovementService.reverse(exitResponse.getId(), "Descargo erroneo");
        entityManager.flush();
        entityManager.clear();

        Product persistedProduct = entityManager.find(Product.class, product.getId());
        InventoryMovement reversedMovement = inventoryMovementRepository.findById(reversalResponse.getId()).orElseThrow();
        List<InventoryStockResponseDto> globalStock = inventoryStockService.getStock(product.getId(), null, null);
        List<InventoryStockResponseDto> laboratoryStock =
                inventoryStockService.getStock(product.getId(), laboratory.getId(), null);

        assertThat(persistedProduct.getCurrentStock()).isEqualByComparingTo("100");
        assertThat(reversedMovement.getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(reversedMovement.getCorrectionType()).isEqualTo(CorrectionType.REVERSAL);
        assertThat(reversedMovement.getRelatedMovement().getId()).isEqualTo(exitResponse.getId());
        assertThat(globalStock).hasSize(1);
        assertThat(globalStock.get(0).getQuantityAvailable()).isEqualByComparingTo("100");
        assertThat(laboratoryStock).hasSize(1);
        assertThat(laboratoryStock.get(0).getQuantityAvailable()).isEqualByComparingTo("100");
    }

    @Test
    void shouldGenerateLowStockAndExpiringBatchAlerts() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User user = persistUser("alert-sync-user");
        Product product = persistProduct("ALERT-SYNC-001", BigDecimal.ZERO, true, true);
        product.setMinimumStock(new BigDecimal("40"));
        entityManager.flush();
        authenticate(user);

        inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Entrada para lote",
                List.of(new InventoryMovementLineRequestDto(
                        product.getId(),
                        null,
                        "LOT-ALERT-SYNC",
                        LocalDate.now().plusDays(10),
                        new BigDecimal("50"),
                        new BigDecimal("1.1000"),
                        product.getBaseUnit().getId(),
                        "Ingreso"))));
        ProductBatch batch = productBatchRepository
                .findByProductIdAndLaboratoryIdAndBatchCode(product.getId(), laboratory.getId(), "LOT-ALERT-SYNC")
                .orElseThrow();

        inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.EXIT,
                laboratory.getId(),
                "Salida con alerta",
                List.of(new InventoryMovementLineRequestDto(
                        product.getId(),
                        batch.getId(),
                        new BigDecimal("20"),
                        "Consumo"))));
        entityManager.flush();
        entityManager.clear();

        List<sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto> alerts =
                inventoryAlertService.getAlerts(laboratory.getId(), null, true);

        assertThat(alerts).extracting("alertType")
                .contains(InventoryAlertType.LOW_STOCK, InventoryAlertType.EXPIRING_BATCH);
    }

    private User persistUser(String username) {
        Role role = persist(Role.builder()
                .name("ROLE_" + username.toUpperCase())
                .description("Integration test role")
                .build());

        return persist(User.builder()
                .username(username)
                .email(username + "@test.local")
                .password("encoded-password")
                .fullName("Test User " + username)
                .active(Boolean.TRUE)
                .accessScope(AccessScope.ALL_LABS)
                .role(role)
                .build());
    }

    private Product persistProduct(String code) {
        return persistProduct(code, BigDecimal.TEN);
    }

    private Product persistProduct(String code, BigDecimal currentStock) {
        return persistProduct(code, currentStock, false, false);
    }

    private Product persistProduct(
            String code,
            BigDecimal currentStock,
            boolean requiresBatchControl,
            boolean requiresExpiration) {
        Category category = persist(Category.builder()
                .name("Category-" + code)
                .description("Integration test category")
                .active(Boolean.TRUE)
                .build());
        UnitOfMeasure unit = persist(UnitOfMeasure.builder()
                .name("Unit-" + code)
                .symbol("u" + code.toLowerCase())
                .type(UnitType.COUNT)
                .active(Boolean.TRUE)
                .build());
        Location location = persist(Location.builder()
                .name("Location-" + code)
                .description("Integration test location")
                .active(Boolean.TRUE)
                .build());

        return persist(Product.builder()
                .code(code)
                .name("Product " + code)
                .description("Integration test product")
                .category(category)
                .baseUnit(unit)
                .minimumStock(BigDecimal.ZERO)
                .currentStock(currentStock)
                .location(location)
                .active(Boolean.TRUE)
                .requiresExpiration(requiresExpiration)
                .requiresBatchControl(requiresBatchControl)
                .build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private InventoryMovementLineRequestDto buildEntryLineRequest(
            Product product,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String lineNotes) {
        return new InventoryMovementLineRequestDto(
                product.getId(),
                quantity,
                unitPrice,
                product.getBaseUnit().getId(),
                lineNotes);
    }

    private InventoryMovementLineRequestDto buildEntryBatchLineRequest(
            Product product,
            Long productBatchId,
            BigDecimal quantity,
            BigDecimal unitPrice,
            String lineNotes) {
        return new InventoryMovementLineRequestDto(
                product.getId(),
                productBatchId,
                quantity,
                unitPrice,
                product.getBaseUnit().getId(),
                lineNotes);
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), "n/a", List.of()));
    }
}
