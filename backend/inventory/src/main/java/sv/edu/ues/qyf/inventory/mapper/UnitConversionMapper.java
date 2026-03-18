package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.UnitConversionRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitConversionResponseDto;
import sv.edu.ues.qyf.inventory.entity.UnitConversion;

@Component
public class UnitConversionMapper {

    public UnitConversion toEntity(UnitConversionRequestDto request) {
        if (request == null) {
            return null;
        }

        return UnitConversion.builder()
                .conversionFactor(request.getConversionFactor())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(UnitConversion unitConversion, UnitConversionRequestDto request) {
        unitConversion.setConversionFactor(request.getConversionFactor());
    }

    public UnitConversionResponseDto toResponseDto(UnitConversion unitConversion) {
        if (unitConversion == null) {
            return null;
        }

        return UnitConversionResponseDto.builder()
                .id(unitConversion.getId())
                .sourceUnitId(unitConversion.getSourceUnit().getId())
                .sourceUnitName(unitConversion.getSourceUnit().getName())
                .sourceUnitSymbol(unitConversion.getSourceUnit().getSymbol())
                .targetUnitId(unitConversion.getTargetUnit().getId())
                .targetUnitName(unitConversion.getTargetUnit().getName())
                .targetUnitSymbol(unitConversion.getTargetUnit().getSymbol())
                .unitType(unitConversion.getSourceUnit().getType())
                .conversionFactor(unitConversion.getConversionFactor())
                .active(unitConversion.getActive())
                .build();
    }
}
