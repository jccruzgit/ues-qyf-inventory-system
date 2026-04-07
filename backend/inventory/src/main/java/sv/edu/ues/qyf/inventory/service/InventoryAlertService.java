package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;

public interface InventoryAlertService {

    List<InventoryAlertResponseDto> getPendingByLaboratory(Long laboratoryId);
}
