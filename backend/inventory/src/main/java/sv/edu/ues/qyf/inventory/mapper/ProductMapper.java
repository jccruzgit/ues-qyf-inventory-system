package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.entity.Product;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .minimumStock(request.getMinimumStock())
                .currentStock(request.getCurrentStock())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(Product product, ProductRequestDto request) {
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setMinimumStock(request.getMinimumStock());
        product.setCurrentStock(request.getCurrentStock());
    }

    public ProductResponseDto toResponseDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .categoryName(product.getCategory().getName())
                .baseUnitName(product.getBaseUnit().getName())
                .baseUnitSymbol(product.getBaseUnit().getSymbol())
                .minimumStock(product.getMinimumStock())
                .currentStock(product.getCurrentStock())
                .locationName(product.getLocation().getName())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
