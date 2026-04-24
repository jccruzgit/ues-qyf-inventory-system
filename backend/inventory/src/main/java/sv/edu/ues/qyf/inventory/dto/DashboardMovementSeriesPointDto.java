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
public class DashboardMovementSeriesPointDto {

    private LocalDate date;
    private String dayLabel;
    private BigDecimal entryQuantity;
    private BigDecimal exitQuantity;
    private Long entryMovements;
    private Long exitMovements;
}
