package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductResponseDto;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;

@Component
public class ManufacturedProductMapper {

    public ManufacturedProduct toEntity(ManufacturedProductRequestDto request) {
        if (request == null) {
            return null;
        }

        return ManufacturedProduct.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(ManufacturedProduct manufacturedProduct, ManufacturedProductRequestDto request) {
        manufacturedProduct.setCode(request.getCode());
        manufacturedProduct.setName(request.getName());
        manufacturedProduct.setDescription(request.getDescription());
        manufacturedProduct.setActive(request.getActive());
    }

    public ManufacturedProductResponseDto toResponseDto(ManufacturedProduct manufacturedProduct) {
        if (manufacturedProduct == null) {
            return null;
        }

        return ManufacturedProductResponseDto.builder()
                .id(manufacturedProduct.getId())
                .code(manufacturedProduct.getCode())
                .name(manufacturedProduct.getName())
                .description(manufacturedProduct.getDescription())
                .active(manufacturedProduct.getActive())
                .createdAt(manufacturedProduct.getCreatedAt())
                .updatedAt(manufacturedProduct.getUpdatedAt())
                .deletedAt(manufacturedProduct.getDeletedAt())
                .deletedById(manufacturedProduct.getDeletedBy() != null
                        ? manufacturedProduct.getDeletedBy().getId()
                        : null)
                .build();
    }
}
