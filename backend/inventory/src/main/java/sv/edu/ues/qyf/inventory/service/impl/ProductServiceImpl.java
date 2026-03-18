package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.ProductMapper;
import sv.edu.ues.qyf.inventory.repository.CategoryRepository;
import sv.edu.ues.qyf.inventory.repository.LocationRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.ProductService;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final LocationRepository locationRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            LocationRepository locationRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.locationRepository = locationRepository;
        this.productMapper = productMapper;
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

        return productMapper.toResponseDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getById(Long id) {
        return productMapper.toResponseDto(getProduct(id));
    }

    @Override
    public ProductResponseDto update(Long id, ProductRequestDto request) {
        Product product = getProduct(id);
        String code = normalize(request.getCode());
        String name = normalize(request.getName());
        validateDuplicateCode(code, id);

        productMapper.updateEntity(product, request);
        applyRelations(product, request);
        product.setCode(code);
        product.setName(name);
        product.setDescription(normalizeNullable(request.getDescription()));
        product.setActive(resolveActive(request.getActive(), product.getActive()));

        return productMapper.toResponseDto(productRepository.save(product));
    }

    @Override
    public ProductResponseDto deactivate(Long id) {
        Product product = getProduct(id);
        product.setActive(Boolean.FALSE);
        return productMapper.toResponseDto(productRepository.save(product));
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
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
}
