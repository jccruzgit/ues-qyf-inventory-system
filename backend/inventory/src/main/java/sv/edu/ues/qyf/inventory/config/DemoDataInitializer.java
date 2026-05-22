package sv.edu.ues.qyf.inventory.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.Recipe;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.CategoryRepository;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.LocationRepository;
import sv.edu.ues.qyf.inventory.repository.ManufacturedProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeItemRepository;
import sv.edu.ues.qyf.inventory.repository.RecipeRepository;
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
            ProductBatchRepository productBatchRepository,
            ManufacturedProductRepository manufacturedProductRepository,
            RecipeRepository recipeRepository,
            RecipeItemRepository recipeItemRepository,
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

            Product distilledWater = productRepository.findByCode("AGUA-DEST-001")
                    .orElseGet(() -> productRepository.save(Product.builder()
                            .code("AGUA-DEST-001")
                            .name("Agua destilada")
                            .description("Demo input for recipe-based discharge example")
                            .category(category)
                            .baseUnit(unit)
                            .minimumStock(new BigDecimal("500"))
                            .currentStock(BigDecimal.ZERO)
                            .location(location)
                            .observations("Seeded for recipe-based discharge demo")
                            .storageCondition("Dry place")
                            .requiresExpiration(Boolean.TRUE)
                            .requiresBatchControl(Boolean.TRUE)
                            .active(Boolean.TRUE)
                            .build()));

            Product glycerin = productRepository.findByCode("GLIC-001")
                    .orElseGet(() -> productRepository.save(Product.builder()
                            .code("GLIC-001")
                            .name("Glicerina liquida")
                            .description("Demo input for recipe-based discharge example")
                            .category(category)
                            .baseUnit(unit)
                            .minimumStock(new BigDecimal("100"))
                            .currentStock(BigDecimal.ZERO)
                            .location(location)
                            .observations("Seeded for recipe-based discharge demo")
                            .storageCondition("Dry place")
                            .requiresExpiration(Boolean.TRUE)
                            .requiresBatchControl(Boolean.TRUE)
                            .active(Boolean.TRUE)
                            .build()));

            Product citrusEssence = productRepository.findByCode("ESEN-CIT-001")
                    .orElseGet(() -> productRepository.save(Product.builder()
                            .code("ESEN-CIT-001")
                            .name("Esencia citrica")
                            .description("Demo input for recipe-based discharge example")
                            .category(category)
                            .baseUnit(unit)
                            .minimumStock(new BigDecimal("25"))
                            .currentStock(BigDecimal.ZERO)
                            .location(location)
                            .observations("Seeded for recipe-based discharge demo")
                            .storageCondition("Dry place")
                            .requiresExpiration(Boolean.TRUE)
                            .requiresBatchControl(Boolean.TRUE)
                            .active(Boolean.TRUE)
                            .build()));

            ManufacturedProduct liquidSoap = manufacturedProductRepository.findByCode("ELAB-JAB-001")
                    .orElseGet(() -> manufacturedProductRepository.save(ManufacturedProduct.builder()
                            .code("ELAB-JAB-001")
                            .name("Jabon liquido citrico")
                            .description("Manufactured product seeded for recipe-based discharge demo")
                            .active(Boolean.TRUE)
                            .build()));

            Recipe liquidSoapRecipe = recipeRepository.findByCode("REC-JAB-001")
                    .orElseGet(() -> recipeRepository.save(Recipe.builder()
                            .manufacturedProduct(liquidSoap)
                            .code("REC-JAB-001")
                            .name("Formula base de jabon liquido citrico")
                            .description("Seeded recipe example for recipe-based discharge flow")
                            .active(Boolean.TRUE)
                            .build()));

            if (!recipeItemRepository.existsByRecipeIdAndProductId(liquidSoapRecipe.getId(), distilledWater.getId())) {
                recipeItemRepository.save(RecipeItem.builder()
                        .recipe(liquidSoapRecipe)
                        .product(distilledWater)
                        .unitOfMeasure(unit)
                        .quantity(new BigDecimal("500"))
                        .itemOrder(1)
                        .observations("Base liquida principal para una elaboracion de prueba")
                        .build());
            }

            if (!recipeItemRepository.existsByRecipeIdAndProductId(liquidSoapRecipe.getId(), glycerin.getId())) {
                recipeItemRepository.save(RecipeItem.builder()
                        .recipe(liquidSoapRecipe)
                        .product(glycerin)
                        .unitOfMeasure(unit)
                        .quantity(new BigDecimal("120"))
                        .itemOrder(2)
                        .observations("Aporta viscosidad y estabilidad a la mezcla")
                        .build());
            }

            if (!recipeItemRepository.existsByRecipeIdAndProductId(liquidSoapRecipe.getId(), citrusEssence.getId())) {
                recipeItemRepository.save(RecipeItem.builder()
                        .recipe(liquidSoapRecipe)
                        .product(citrusEssence)
                        .unitOfMeasure(unit)
                        .quantity(new BigDecimal("20"))
                        .itemOrder(3)
                        .observations("Aroma del ejemplo de producto elaborado")
                        .build());
            }

            List<InventoryMovementLineRequestDto> demoEntryLines = new ArrayList<>();

            if (productBatchRepository.findByProductIdAndLaboratoryIdAndBatchCode(
                    alcohol.getId(), laboratory.getId(), "DEMO-ALCO-001").isEmpty()) {
                demoEntryLines.add(new InventoryMovementLineRequestDto(
                        alcohol.getId(),
                        null,
                        "DEMO-ALCO-001",
                        LocalDate.now().plusDays(10),
                        new BigDecimal("20"),
                        new BigDecimal("1.2500"),
                        unit.getId(),
                        "Seeded demo batch"));
            }

            if (productBatchRepository.findByProductIdAndLaboratoryIdAndBatchCode(
                    acetone.getId(), laboratory.getId(), "DEMO-ACET-001").isEmpty()) {
                demoEntryLines.add(new InventoryMovementLineRequestDto(
                        acetone.getId(),
                        null,
                        "DEMO-ACET-001",
                        LocalDate.now().plusDays(120),
                        new BigDecimal("100"),
                        new BigDecimal("0.8500"),
                        unit.getId(),
                        "Seeded demo batch"));
            }

            if (productBatchRepository.findByProductIdAndLaboratoryIdAndBatchCode(
                    distilledWater.getId(), laboratory.getId(), "DEMO-AGUA-001").isEmpty()) {
                demoEntryLines.add(new InventoryMovementLineRequestDto(
                        distilledWater.getId(),
                        null,
                        "DEMO-AGUA-001",
                        LocalDate.now().plusDays(180),
                        new BigDecimal("2000"),
                        new BigDecimal("0.0150"),
                        unit.getId(),
                        "Seeded batch for recipe-based discharge example"));
            }

            if (productBatchRepository.findByProductIdAndLaboratoryIdAndBatchCode(
                    glycerin.getId(), laboratory.getId(), "DEMO-GLIC-001").isEmpty()) {
                demoEntryLines.add(new InventoryMovementLineRequestDto(
                        glycerin.getId(),
                        null,
                        "DEMO-GLIC-001",
                        LocalDate.now().plusDays(150),
                        new BigDecimal("600"),
                        new BigDecimal("0.0450"),
                        unit.getId(),
                        "Seeded batch for recipe-based discharge example"));
            }

            if (productBatchRepository.findByProductIdAndLaboratoryIdAndBatchCode(
                    citrusEssence.getId(), laboratory.getId(), "DEMO-ESEN-001").isEmpty()) {
                demoEntryLines.add(new InventoryMovementLineRequestDto(
                        citrusEssence.getId(),
                        null,
                        "DEMO-ESEN-001",
                        LocalDate.now().plusDays(90),
                        new BigDecimal("200"),
                        new BigDecimal("0.1200"),
                        unit.getId(),
                        "Seeded batch for recipe-based discharge example"));
            }

            if (!demoEntryLines.isEmpty()) {
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(
                                adminUser.getUsername(), "seed", List.of()));
                try {
                    inventoryMovementService.create(new InventoryMovementRequestDto(
                            MovementType.ENTRY,
                            laboratory.getId(),
                            inventoryMovementRepository.count() == 0L
                                    ? "Initial inventory seeded for demo and recipe-based discharge example"
                                    : "Additional inventory seeded for recipe-based discharge example",
                            demoEntryLines));
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }

            LOGGER.info(
                    "Demo seed ready. Username: {} | Laboratory: {} | Manufactured product: {} | Recipe: {}",
                    adminUser.getUsername(),
                    laboratory.getCode(),
                    liquidSoap.getCode(),
                    liquidSoapRecipe.getCode());
        };
    }
}
