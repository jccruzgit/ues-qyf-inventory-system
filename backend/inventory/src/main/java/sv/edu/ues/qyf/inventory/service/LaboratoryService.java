package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.LaboratoryResponseDto;

public interface LaboratoryService {

    List<LaboratoryResponseDto> getAll();

    LaboratoryResponseDto getById(Long id);

    LaboratoryResponseDto deactivate(Long id);

    LaboratoryResponseDto restore(Long id);
}
