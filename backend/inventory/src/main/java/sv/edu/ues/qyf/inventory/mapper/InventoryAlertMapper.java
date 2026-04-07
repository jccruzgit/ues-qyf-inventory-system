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
                .acknowledgedById(inventoryAlert.getAcknowledgedBy() != null
                        ? inventoryAlert.getAcknowledgedBy().getId()
                        : null)
                .acknowledgedAt(inventoryAlert.getAcknowledgedAt())
                .build();
    }
}
