package sv.edu.ues.qyf.inventory.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponseDto {

    private Long totalActiveProducts;
    private Long lowStockProducts;
    private Long expiringBatches;
    private Long accessibleLaboratories;
    private Long movementsLastSevenDays;
    private List<DashboardMovementSeriesPointDto> movementSeries;
    private List<DashboardRecentMovementDto> recentMovements;
    private List<DashboardLaboratorySummaryDto> inventoryByLaboratory;
}
