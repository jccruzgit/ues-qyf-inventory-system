package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.UserLaboratoryRepository;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class LaboratoryAccessServiceImpl implements LaboratoryAccessService {

    private final CurrentUserService currentUserService;
    private final UserLaboratoryRepository userLaboratoryRepository;

    public LaboratoryAccessServiceImpl(
            CurrentUserService currentUserService, UserLaboratoryRepository userLaboratoryRepository) {
        this.currentUserService = currentUserService;
        this.userLaboratoryRepository = userLaboratoryRepository;
    }

    @Override
    public void validateAccessToLaboratory(Long laboratoryId) {
        if (laboratoryId == null) {
            throw new AccessDeniedException("Laboratory access cannot be resolved");
        }

        User currentUser = currentUserService.getAuthenticatedUser();
        AccessScope accessScope = resolveAccessScope(currentUser);

        switch (accessScope) {
            case ALL_LABS:
                return;
            case ASSIGNED_ONLY:
            case MULTI_LAB:
                validateAssignedLaboratoryAccess(currentUser.getId(), laboratoryId);
                return;
            default:
                throw new AccessDeniedException("Unsupported laboratory access scope: " + accessScope);
        }
    }

    @Override
    public boolean hasAccessToAllLaboratories() {
        User currentUser = currentUserService.getAuthenticatedUser();
        return resolveAccessScope(currentUser) == AccessScope.ALL_LABS;
    }

    @Override
    public List<Long> getAccessibleLaboratoryIds() {
        User currentUser = currentUserService.getAuthenticatedUser();
        AccessScope accessScope = resolveAccessScope(currentUser);

        if (accessScope == AccessScope.ALL_LABS) {
            return List.of();
        }

        return userLaboratoryRepository.findByUserIdAndActiveTrue(currentUser.getId()).stream()
                .map(assignment -> assignment.getLaboratory().getId())
                .toList();
    }

    private AccessScope resolveAccessScope(User currentUser) {
        if (currentUser.getAccessScope() == null) {
            throw new AccessDeniedException("User access scope is not configured");
        }
        return currentUser.getAccessScope();
    }

    private void validateAssignedLaboratoryAccess(Long userId, Long laboratoryId) {
        boolean hasAssignment = userLaboratoryRepository.existsByUserIdAndLaboratoryIdAndActiveTrue(userId, laboratoryId);

        if (!hasAssignment) {
            throw new AccessDeniedException("Access denied to laboratory id: " + laboratoryId);
        }
    }
}
