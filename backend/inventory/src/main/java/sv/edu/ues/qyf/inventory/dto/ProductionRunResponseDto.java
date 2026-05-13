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
public class ProductionRunResponseDto {

    private Long id;
    private ProductionRunStatus status;
    private Long recipeId;
    private String recipeCode;
    private String recipeName;
    private Long manufacturedProductId;
    private String manufacturedProductCode;
    private String manufacturedProductName;
    private Long laboratoryId;
    private String laboratoryCode;
    private String laboratoryName;
    private Long createdById;
    private String createdByUsername;
    private Long confirmedById;
    private String confirmedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private String groupName;
    private String notes;
    private Long inventoryMovementId;
    private Boolean readyToConfirm;
    private List<ProductionRunItemResponseDto> items;
}
