package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlertResponseDto {

    private Long id;
    private Long laboratoryId;
    private InventoryAlertType alertType;
    private Long productId;
    private String productCode;
    private Long productBatchId;
    private String batchCode;
    private String message;
    private LocalDateTime triggeredAt;
    private Long acknowledgedById;
    private LocalDateTime acknowledgedAt;
}
