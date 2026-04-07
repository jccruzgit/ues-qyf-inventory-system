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
public class AuditLogResponseDto {

    private Long id;
    private String tableName;
    private Long recordId;
    private String action;
    private Long changedById;
    private LocalDateTime changedAt;
    private Long laboratoryId;
    private String description;
}
