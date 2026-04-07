package sv.edu.ues.qyf.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.AccessScope;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String username;
    private String role;
    private String fullName;
    private AccessScope accessScope;
}
