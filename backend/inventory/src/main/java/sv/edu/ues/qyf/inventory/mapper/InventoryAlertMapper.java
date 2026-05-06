package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;

@Component
public class InventoryAlertMapper {

    public InventoryAlertResponseDto toResponseDto(InventoryAlert inventoryAlert) {
        if (inventoryAlert == null) {
            return null;
        }

        return InventoryAlertResponseDto.builder()
                .id(inventoryAlert.getId())
                .laboratoryId(inventoryAlert.getLaboratory() != null ? inventoryAlert.getLaboratory().getId() : null)
                .laboratoryCode(inventoryAlert.getLaboratory() != null ? inventoryAlert.getLaboratory().getCode() : null)
                .laboratoryName(inventoryAlert.getLaboratory() != null ? inventoryAlert.getLaboratory().getName() : null)
                .alertType(inventoryAlert.getAlertType())
                .productId(inventoryAlert.getProduct() != null ? inventoryAlert.getProduct().getId() : null)
                .productCode(inventoryAlert.getProduct() != null ? inventoryAlert.getProduct().getCode() : null)
                .productName(inventoryAlert.getProduct() != null ? inventoryAlert.getProduct().getName() : null)
                .productBatchId(inventoryAlert.getProductBatch() != null ? inventoryAlert.getProductBatch().getId() : null)
                .batchCode(inventoryAlert.getProductBatch() != null
                        ? inventoryAlert.getProductBatch().getBatchCode()
                        : null)
                .locationName(inventoryAlert.getProduct() != null && inventoryAlert.getProduct().getLocation() != null
                        ? inventoryAlert.getProduct().getLocation().getName()
                        : null)
                .minimumStock(inventoryAlert.getProduct() != null ? inventoryAlert.getProduct().getMinimumStock() : null)
                .expirationDate(inventoryAlert.getProductBatch() != null
                        ? inventoryAlert.getProductBatch().getExpirationDate()
                        : null)
                .message(inventoryAlert.getMessage())
                .triggeredAt(inventoryAlert.getTriggeredAt())
                .acknowledgedById(inventoryAlert.getAcknowledgedBy() != null
                        ? inventoryAlert.getAcknowledgedBy().getId()
                        : null)
                .acknowledgedAt(inventoryAlert.getAcknowledgedAt())
                .build();
    }
}
