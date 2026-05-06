package sv.edu.ues.qyf.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.BatchStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchOverviewResponseDto {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Long laboratoryId;
    private String laboratoryCode;
    private String laboratoryName;
    private String batchCode;
    private String locationName;
    private String unitName;
    private String unitSymbol;
    private LocalDate expirationDate;
    private BigDecimal quantityAvailable;
    private BigDecimal unitPrice;
    private String priceUnitName;
    private String priceUnitSymbol;
    private BatchStatus status;
    private String notes;
}
