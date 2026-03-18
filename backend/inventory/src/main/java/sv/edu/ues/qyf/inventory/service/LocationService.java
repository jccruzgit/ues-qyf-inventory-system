package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.LocationRequestDto;
import sv.edu.ues.qyf.inventory.dto.LocationResponseDto;

public interface LocationService {

    LocationResponseDto create(LocationRequestDto request);

    List<LocationResponseDto> getAll();

    LocationResponseDto getById(Long id);

    LocationResponseDto update(Long id, LocationRequestDto request);

    LocationResponseDto deactivate(Long id);
}
