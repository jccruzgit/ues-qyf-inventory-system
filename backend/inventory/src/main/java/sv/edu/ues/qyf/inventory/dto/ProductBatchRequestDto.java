package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchRequestDto {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Laboratory id is required")
    private Long laboratoryId;

    @NotBlank(message = "Batch code is required")
    @Size(max = 100, message = "Batch code must not exceed 100 characters")
    private String batchCode;

    private Long certificateDocumentId;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    private String status;

    private String notes;

    private Boolean active;
}
