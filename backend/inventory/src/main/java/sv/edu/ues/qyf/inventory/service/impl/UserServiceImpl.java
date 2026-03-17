package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.UserRequestDto;
import sv.edu.ues.qyf.inventory.dto.UserResponseDto;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.UserMapper;
import sv.edu.ues.qyf.inventory.repository.RoleRepository;
import sv.edu.ues.qyf.inventory.repository.UserRepository;
import sv.edu.ues.qyf.inventory.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDto createUser(UserRequestDto request) {
        validatePassword(request.getPassword());
        validateDuplicates(request.getUsername(), request.getEmail());

        Role role = roleRepository.findByName(normalizeRoleName(request.getRoleName()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with name: " + request.getRoleName()));

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .active(request.getActive() != null ? request.getActive() : Boolean.TRUE)
                .role(role)
                .build();

        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateDuplicates(String username, String email) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DuplicateResourceException("Username already exists: " + normalizedUsername);
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email already exists: " + normalizedEmail);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password cannot be null or blank");
        }
    }

    private String normalizeRoleName(String roleName) {
        return roleName.trim().toUpperCase(Locale.ROOT);
    }
}
