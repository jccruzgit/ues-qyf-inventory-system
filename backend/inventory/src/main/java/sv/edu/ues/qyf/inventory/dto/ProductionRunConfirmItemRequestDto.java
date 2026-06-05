package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunConfirmItemRequestDto {

    @NotNull(message = "Recipe item id is required")
    private Long recipeItemId;

    @NotNull(message = "Actual quantity is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Actual quantity must be greater than or equal to 0")
    private BigDecimal actualQuantity;

    @Valid
    private List<ProductionRunConfirmAllocationRequestDto> allocations;
}
