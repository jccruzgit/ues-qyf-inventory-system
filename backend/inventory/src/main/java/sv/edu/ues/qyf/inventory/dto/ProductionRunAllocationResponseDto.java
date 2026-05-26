package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunAllocationResponseDto {

    private Long productBatchId;
    private String batchCode;
    private LocalDate expirationDate;
    private BigDecimal availableQuantity;
    private BigDecimal suggestedQuantity;
}
