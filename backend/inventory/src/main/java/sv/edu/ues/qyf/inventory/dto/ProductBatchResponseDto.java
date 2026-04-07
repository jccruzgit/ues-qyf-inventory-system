package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String status;
    private String notes;
    private Boolean active;
    private LocalDateTime deletedAt;
    private Long deletedById;
}
