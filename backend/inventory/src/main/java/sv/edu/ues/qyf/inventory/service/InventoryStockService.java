package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;

public interface InventoryStockService {

    List<InventoryStockResponseDto> getStock(Long productId, Long laboratoryId, Long productBatchId);
}
