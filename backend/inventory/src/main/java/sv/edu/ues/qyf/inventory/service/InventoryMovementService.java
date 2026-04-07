package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;

public interface InventoryMovementService {

    List<InventoryMovementResponseDto> getByLaboratory(Long laboratoryId);
}
