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

        if (hasAccessToAllLaboratories()) {
            return;
        }

        User currentUser = currentUserService.getAuthenticatedUser();
        boolean hasAssignment = userLaboratoryRepository.existsByUserIdAndLaboratoryIdAndActiveTrue(
                currentUser.getId(), laboratoryId);

        if (!hasAssignment) {
            throw new AccessDeniedException("Access denied to laboratory id: " + laboratoryId);
        }
    }

    @Override
    public boolean hasAccessToAllLaboratories() {
        return currentUserService.getAuthenticatedUser().getAccessScope() == AccessScope.ALL_LABS;
    }

    @Override
    public List<Long> getAccessibleLaboratoryIds() {
        if (hasAccessToAllLaboratories()) {
            return List.of();
        }

        User currentUser = currentUserService.getAuthenticatedUser();
        return userLaboratoryRepository.findByUserIdAndActiveTrue(currentUser.getId()).stream()
                .map(assignment -> assignment.getLaboratory().getId())
                .toList();
    }
}
