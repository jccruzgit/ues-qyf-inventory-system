package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipePrintResponseDto {

    private Long recipeId;
    private String recipeCode;
    private String recipeName;
    private Long manufacturedProductId;
    private String manufacturedProductCode;
    private String manufacturedProductName;
    private String groupCode;
    private String cycle;
    private String lotNumber;
    private LocalDateTime generatedAt;
    private List<RecipePrintItemResponseDto> items;
}
