package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.ProductionRunStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunPrintResponseDto {

    private Long productionRunId;
    private ProductionRunStatus status;
    private String controlMark;
    private Long recipeId;
    private String recipeCode;
    private String recipeName;
    private Long manufacturedProductId;
    private String manufacturedProductCode;
    private String manufacturedProductName;
    private String groupName;
    private String cycle;
    private String lotNumber;
    private Long laboratoryId;
    private String laboratoryCode;
    private String laboratoryName;
    private LocalDateTime laboratoryDate;
    private LocalDateTime generatedAt;
    private String preparedByUsername;
    private String confirmedByUsername;
    private String notes;
    private List<ProductionRunPrintItemResponseDto> items;
}
