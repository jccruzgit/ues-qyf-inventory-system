package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductUpdateRequestDto;
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.UnitType;
import sv.edu.ues.qyf.inventory.mapper.ProductMapper;
import sv.edu.ues.qyf.inventory.repository.CategoryRepository;
import sv.edu.ues.qyf.inventory.repository.LocationRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    private ProductMapper productMapper;

    private ObjectMapper objectMapper;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
        objectMapper = new ObjectMapper();
        productService = new ProductServiceImpl(
                productRepository,
                categoryRepository,
                unitOfMeasureRepository,
                locationRepository,
                productMapper,
                currentUserService,
                auditLogService,
                objectMapper);
    }

    @Test
    void create_setsInitialStockToOneHundredAndWritesAuditLog() {
        ProductRequestDto request = buildRequest(" PRD-100 ", " Acetona ", new BigDecimal("100"));
        Category category = Category.builder().id(1L).name("Reactivos").active(Boolean.TRUE).build();
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(2L)
                .name("Unit")
                .symbol("unit")
                .type(UnitType.COUNT)
                .active(Boolean.TRUE)
                .build();
        Location location = Location.builder().id(3L).name("Bodega").active(Boolean.TRUE).build();

        when(productRepository.findByCode("PRD-100")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
        when(locationRepository.findById(3L)).thenReturn(Optional.of(location));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(10L);
            product.setCreatedAt(LocalDateTime.of(2026, 4, 7, 10, 0));
            product.setUpdatedAt(LocalDateTime.of(2026, 4, 7, 10, 0));
            return product;
        });

        ProductResponseDto response = productService.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCode()).isEqualTo("PRD-100");
        assertThat(response.getName()).isEqualTo("Acetona");
        assertThat(response.getCurrentStock()).isEqualByComparingTo("100");
        assertThat(response.getMinimumStock()).isEqualByComparingTo("10");
        verify(productRepository).save(argThat(product ->
                product.getCurrentStock().compareTo(new BigDecimal("100")) == 0
                        && product.getCategory().getId().equals(1L)
                        && product.getBaseUnit().getId().equals(2L)
                        && product.getLocation().getId().equals(3L)));
        verify(auditLogService)
                .logAction(
                        eq("products"),
                        eq(10L),
                        eq("CREATE"),
                        isNull(),
                        isNull(),
                        argThat(payload -> payload.contains("\"currentStock\":100")),
                        eq("Product created"));
    }

    @Test
    void create_defaultsCurrentStockToZeroWhenNotProvided() {
        ProductRequestDto request = buildRequest(" PRD-101 ", " Etanol ", null);
        Category category = Category.builder().id(1L).name("Reactivos").active(Boolean.TRUE).build();
        UnitOfMeasure unit = UnitOfMeasure.builder()
                .id(2L)
                .name("Unit")
                .symbol("unit")
                .type(UnitType.COUNT)
                .active(Boolean.TRUE)
                .build();
        Location location = Location.builder().id(3L).name("Bodega").active(Boolean.TRUE).build();

        when(productRepository.findByCode("PRD-101")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(unit));
        when(locationRepository.findById(3L)).thenReturn(Optional.of(location));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(11L);
            product.setCreatedAt(LocalDateTime.of(2026, 4, 7, 10, 30));
            product.setUpdatedAt(LocalDateTime.of(2026, 4, 7, 10, 30));
            return product;
        });

        ProductResponseDto response = productService.create(request);

        assertThat(response.getCurrentStock()).isEqualByComparingTo("0");
        verify(productRepository).save(argThat(product ->
                product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    void update_preservesPersistedStockWhenRequestSuppliesDifferentCurrentStock() {
        Product existingProduct = Product.builder()
                .id(10L)
                .code("PRD-100")
                .name("Acetona")
                .description("Reactivo de prueba")
                .category(Category.builder().id(1L).name("Reactivos").active(Boolean.TRUE).build())
                .baseUnit(UnitOfMeasure.builder()
                        .id(2L)
                        .name("Unit")
                        .symbol("unit")
                        .type(UnitType.COUNT)
                        .active(Boolean.TRUE)
                        .build())
                .minimumStock(new BigDecimal("10"))
                .currentStock(new BigDecimal("100"))
                .location(Location.builder().id(3L).name("Bodega").active(Boolean.TRUE).build())
                .active(Boolean.TRUE)
                .requiresExpiration(Boolean.FALSE)
                .requiresBatchControl(Boolean.TRUE)
                .createdAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 7, 10, 0))
                .build();
        ProductUpdateRequestDto request = buildUpdateRequest("PRD-100", "Acetona");

        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findByCode("PRD-100")).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingProduct.getCategory()));
        when(unitOfMeasureRepository.findById(2L)).thenReturn(Optional.of(existingProduct.getBaseUnit()));
        when(locationRepository.findById(3L)).thenReturn(Optional.of(existingProduct.getLocation()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setUpdatedAt(LocalDateTime.of(2026, 4, 7, 11, 0));
            return product;
        });

        ProductResponseDto response = productService.update(10L, request);

        assertThat(response.getCurrentStock()).isEqualByComparingTo("100");
        verify(productRepository).save(argThat(product ->
                product.getId().equals(10L)
                        && product.getCurrentStock().compareTo(new BigDecimal("100")) == 0));
        verify(auditLogService)
                .logAction(
                        eq("products"),
                        eq(10L),
                        eq("UPDATE"),
                        isNull(),
                        argThat(payload -> payload.contains("\"currentStock\":100")),
                        argThat(payload -> payload.contains("\"currentStock\":100")),
                        eq("Product updated"));
    }

    private ProductRequestDto buildRequest(String code, String name, BigDecimal currentStock) {
        return new ProductRequestDto(
                code,
                name,
                "Reactivo de prueba",
                1L,
                2L,
                new BigDecimal("10"),
                currentStock,
                3L,
                "Uso academico",
                "Seco",
                Boolean.FALSE,
                Boolean.TRUE,
                Boolean.TRUE);
    }

    private ProductUpdateRequestDto buildUpdateRequest(String code, String name) {
        return new ProductUpdateRequestDto(
                code,
                name,
                "Reactivo de prueba",
                1L,
                2L,
                new BigDecimal("10"),
                3L,
                "Uso academico",
                "Seco",
                Boolean.FALSE,
                Boolean.TRUE,
                Boolean.TRUE);
    }
}
