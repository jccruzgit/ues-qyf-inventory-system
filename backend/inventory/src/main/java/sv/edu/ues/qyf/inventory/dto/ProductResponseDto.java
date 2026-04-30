package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String categoryName;
    private Long baseUnitId;
    private String baseUnitName;
    private String baseUnitSymbol;
    private BigDecimal minimumStock;
    private BigDecimal currentStock;
    private String locationName;
    private String observations;
    private String storageCondition;
    private Boolean requiresExpiration;
    private Boolean requiresBatchControl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long deletedById;
}
