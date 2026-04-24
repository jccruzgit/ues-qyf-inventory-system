package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.MovementType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentMovementDto {

    private Long id;
    private MovementType movementType;
    private Long laboratoryId;
    private String laboratoryName;
    private String performedByUsername;
    private LocalDateTime performedAt;
    private String primaryProductName;
    private Long lineCount;
    private BigDecimal totalQuantity;
}
