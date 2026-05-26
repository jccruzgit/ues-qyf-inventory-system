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
public class RecipeItemResponseDto {

    private Long id;
    private Integer itemOrder;
    private Long productId;
    private String productCode;
    private String productName;
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    private String unitOfMeasureSymbol;
    private BigDecimal quantity;
    private String observations;
    private String locationName;
}
