package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;

public interface InventoryAlertService {

    List<InventoryAlertResponseDto> getPendingByLaboratory(Long laboratoryId);

    List<InventoryAlertResponseDto> getAlerts(Long laboratoryId, InventoryAlertType alertType, Boolean pendingOnly);

    void synchronizeAlerts(Long laboratoryId, List<Long> productIds, List<Long> productBatchIds);
}
