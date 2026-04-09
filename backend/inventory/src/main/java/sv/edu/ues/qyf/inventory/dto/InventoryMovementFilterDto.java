package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.ues.qyf.inventory.entity.MovementType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementFilterDto {

    private Long productId;
    private Long laboratoryId;
    private MovementType movementType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
