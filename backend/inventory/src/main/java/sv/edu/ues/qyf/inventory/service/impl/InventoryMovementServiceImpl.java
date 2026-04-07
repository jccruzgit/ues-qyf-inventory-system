package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.mapper.InventoryMovementMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final LaboratoryAccessService laboratoryAccessService;

    public InventoryMovementServiceImpl(
            InventoryMovementRepository inventoryMovementRepository,
            InventoryMovementMapper inventoryMovementMapper,
            LaboratoryAccessService laboratoryAccessService) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMovementMapper = inventoryMovementMapper;
        this.laboratoryAccessService = laboratoryAccessService;
    }

    @Override
    public List<InventoryMovementResponseDto> getByLaboratory(Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        return inventoryMovementRepository.findByLaboratoryIdOrderByIdDesc(laboratoryId).stream()
                .map(inventoryMovementMapper::toResponseDto)
                .toList();
    }
}
