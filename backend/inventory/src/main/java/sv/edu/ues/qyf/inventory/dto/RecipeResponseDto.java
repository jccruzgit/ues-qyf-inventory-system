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
public class RecipeResponseDto {

    private Long id;
    private Long manufacturedProductId;
    private String manufacturedProductCode;
    private String manufacturedProductName;
    private String code;
    private String name;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long deletedById;
    private List<RecipeItemResponseDto> items;
}
