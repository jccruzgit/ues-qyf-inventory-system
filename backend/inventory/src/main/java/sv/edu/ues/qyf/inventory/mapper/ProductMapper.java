package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductUpdateRequestDto;
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
                .observations(request.getObservations())
                .storageCondition(request.getStorageCondition())
                .requiresExpiration(request.getRequiresExpiration())
                .requiresBatchControl(request.getRequiresBatchControl())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(Product product, ProductUpdateRequestDto request) {
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setMinimumStock(request.getMinimumStock());
        product.setObservations(request.getObservations());
        product.setStorageCondition(request.getStorageCondition());
        product.setRequiresExpiration(request.getRequiresExpiration());
        product.setRequiresBatchControl(request.getRequiresBatchControl());
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
                .observations(product.getObservations())
                .storageCondition(product.getStorageCondition())
                .requiresExpiration(product.getRequiresExpiration())
                .requiresBatchControl(product.getRequiresBatchControl())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .deletedAt(product.getDeletedAt())
                .deletedById(product.getDeletedBy() != null ? product.getDeletedBy().getId() : null)
                .build();
    }
}
