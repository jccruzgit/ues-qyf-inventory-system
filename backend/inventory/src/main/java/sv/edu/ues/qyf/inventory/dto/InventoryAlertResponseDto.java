package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlertResponseDto {

    private Long id;
    private Long laboratoryId;
    private String laboratoryCode;
    private String laboratoryName;
    private InventoryAlertType alertType;
    private Long productId;
    private String productCode;
    private String productName;
    private Long productBatchId;
    private String batchCode;
    private String locationName;
    private BigDecimal quantityAvailable;
    private BigDecimal minimumStock;
    private LocalDate expirationDate;
    private String severity;
    private String status;
    private String message;
    private LocalDateTime triggeredAt;
    private Long acknowledgedById;
    private LocalDateTime acknowledgedAt;
}
