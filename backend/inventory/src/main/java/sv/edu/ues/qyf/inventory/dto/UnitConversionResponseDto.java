package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.UnitType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitConversionResponseDto {

    private Long id;
    private Long sourceUnitId;
    private String sourceUnitName;
    private String sourceUnitSymbol;
    private Long targetUnitId;
    private String targetUnitName;
    private String targetUnitSymbol;
    private UnitType unitType;
    private BigDecimal conversionFactor;
    private Boolean active;
}
