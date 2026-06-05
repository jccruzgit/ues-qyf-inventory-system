package sv.edu.ues.qyf.inventory.service;

import sv.edu.ues.qyf.inventory.dto.ProductionRunConfirmRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunPrintResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunResponseDto;

public interface ProductionRunService {

    ProductionRunResponseDto create(ProductionRunRequestDto request);

    ProductionRunResponseDto confirm(Long id);

    ProductionRunResponseDto confirm(Long id, ProductionRunConfirmRequestDto request);

    ProductionRunResponseDto getById(Long id);

    ProductionRunPrintResponseDto getPrintableById(Long id);
}
