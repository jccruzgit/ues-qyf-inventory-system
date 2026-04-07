package sv.edu.ues.qyf.inventory.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.AccessScope;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean active;
    private AccessScope accessScope;
    private OffsetDateTime createdAt;
    private RoleResponseDto role;
}
