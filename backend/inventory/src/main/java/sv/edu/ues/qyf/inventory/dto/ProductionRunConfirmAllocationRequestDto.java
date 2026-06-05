package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunConfirmAllocationRequestDto {

    @NotNull(message = "Product batch id is required")
    private Long productBatchId;

    @NotNull(message = "Allocation quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Allocation quantity must be greater than 0")
    private BigDecimal quantity;
}
