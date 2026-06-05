package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipePrintItemResponseDto {

    private Integer itemOrder;
    private Long productId;
    private String productCode;
    private String productName;
    private String unitOfMeasureName;
    private String unitOfMeasureSymbol;
    private BigDecimal theoreticalQuantity;
    private String observations;
}
