package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.UserRequestDto;
import sv.edu.ues.qyf.inventory.dto.UserResponseDto;

public interface UserService {

    UserResponseDto createUser(UserRequestDto request);

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id);
}
