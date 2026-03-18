package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.CategoryRequestDto;
import sv.edu.ues.qyf.inventory.dto.CategoryResponseDto;

public interface CategoryService {

    CategoryResponseDto create(CategoryRequestDto request);

    List<CategoryResponseDto> getAll();

    CategoryResponseDto getById(Long id);

    CategoryResponseDto update(Long id, CategoryRequestDto request);

    CategoryResponseDto deactivate(Long id);
}
