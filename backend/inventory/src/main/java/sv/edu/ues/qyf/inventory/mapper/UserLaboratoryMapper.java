package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.UserLaboratoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.UserLaboratory;

@Component
public class UserLaboratoryMapper {

    public UserLaboratoryResponseDto toResponseDto(UserLaboratory userLaboratory) {
        if (userLaboratory == null) {
            return null;
        }

        return UserLaboratoryResponseDto.builder()
                .id(userLaboratory.getId())
                .userId(userLaboratory.getUser() != null ? userLaboratory.getUser().getId() : null)
                .laboratoryId(userLaboratory.getLaboratory() != null ? userLaboratory.getLaboratory().getId() : null)
                .assignedAt(userLaboratory.getAssignedAt())
                .active(userLaboratory.getActive())
                .build();
    }
}
