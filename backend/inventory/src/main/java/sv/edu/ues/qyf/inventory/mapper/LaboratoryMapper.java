package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.LaboratoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.Laboratory;

@Component
public class LaboratoryMapper {

    public LaboratoryResponseDto toResponseDto(Laboratory laboratory) {
        if (laboratory == null) {
            return null;
        }

        return LaboratoryResponseDto.builder()
                .id(laboratory.getId())
                .code(laboratory.getCode())
                .name(laboratory.getName())
                .description(laboratory.getDescription())
                .active(laboratory.getActive())
                .createdAt(laboratory.getCreatedAt())
                .updatedAt(laboratory.getUpdatedAt())
                .deletedAt(laboratory.getDeletedAt())
                .deletedById(laboratory.getDeletedBy() != null ? laboratory.getDeletedBy().getId() : null)
                .build();
    }
}
