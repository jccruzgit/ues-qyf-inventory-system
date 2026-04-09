package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementLineRequestDto {

    @NotNull(message = "Product id is required")
    private Long productId;

    private Long productBatchId;

    private String batchCode;

    private LocalDate expirationDate;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String lineNotes;

    public InventoryMovementLineRequestDto(Long productId, BigDecimal quantity, String lineNotes) {
        this.productId = productId;
        this.quantity = quantity;
        this.lineNotes = lineNotes;
    }

    public InventoryMovementLineRequestDto(
            Long productId,
            Long productBatchId,
            BigDecimal quantity,
            String lineNotes) {
        this.productId = productId;
        this.productBatchId = productBatchId;
        this.quantity = quantity;
        this.lineNotes = lineNotes;
    }
}
