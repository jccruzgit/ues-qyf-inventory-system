package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.ues.qyf.inventory.entity.UnitType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureRequestDto {

    @NotBlank(message = "Unit name is required")
    @Size(max = 100, message = "Unit name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Unit symbol is required")
    @Size(max = 20, message = "Unit symbol must not exceed 20 characters")
    private String symbol;

    @NotNull(message = "Unit type is required")
    private UnitType type;

    private Boolean active;
}
