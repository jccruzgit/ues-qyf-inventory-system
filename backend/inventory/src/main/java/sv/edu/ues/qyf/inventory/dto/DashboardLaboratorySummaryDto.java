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
public class DashboardLaboratorySummaryDto {

    private Long laboratoryId;
    private String laboratoryCode;
    private String laboratoryName;
    private Long visibleProducts;
    private Long lowStockProducts;
    private Long expiringBatches;
    private BigDecimal quantityAvailable;
}
