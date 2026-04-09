package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.BatchStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchResponseDto {

    private Long id;
    private Long productId;
    private Long laboratoryId;
    private String batchCode;
    private Long certificateDocumentId;
    private BatchStatus status;
    private LocalDate expirationDate;
    private String notes;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long deletedById;
}
