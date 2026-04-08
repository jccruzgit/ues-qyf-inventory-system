package sv.edu.ues.qyf.inventory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductUpdateRequestDto {

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Product description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotNull(message = "Base unit id is required")
    private Long baseUnitId;

    @NotNull(message = "Minimum stock is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum stock must be greater than or equal to 0")
    private BigDecimal minimumStock;

    @NotNull(message = "Location id is required")
    private Long locationId;

    private String observations;

    @Size(max = 120, message = "Storage condition must not exceed 120 characters")
    private String storageCondition;

    private Boolean requiresExpiration;

    private Boolean requiresBatchControl;

    private Boolean active;
}
