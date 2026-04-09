package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementLineResponseDto {

    private Long id;
    private Long productId;
    private Long productBatchId;
    private String batchCode;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private String lineNotes;
}
