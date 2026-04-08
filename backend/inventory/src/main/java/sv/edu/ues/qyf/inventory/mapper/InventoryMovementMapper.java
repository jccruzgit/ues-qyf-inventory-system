package sv.edu.ues.qyf.inventory.mapper;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineResponseDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;

@Component
public class InventoryMovementMapper {

    public InventoryMovementResponseDto toResponseDto(InventoryMovement inventoryMovement) {
        if (inventoryMovement == null) {
            return null;
        }

        return InventoryMovementResponseDto.builder()
                .id(inventoryMovement.getId())
                .movementType(inventoryMovement.getMovementType())
                .laboratoryId(inventoryMovement.getLaboratory() != null ? inventoryMovement.getLaboratory().getId() : null)
                .performedById(inventoryMovement.getPerformedBy() != null ? inventoryMovement.getPerformedBy().getId() : null)
                .performedByUsername(inventoryMovement.getPerformedBy() != null
                        ? inventoryMovement.getPerformedBy().getUsername()
                        : null)
                .performedAt(inventoryMovement.getPerformedAt())
                .observation(inventoryMovement.getObservation())
                .attachmentDocumentId(inventoryMovement.getAttachmentDocument() != null
                        ? inventoryMovement.getAttachmentDocument().getId()
                        : null)
                .lines(mapLines(inventoryMovement.getLines()))
                .build();
    }

    private List<InventoryMovementLineResponseDto> mapLines(List<InventoryMovementLine> lines) {
        if (lines == null) {
            return List.of();
        }

        return lines.stream()
                .sorted(Comparator.comparing(InventoryMovementLine::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toLineResponseDto)
                .toList();
    }

    private InventoryMovementLineResponseDto toLineResponseDto(InventoryMovementLine line) {
        return InventoryMovementLineResponseDto.builder()
                .id(line.getId())
                .productId(line.getProduct() != null ? line.getProduct().getId() : null)
                .productCode(line.getProduct() != null ? line.getProduct().getCode() : null)
                .productName(line.getProduct() != null ? line.getProduct().getName() : null)
                .quantity(line.getQuantity())
                .lineNotes(line.getLineNotes())
                .build();
    }
}
