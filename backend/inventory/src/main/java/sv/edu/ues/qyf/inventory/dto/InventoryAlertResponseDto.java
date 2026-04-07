package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAlertResponseDto {

    private Long id;
    private Long laboratoryId;
    private Long acknowledgedById;
    private LocalDateTime acknowledgedAt;
}
