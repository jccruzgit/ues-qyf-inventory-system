package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureResponseDto;

public interface UnitOfMeasureService {

    UnitOfMeasureResponseDto create(UnitOfMeasureRequestDto request);

    List<UnitOfMeasureResponseDto> getAll();

    UnitOfMeasureResponseDto getById(Long id);

    UnitOfMeasureResponseDto update(Long id, UnitOfMeasureRequestDto request);

    UnitOfMeasureResponseDto deactivate(Long id);
}
