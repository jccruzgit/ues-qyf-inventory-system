package sv.edu.ues.qyf.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.UserRequestDto;
import sv.edu.ues.qyf.inventory.dto.UserResponseDto;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.RoleRepository;
import sv.edu.ues.qyf.inventory.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DefaultAdminSeederServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserService userService;

    @Test
    void ensureDefaultAdmin_createsAdminRoleAndUserWhenMissing() {
        DefaultAdminSeederService defaultAdminSeederService = createSeederService();
        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("System administrator")
                .build();
        User persistedAdmin = User.builder()
                .id(10L)
                .username("admin")
                .email("admin@qyf.demo")
                .fullName("Administrador Demo")
                .active(Boolean.TRUE)
                .accessScope(AccessScope.ALL_LABS)
                .role(adminRole)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@qyf.demo")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);
        when(userService.createUser(any(UserRequestDto.class)))
                .thenReturn(UserResponseDto.builder().id(10L).build());
        when(userRepository.findById(10L)).thenReturn(Optional.of(persistedAdmin));

        Optional<User> result = defaultAdminSeederService.ensureDefaultAdmin();

        assertThat(result).containsSame(persistedAdmin);
        verify(roleRepository).save(argThat(role ->
                "ADMIN".equals(role.getName())
                        && "System administrator".equals(role.getDescription())));
        verify(userService).createUser(argThat(request ->
                "admin".equals(request.getUsername())
                        && "admin@qyf.demo".equals(request.getEmail())
                        && "TemporaryPass123*".equals(request.getPassword())
                        && "Administrador Demo".equals(request.getFullName())
                        && Boolean.TRUE.equals(request.getActive())
                        && AccessScope.ALL_LABS == request.getAccessScope()
                        && "ADMIN".equals(request.getRoleName())));
    }

    @Test
    void ensureDefaultAdmin_skipsCreationWhenAdminUsernameAlreadyExists() {
        DefaultAdminSeederService defaultAdminSeederService = createSeederService();
        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("System administrator")
                .build();
        User existingAdmin = User.builder()
                .id(10L)
                .username("admin")
                .email("admin@qyf.demo")
                .fullName("Administrador Demo")
                .active(Boolean.TRUE)
                .accessScope(AccessScope.ALL_LABS)
                .role(adminRole)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        Optional<User> result = defaultAdminSeederService.ensureDefaultAdmin();

        assertThat(result).containsSame(existingAdmin);
        verify(userRepository, never()).findByEmail(any());
        verifyNoInteractions(roleRepository, userService);
    }

    private DefaultAdminSeederService createSeederService() {
        return new DefaultAdminSeederService(
                userRepository,
                roleRepository,
                userService,
                true,
                "admin",
                "admin@qyf.demo",
                "TemporaryPass123*",
                "Administrador Demo");
    }
}
