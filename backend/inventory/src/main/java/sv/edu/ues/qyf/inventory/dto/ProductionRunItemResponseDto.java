package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunItemResponseDto {

    private Long recipeItemId;
    private Long productId;
    private String productCode;
    private String productName;
    private String locationName;
    private Long unitOfMeasureId;
    private String unitOfMeasureName;
    private String unitOfMeasureSymbol;
    private BigDecimal requiredQuantity;
    private BigDecimal totalAvailableQuantity;
    private Boolean stockSufficient;
    private String observations;
    private List<ProductionRunAllocationResponseDto> suggestedAllocations;
}
