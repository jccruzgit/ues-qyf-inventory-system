package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.CategoryRequestDto;
import sv.edu.ues.qyf.inventory.dto.CategoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.Category;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDto request) {
        if (request == null) {
            return null;
        }

        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(Category category, CategoryRequestDto request) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
    }

    public CategoryResponseDto toResponseDto(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .build();
    }
}
