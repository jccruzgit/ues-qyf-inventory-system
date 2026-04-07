package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.ProductMapper;
import sv.edu.ues.qyf.inventory.repository.CategoryRepository;
import sv.edu.ues.qyf.inventory.repository.LocationRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.ProductService;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final String TABLE_NAME = "products";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final LocationRepository locationRepository;
    private final ProductMapper productMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            LocationRepository locationRepository,
            ProductMapper productMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.locationRepository = locationRepository;
        this.productMapper = productMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductResponseDto create(ProductRequestDto request) {
        String code = normalize(request.getCode());
        String name = normalize(request.getName());
        validateDuplicateCode(code, null);

        Product product = productMapper.toEntity(request);
        applyRelations(product, request);
        product.setCode(code);
        product.setName(name);
        product.setDescription(normalizeNullable(request.getDescription()));
        product.setActive(resolveActive(request.getActive(), Boolean.TRUE));

        Product savedProduct = productRepository.save(product);
        auditLogService.logAction(
                TABLE_NAME,
                savedProduct.getId(),
                ACTION_CREATE,
                null,
                null,
                serializeState(savedProduct),
                "Product created");

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAll() {
        return productRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getById(Long id) {
        return productMapper.toResponseDto(getActiveProduct(id));
    }

    @Override
    public ProductResponseDto update(Long id, ProductRequestDto request) {
        Product product = getActiveProduct(id);
        String oldValues = serializeState(product);
        String code = normalize(request.getCode());
        String name = normalize(request.getName());
        validateDuplicateCode(code, id);

        productMapper.updateEntity(product, request);
        applyRelations(product, request);
        product.setCode(code);
        product.setName(name);
        product.setDescription(normalizeNullable(request.getDescription()));
        product.setActive(resolveActive(request.getActive(), product.getActive()));

        Product savedProduct = productRepository.save(product);
        auditLogService.logAction(
                TABLE_NAME,
                savedProduct.getId(),
                ACTION_UPDATE,
                null,
                oldValues,
                serializeState(savedProduct),
                "Product updated");

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDto deactivate(Long id) {
        Product product = getActiveProduct(id);
        User currentUser = currentUserService.getAuthenticatedUser();
        String oldValues = serializeState(product);

        product.setActive(Boolean.FALSE);
        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(currentUser);

        Product savedProduct = productRepository.save(product);
        auditLogService.logSoftDelete(
                TABLE_NAME,
                savedProduct.getId(),
                null,
                oldValues,
                serializeState(savedProduct),
                "Product soft deleted");

        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDto restore(Long id) {
        Product product = getProduct(id);
        String oldValues = serializeState(product);

        product.setActive(Boolean.TRUE);
        product.setDeletedAt(null);
        product.setDeletedBy(null);

        Product savedProduct = productRepository.save(product);
        auditLogService.logRestore(
                TABLE_NAME,
                savedProduct.getId(),
                null,
                oldValues,
                serializeState(savedProduct),
                "Product restored");

        return productMapper.toResponseDto(savedProduct);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Product getActiveProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private void applyRelations(Product product, ProductRequestDto request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));
        UnitOfMeasure baseUnit = unitOfMeasureRepository.findById(request.getBaseUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unit of measure not found with id: " + request.getBaseUnitId()));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id: " + request.getLocationId()));

        product.setCategory(category);
        product.setBaseUnit(baseUnit);
        product.setLocation(location);
    }

    private void validateDuplicateCode(String code, Long currentId) {
        productRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Product code already exists: " + code);
                });
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

    private Boolean resolveActive(Boolean requestedActive, Boolean currentValue) {
        return requestedActive != null ? requestedActive : currentValue;
    }

    private String serializeState(Product product) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", product.getId());
        state.put("code", product.getCode());
        state.put("name", product.getName());
        state.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
        state.put("baseUnitId", product.getBaseUnit() != null ? product.getBaseUnit().getId() : null);
        state.put("locationId", product.getLocation() != null ? product.getLocation().getId() : null);
        state.put("minimumStock", product.getMinimumStock());
        state.put("currentStock", product.getCurrentStock());
        state.put("active", product.getActive());
        state.put("requiresExpiration", product.getRequiresExpiration());
        state.put("requiresBatchControl", product.getRequiresBatchControl());
        state.put("deletedAt", product.getDeletedAt());
        state.put("deletedById", product.getDeletedBy() != null ? product.getDeletedBy().getId() : null);
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize product audit state", exception);
        }
    }
}
