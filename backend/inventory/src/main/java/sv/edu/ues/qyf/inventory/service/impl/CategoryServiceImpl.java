package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.CategoryRequestDto;
import sv.edu.ues.qyf.inventory.dto.CategoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.Category;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.CategoryMapper;
import sv.edu.ues.qyf.inventory.repository.CategoryRepository;
import sv.edu.ues.qyf.inventory.service.CategoryService;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponseDto create(CategoryRequestDto request) {
        String name = normalize(request.getName());
        validateDuplicateName(name, null);

        Category category = categoryMapper.toEntity(request);
        category.setName(name);
        category.setDescription(normalizeNullable(request.getDescription()));
        category.setActive(resolveActive(request.getActive(), Boolean.TRUE));

        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getById(Long id) {
        return categoryMapper.toResponseDto(getCategory(id));
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        Category category = getCategory(id);
        String name = normalize(request.getName());
        validateDuplicateName(name, id);

        categoryMapper.updateEntity(category, request);
        category.setName(name);
        category.setDescription(normalizeNullable(request.getDescription()));
        category.setActive(resolveActive(request.getActive(), category.getActive()));

        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDto deactivate(Long id) {
        Category category = getCategory(id);
        category.setActive(Boolean.FALSE);
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private void validateDuplicateName(String name, Long currentId) {
        categoryRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Category name already exists: " + name);
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
