package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.mapper.InventoryAlertMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.service.InventoryAlertService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class InventoryAlertServiceImpl implements InventoryAlertService {

    private final InventoryAlertRepository inventoryAlertRepository;
    private final InventoryAlertMapper inventoryAlertMapper;
    private final LaboratoryAccessService laboratoryAccessService;

    public InventoryAlertServiceImpl(
            InventoryAlertRepository inventoryAlertRepository,
            InventoryAlertMapper inventoryAlertMapper,
            LaboratoryAccessService laboratoryAccessService) {
        this.inventoryAlertRepository = inventoryAlertRepository;
        this.inventoryAlertMapper = inventoryAlertMapper;
        this.laboratoryAccessService = laboratoryAccessService;
    }

    @Override
    public List<InventoryAlertResponseDto> getPendingByLaboratory(Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        return inventoryAlertRepository.findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(laboratoryId).stream()
                .map(inventoryAlertMapper::toResponseDto)
                .toList();
    }
}
