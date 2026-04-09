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
import org.mockito.InjectMocks;
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

    @InjectMocks
    private DefaultAdminSeederService defaultAdminSeederService;

    @Test
    void ensureDefaultAdmin_createsAdminRoleAndUserWhenMissing() {
        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("System administrator")
                .build();
        User persistedAdmin = User.builder()
                .id(10L)
                .username("admin")
                .email("admin@qyf.local")
                .fullName("Administrador del sistema")
                .active(Boolean.TRUE)
                .accessScope(AccessScope.ALL_LABS)
                .role(adminRole)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@qyf.local")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(adminRole);
        when(userService.createUser(any(UserRequestDto.class)))
                .thenReturn(UserResponseDto.builder().id(10L).build());
        when(userRepository.findById(10L)).thenReturn(Optional.of(persistedAdmin));

        User result = defaultAdminSeederService.ensureDefaultAdmin();

        assertThat(result).isSameAs(persistedAdmin);
        verify(roleRepository).save(argThat(role ->
                "ADMIN".equals(role.getName())
                        && "System administrator".equals(role.getDescription())));
        verify(userService).createUser(argThat(request ->
                "admin".equals(request.getUsername())
                        && "admin@qyf.local".equals(request.getEmail())
                        && "Admin123*".equals(request.getPassword())
                        && "Administrador del sistema".equals(request.getFullName())
                        && Boolean.TRUE.equals(request.getActive())
                        && AccessScope.ALL_LABS == request.getAccessScope()
                        && "ADMIN".equals(request.getRoleName())));
    }

    @Test
    void ensureDefaultAdmin_skipsCreationWhenAdminUsernameAlreadyExists() {
        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("System administrator")
                .build();
        User existingAdmin = User.builder()
                .id(10L)
                .username("admin")
                .email("admin@qyf.local")
                .fullName("Administrador del sistema")
                .active(Boolean.TRUE)
                .accessScope(AccessScope.ALL_LABS)
                .role(adminRole)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        User result = defaultAdminSeederService.ensureDefaultAdmin();

        assertThat(result).isSameAs(existingAdmin);
        verify(userRepository, never()).findByEmail(any());
        verifyNoInteractions(roleRepository, userService);
    }
}
