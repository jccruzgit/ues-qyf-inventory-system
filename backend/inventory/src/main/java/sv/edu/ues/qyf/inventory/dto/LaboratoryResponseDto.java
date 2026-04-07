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
public class LaboratoryResponseDto {

    private Long id;
    private Boolean active;
    private LocalDateTime deletedAt;
    private Long deletedById;
}
