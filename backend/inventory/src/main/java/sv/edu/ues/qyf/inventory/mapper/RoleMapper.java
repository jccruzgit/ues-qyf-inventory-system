package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.RoleResponseDto;
import sv.edu.ues.qyf.inventory.entity.Role;

@Component
public class RoleMapper {

    public RoleResponseDto toResponseDto(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
