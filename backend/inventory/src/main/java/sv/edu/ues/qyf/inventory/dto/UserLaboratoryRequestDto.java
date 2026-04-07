package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLaboratoryRequestDto {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Laboratory id is required")
    private Long laboratoryId;

    private LocalDateTime assignedAt;

    private Boolean active;
}
