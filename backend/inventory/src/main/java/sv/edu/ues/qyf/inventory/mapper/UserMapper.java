package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.UserResponseDto;
import sv.edu.ues.qyf.inventory.entity.User;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .active(user.getActive())
                .accessScope(user.getAccessScope())
                .createdAt(user.getCreatedAt())
                .role(roleMapper.toResponseDto(user.getRole()))
                .build();
    }
}
