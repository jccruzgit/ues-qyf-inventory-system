package sv.edu.ues.qyf.inventory.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
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
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.ProductDocument;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;

import java.math.BigDecimal;

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
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private InventoryAlertRepository inventoryAlertRepository;

    @Test
    void shouldApplyFlywayMigrationsAndCreateExpectedTables() {
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("5");

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
    void shouldPersistInventoryMovementAndMovementLine() {
        Laboratory laboratory = persist(Laboratory.builder().build());
        User uploadedBy = persistUser("movement-doc-user");
        Product product = persistProduct("MOVE-001");
        ProductDocument attachmentDocument = persist(ProductDocument.builder()
                .product(product)
                .fileName("movement-attachment.png")
                .originalName("movement-attachment.png")
                .fileType("PNG")
                .filePath("/logical/docs/movement-attachment.png")
                .uploadedBy(uploadedBy)
                .active(Boolean.TRUE)
                .build());

        InventoryMovement inventoryMovement = inventoryMovementRepository.save(InventoryMovement.builder()
                .laboratory(laboratory)
                .attachmentDocument(attachmentDocument)
                .build());
        InventoryMovementLine movementLine = persist(InventoryMovementLine.builder()
                .lineNotes("Entrada inicial de lote")
                .build());
        entityManager.clear();

        InventoryMovement persistedMovement = inventoryMovementRepository.findById(inventoryMovement.getId()).orElseThrow();
        InventoryMovementLine persistedLine = entityManager.find(InventoryMovementLine.class, movementLine.getId());

        assertThat(persistedMovement.getLaboratory().getId()).isEqualTo(laboratory.getId());
        assertThat(persistedMovement.getAttachmentDocument().getId()).isEqualTo(attachmentDocument.getId());
        assertThat(persistedLine.getLineNotes()).isEqualTo("Entrada inicial de lote");
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
                .currentStock(BigDecimal.TEN)
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
}
