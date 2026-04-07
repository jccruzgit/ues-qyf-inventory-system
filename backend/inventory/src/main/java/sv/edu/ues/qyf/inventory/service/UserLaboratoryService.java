package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.UserLaboratoryRequestDto;
import sv.edu.ues.qyf.inventory.dto.UserLaboratoryResponseDto;

public interface UserLaboratoryService {

    UserLaboratoryResponseDto assign(UserLaboratoryRequestDto request);

    List<UserLaboratoryResponseDto> getActiveAssignmentsByUser(Long userId);
}
