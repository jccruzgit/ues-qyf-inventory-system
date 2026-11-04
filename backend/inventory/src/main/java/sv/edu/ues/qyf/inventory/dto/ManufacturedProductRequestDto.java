package sv.edu.ues.qyf.inventory.dto;

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
public class ManufacturedProductRequestDto {

    @NotBlank(message = "Manufactured product code is required")
    @Size(max = 50, message = "Manufactured product code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Manufactured product name is required")
    @Size(max = 150, message = "Manufactured product name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Manufactured product description must not exceed 500 characters")
    private String description;

    private Boolean active;
}
