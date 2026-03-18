package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureResponseDto;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;

@Component
public class UnitOfMeasureMapper {

    public UnitOfMeasure toEntity(UnitOfMeasureRequestDto request) {
        if (request == null) {
            return null;
        }

        return UnitOfMeasure.builder()
                .name(request.getName())
                .symbol(request.getSymbol())
                .type(request.getType())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(UnitOfMeasure unitOfMeasure, UnitOfMeasureRequestDto request) {
        unitOfMeasure.setName(request.getName());
        unitOfMeasure.setSymbol(request.getSymbol());
        unitOfMeasure.setType(request.getType());
    }

    public UnitOfMeasureResponseDto toResponseDto(UnitOfMeasure unitOfMeasure) {
        if (unitOfMeasure == null) {
            return null;
        }

        return UnitOfMeasureResponseDto.builder()
                .id(unitOfMeasure.getId())
                .name(unitOfMeasure.getName())
                .symbol(unitOfMeasure.getSymbol())
                .type(unitOfMeasure.getType())
                .active(unitOfMeasure.getActive())
                .build();
    }
}
