package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sv.edu.ues.qyf.inventory.entity.BatchStatus;

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

    private BatchStatus status;

    private LocalDate expirationDate;

    private String notes;

    private Boolean active;
}
