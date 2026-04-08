package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.ues.qyf.inventory.entity.MovementType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementRequestDto {

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Laboratory id is required")
    private Long laboratoryId;

    private String observation;

    @Valid
    @NotEmpty(message = "At least one movement line is required")
    private List<InventoryMovementLineRequestDto> lines;
}
