package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;

@Component
public class InventoryMovementMapper {

    public InventoryMovementResponseDto toResponseDto(InventoryMovement inventoryMovement) {
        if (inventoryMovement == null) {
            return null;
        }

        return InventoryMovementResponseDto.builder()
                .id(inventoryMovement.getId())
                .laboratoryId(inventoryMovement.getLaboratory() != null ? inventoryMovement.getLaboratory().getId() : null)
                .attachmentDocumentId(inventoryMovement.getAttachmentDocument() != null
                        ? inventoryMovement.getAttachmentDocument().getId()
                        : null)
                .build();
    }
}
