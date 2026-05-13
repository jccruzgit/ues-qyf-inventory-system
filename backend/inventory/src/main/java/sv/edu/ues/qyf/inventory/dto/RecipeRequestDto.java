package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotBlank;
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
public class RecipeRequestDto {

    @NotNull(message = "Manufactured product id is required")
    private Long manufacturedProductId;

    @NotBlank(message = "Recipe code is required")
    @Size(max = 50, message = "Recipe code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Recipe name is required")
    @Size(max = 150, message = "Recipe name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Recipe description must not exceed 500 characters")
    private String description;

    private Boolean active;
}
