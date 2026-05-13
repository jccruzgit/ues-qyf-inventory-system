package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotNull;
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

    @Size(max = 150, message = "Group name must not exceed 150 characters")
    private String groupName;

    private String notes;
}
