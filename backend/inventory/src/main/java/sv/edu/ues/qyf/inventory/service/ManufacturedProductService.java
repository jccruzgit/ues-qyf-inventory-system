package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductResponseDto;

public interface ManufacturedProductService {

    ManufacturedProductResponseDto create(ManufacturedProductRequestDto request);

    List<ManufacturedProductResponseDto> getAll();

    ManufacturedProductResponseDto getById(Long id);

    ManufacturedProductResponseDto update(Long id, ManufacturedProductRequestDto request);
}
