package sv.edu.ues.qyf.inventory.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.CategoryRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.LocationRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.DefaultAdminSeederService;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;

@Configuration
public class DemoDataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataInitializer.class);

    @Bean
    @ConditionalOnProperty(prefix = "app.demo.seed", name = "enabled", havingValue = "true")
    ApplicationRunner demoDataRunner(
            LaboratoryRepository laboratoryRepository,
            CategoryRepository categoryRepository,
            LocationRepository locationRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            ProductRepository productRepository,
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMovementService inventoryMovementService,
            DefaultAdminSeederService defaultAdminSeederService) {
        return args -> {
            User adminUser = defaultAdminSeederService.ensureDefaultAdminSilently()
                    .orElseThrow(() -> new IllegalStateException(
                            "Demo seed requires APP_DEFAULT_ADMIN_ENABLED=true and a non-empty APP_DEFAULT_ADMIN_PASSWORD."));

            Laboratory laboratory = laboratoryRepository.findByCode("LAB-DEMO")
                    .orElseGet(() -> laboratoryRepository.save(Laboratory.builder()
                            .code("LAB-DEMO")
                            .name("Laboratorio Demo QYF")
                            .description("Laboratory seeded for local demo and frontend validation")
                            .active(Boolean.TRUE)
                            .build()));

            Category category = categoryRepository.findByName("Reactivos")
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name("Reactivos")
                            .description("Seeded category for demo data")
                            .active(Boolean.TRUE)
                            .build()));

            Location location = locationRepository.findByName("Bodega Demo")
                    .orElseGet(() -> locationRepository.save(Location.builder()
                            .name("Bodega Demo")
                            .description("Seeded location for demo data")
                            .active(Boolean.TRUE)
                            .build()));

            UnitOfMeasure unit = unitOfMeasureRepository.findBySymbol("ml")
                    .orElseGet(() -> unitOfMeasureRepository.save(UnitOfMeasure.builder()
                            .name("Mililitro")
                            .symbol("ml")
                            .type(UnitType.VOLUME)
                            .active(Boolean.TRUE)
                            .build()));

            Product alcohol = productRepository.findByCode("ALCO-001")
                    .orElseGet(() -> productRepository.save(Product.builder()
                            .code("ALCO-001")
                            .name("Alcohol etilico")
                            .description("Demo product with low stock and near expiration")
                            .category(category)
                            .baseUnit(unit)
                            .minimumStock(new BigDecimal("25"))
                            .currentStock(BigDecimal.ZERO)
                            .location(location)
                            .observations("Seeded for local demo")
                            .storageCondition("Dry place")
                            .requiresExpiration(Boolean.TRUE)
                            .requiresBatchControl(Boolean.TRUE)
                            .active(Boolean.TRUE)
                            .build()));

            Product acetone = productRepository.findByCode("ACET-001")
                    .orElseGet(() -> productRepository.save(Product.builder()
                            .code("ACET-001")
                            .name("Acetona")
                            .description("Demo product with healthy stock")
                            .category(category)
                            .baseUnit(unit)
                            .minimumStock(new BigDecimal("10"))
                            .currentStock(BigDecimal.ZERO)
                            .location(location)
                            .observations("Seeded for local demo")
                            .storageCondition("Dry place")
                            .requiresExpiration(Boolean.TRUE)
                            .requiresBatchControl(Boolean.TRUE)
                            .active(Boolean.TRUE)
                            .build()));

            if (inventoryMovementRepository.count() == 0L) {
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(
                                adminUser.getUsername(), "seed", List.of()));
                try {
                    inventoryMovementService.create(new InventoryMovementRequestDto(
                            MovementType.ENTRY,
                            laboratory.getId(),
                            "Initial inventory seeded for demo",
                            List.of(
                                    new InventoryMovementLineRequestDto(
                                            alcohol.getId(),
                                            null,
                                            "DEMO-ALCO-001",
                                            LocalDate.now().plusDays(10),
                                            new BigDecimal("20"),
                                            new BigDecimal("1.2500"),
                                            unit.getId(),
                                            "Seeded demo batch"),
                                    new InventoryMovementLineRequestDto(
                                            acetone.getId(),
                                            null,
                                            "DEMO-ACET-001",
                                            LocalDate.now().plusDays(120),
                                            new BigDecimal("100"),
                                            new BigDecimal("0.8500"),
                                            unit.getId(),
                                            "Seeded demo batch"))));
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }

            LOGGER.info("Demo seed ready. Username: {} | Laboratory: {}", adminUser.getUsername(), laboratory.getCode());
        };
    }
}
