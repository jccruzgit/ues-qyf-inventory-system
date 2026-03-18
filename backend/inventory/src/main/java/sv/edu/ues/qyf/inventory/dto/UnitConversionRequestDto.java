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
public class UnitConversionRequestDto {

    @NotNull(message = "Source unit id is required")
    private Long sourceUnitId;

    @NotNull(message = "Target unit id is required")
    private Long targetUnitId;

    @NotNull(message = "Conversion factor is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Conversion factor must be greater than 0")
    private BigDecimal conversionFactor;

    private Boolean active;
}
