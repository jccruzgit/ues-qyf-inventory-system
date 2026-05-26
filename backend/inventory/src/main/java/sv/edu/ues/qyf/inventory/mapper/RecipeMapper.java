package sv.edu.ues.qyf.inventory.mapper;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.RecipeItemResponseDto;
import sv.edu.ues.qyf.inventory.dto.RecipeResponseDto;
import sv.edu.ues.qyf.inventory.entity.Recipe;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;

@Component
public class RecipeMapper {

    public RecipeResponseDto toResponseDto(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        return RecipeResponseDto.builder()
                .id(recipe.getId())
                .manufacturedProductId(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getId()
                        : null)
                .manufacturedProductCode(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getCode()
                        : null)
                .manufacturedProductName(recipe.getManufacturedProduct() != null
                        ? recipe.getManufacturedProduct().getName()
                        : null)
                .code(recipe.getCode())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .active(recipe.getActive())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .deletedAt(recipe.getDeletedAt())
                .deletedById(recipe.getDeletedBy() != null ? recipe.getDeletedBy().getId() : null)
                .items(mapItems(recipe.getItems()))
                .build();
    }

    private List<RecipeItemResponseDto> mapItems(List<RecipeItem> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .sorted(Comparator.comparing(RecipeItem::getItemOrder).thenComparing(RecipeItem::getId))
                .map(this::toItemResponseDto)
                .toList();
    }

    private RecipeItemResponseDto toItemResponseDto(RecipeItem item) {
        return RecipeItemResponseDto.builder()
                .id(item.getId())
                .itemOrder(item.getItemOrder())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productCode(item.getProduct() != null ? item.getProduct().getCode() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .unitOfMeasureId(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getId() : null)
                .unitOfMeasureName(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getName() : null)
                .unitOfMeasureSymbol(item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getSymbol() : null)
                .quantity(item.getQuantity())
                .observations(item.getObservations())
                .locationName(item.getProduct() != null && item.getProduct().getLocation() != null
                        ? item.getProduct().getLocation().getName()
                        : null)
                .build();
    }
}
