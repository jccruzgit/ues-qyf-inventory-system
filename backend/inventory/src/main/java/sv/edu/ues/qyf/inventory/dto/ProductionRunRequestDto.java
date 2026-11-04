package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunRequestDto {

    @NotNull(message = "Recipe id is required")
    private Long recipeId;

    @NotNull(message = "Laboratory id is required")
    private Long laboratoryId;

    @NotBlank(message = "Production lot number is required")
    @Size(max = 50, message = "Production lot number must not exceed 50 characters")
    private String lotNumber;

    private String notes;
}
