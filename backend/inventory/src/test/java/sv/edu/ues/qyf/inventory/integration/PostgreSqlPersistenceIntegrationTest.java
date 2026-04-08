package sv.edu.ues.qyf.inventory.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
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
import sv.edu.ues.qyf.inventory.dto.ProductUpdateRequestDto;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.ProductService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;

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
    private ProductService productService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldApplyFlywayMigrationsAndCreateExpectedTables() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("6");

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

        Laboratory persistedLaboratory = laboratoryRepository.findByIdAndActiveTrue(laboratory.getId()).orElseThrow();
        assertThat(persistedLaboratory.getId()).isEqualTo(laboratory.getId());
    }

    @Test
    void shouldPersistProductBatchLinkedToProductAndLaboratory() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User uploadedBy = persistUser("batch-doc-user");
        Product product = persistProduct("BATCH-001");
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
                .status("ACTIVE")
                .active(Boolean.TRUE)
                .build());
        entityManager.flush();
        entityManager.clear();

        ProductBatch persistedBatch = productBatchRepository.findByIdAndActiveTrue(productBatch.getId()).orElseThrow();
        assertThat(persistedBatch.getProduct().getId()).isEqualTo(product.getId());
        assertThat(persistedBatch.getLaboratory().getId()).isEqualTo(laboratory.getId());
        assertThat(persistedBatch.getCertificateDocument().getId()).isEqualTo(certificateDocument.getId());
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
                List.of(new InventoryMovementLineRequestDto(product.getId(), new BigDecimal("100"), "Ingreso"))));
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
        authenticate(user);

        InventoryMovementResponseDto response = inventoryMovementService.create(new InventoryMovementRequestDto(
                MovementType.ENTRY,
                laboratory.getId(),
                "Movimiento con detalle",
                List.of(new InventoryMovementLineRequestDto(product.getId(), new BigDecimal("100"), "Linea 1"))));
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
        assertThat(persistedLine.getQuantity()).isEqualByComparingTo("100");
        assertThat(persistedLine.getLineNotes()).isEqualTo("Linea 1");
    }

    @Test
    void shouldPersistInventoryAlertLinkedToLaboratory() {
        Laboratory laboratory = persist(Laboratory.builder().build());

        InventoryAlert inventoryAlert = inventoryAlertRepository.save(InventoryAlert.builder()
                .laboratory(laboratory)
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
        assertThat(persistedAlert.getAcknowledgedAt()).isNull();
    }

    @Test
    void shouldEnforceReferentialIntegrityForProductBatchLaboratory() {
        Product product = persistProduct("FK-001");

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
                .requiresExpiration(Boolean.FALSE)
                .requiresBatchControl(Boolean.TRUE)
                .build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), "n/a", List.of()));
    }
}
