package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Manufactured product group code is required")
    @Size(max = 50, message = "Manufactured product group code must not exceed 50 characters")
    private String groupCode;

    @NotBlank(message = "Manufactured product cycle is required")
    @Size(max = 50, message = "Manufactured product cycle must not exceed 50 characters")
    private String cycle;

    @NotBlank(message = "Manufactured product lot number is required")
    @Size(max = 50, message = "Manufactured product lot number must not exceed 50 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9._/-]*$",
            message = "Manufactured product lot number format is invalid")
    private String lotNumber;

    private Boolean active;
}
