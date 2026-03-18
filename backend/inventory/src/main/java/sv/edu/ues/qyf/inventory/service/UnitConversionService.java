package sv.edu.ues.qyf.inventory.service;

import java.math.BigDecimal;
import java.util.List;
import sv.edu.ues.qyf.inventory.dto.UnitConversionRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitConversionResponseDto;

public interface UnitConversionService {

    UnitConversionResponseDto create(UnitConversionRequestDto request);

    List<UnitConversionResponseDto> getAll();

    UnitConversionResponseDto getById(Long id);

    UnitConversionResponseDto update(Long id, UnitConversionRequestDto request);

    UnitConversionResponseDto deactivate(Long id);

    BigDecimal convert(Long sourceUnitId, Long targetUnitId, BigDecimal quantity);
}
