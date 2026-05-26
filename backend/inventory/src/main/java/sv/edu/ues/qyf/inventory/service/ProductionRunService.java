package sv.edu.ues.qyf.inventory.service;

import sv.edu.ues.qyf.inventory.dto.ProductionRunRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunResponseDto;

public interface ProductionRunService {

    ProductionRunResponseDto create(ProductionRunRequestDto request);

    ProductionRunResponseDto confirm(Long id);

    ProductionRunResponseDto getById(Long id);
}
