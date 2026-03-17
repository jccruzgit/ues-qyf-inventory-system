package sv.edu.ues.qyf.inventory.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private OffsetDateTime createdAt;
    private RoleResponseDto role;
}
