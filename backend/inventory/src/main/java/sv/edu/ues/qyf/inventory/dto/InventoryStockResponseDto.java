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
public class InventoryStockResponseDto {

    private Long productId;
    private String productCode;
    private String productName;
    private Long laboratoryId;
    private String laboratoryCode;
    private String laboratoryName;
    private Long productBatchId;
    private String batchCode;
    private LocalDate expirationDate;
    private BigDecimal quantityAvailable;
    private BigDecimal minimumStock;
    private Boolean lowStock;
}
