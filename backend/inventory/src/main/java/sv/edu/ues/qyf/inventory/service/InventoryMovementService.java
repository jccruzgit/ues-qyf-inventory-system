package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementFilterDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;

public interface InventoryMovementService {

    InventoryMovementResponseDto create(InventoryMovementRequestDto request);

    List<InventoryMovementResponseDto> getAll();

    List<InventoryMovementResponseDto> search(InventoryMovementFilterDto filter);

    InventoryMovementResponseDto getById(Long id);

    List<InventoryMovementResponseDto> getByLaboratory(Long laboratoryId);
}
