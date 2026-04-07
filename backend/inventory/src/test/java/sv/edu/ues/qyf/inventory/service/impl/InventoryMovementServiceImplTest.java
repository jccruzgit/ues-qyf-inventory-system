package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.mapper.InventoryMovementMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceImplTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private InventoryMovementMapper inventoryMovementMapper;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @InjectMocks
    private InventoryMovementServiceImpl inventoryMovementService;

    @Test
    void getByLaboratory_validatesAccessBeforeLoadingMovements() {
        InventoryMovement movement = InventoryMovement.builder().id(4L).build();
        InventoryMovementResponseDto response = InventoryMovementResponseDto.builder().id(4L).laboratoryId(3L).build();

        when(inventoryMovementRepository.findByLaboratoryIdOrderByIdDesc(3L)).thenReturn(List.of(movement));
        when(inventoryMovementMapper.toResponseDto(movement)).thenReturn(response);

        List<InventoryMovementResponseDto> result = inventoryMovementService.getByLaboratory(3L);

        assertThat(result).containsExactly(response);
        InOrder inOrder = inOrder(laboratoryAccessService, inventoryMovementRepository, inventoryMovementMapper);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(3L);
        inOrder.verify(inventoryMovementRepository).findByLaboratoryIdOrderByIdDesc(3L);
        inOrder.verify(inventoryMovementMapper).toResponseDto(movement);
    }
}
