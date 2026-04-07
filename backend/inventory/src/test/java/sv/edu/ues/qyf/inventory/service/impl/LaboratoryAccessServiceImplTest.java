package sv.edu.ues.qyf.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.UserLaboratoryRepository;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class LaboratoryAccessServiceImplTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserLaboratoryRepository userLaboratoryRepository;

    @InjectMocks
    private LaboratoryAccessServiceImpl laboratoryAccessService;

    @Test
    void validateAccessToLaboratory_allowsUserAssignedToRequestedLaboratory() {
        User user = buildUser(7L, AccessScope.ASSIGNED_ONLY);
        when(currentUserService.getAuthenticatedUser()).thenReturn(user);
        when(userLaboratoryRepository.existsByUserIdAndLaboratoryIdAndActiveTrue(7L, 11L)).thenReturn(true);

        assertDoesNotThrow(() -> laboratoryAccessService.validateAccessToLaboratory(11L));

        verify(userLaboratoryRepository).existsByUserIdAndLaboratoryIdAndActiveTrue(7L, 11L);
    }

    @Test
    void validateAccessToLaboratory_deniesUserWithoutAssignment() {
        User user = buildUser(9L, AccessScope.MULTI_LAB);
        when(currentUserService.getAuthenticatedUser()).thenReturn(user);
        when(userLaboratoryRepository.existsByUserIdAndLaboratoryIdAndActiveTrue(9L, 13L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> laboratoryAccessService.validateAccessToLaboratory(13L));

        verify(userLaboratoryRepository).existsByUserIdAndLaboratoryIdAndActiveTrue(9L, 13L);
    }

    @Test
    void validateAccessToLaboratory_allowsGlobalAccessWithoutCheckingAssignments() {
        User user = buildUser(5L, AccessScope.ALL_LABS);
        when(currentUserService.getAuthenticatedUser()).thenReturn(user);

        assertDoesNotThrow(() -> laboratoryAccessService.validateAccessToLaboratory(17L));

        verify(userLaboratoryRepository, never()).existsByUserIdAndLaboratoryIdAndActiveTrue(5L, 17L);
    }

    private User buildUser(Long id, AccessScope accessScope) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .password("encoded-password")
                .email("user" + id + "@example.com")
                .fullName("User " + id)
                .active(Boolean.TRUE)
                .accessScope(accessScope)
                .role(Role.builder().id(1L).name("VIEWER").description("Viewer").build())
                .build();
    }
}
