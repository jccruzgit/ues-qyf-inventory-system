package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.CorrectionType;
import sv.edu.ues.qyf.inventory.entity.MovementType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponseDto {

    private Long id;
    private MovementType movementType;
    private Long laboratoryId;
    private Long performedById;
    private String performedByUsername;
    private LocalDateTime performedAt;
    private CorrectionType correctionType;
    private Long relatedMovementId;
    private String correctionReason;
    private String observation;
    private Long attachmentDocumentId;
    private Long productionRunId;
    private Long recipeId;
    private String recipeName;
    private Long manufacturedProductId;
    private String manufacturedProductName;
    private List<InventoryMovementLineResponseDto> lines;
}
