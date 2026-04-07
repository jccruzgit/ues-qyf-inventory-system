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
public class UserLaboratoryResponseDto {

    private Long id;
    private Long userId;
    private Long laboratoryId;
    private LocalDateTime assignedAt;
    private Boolean active;
}
