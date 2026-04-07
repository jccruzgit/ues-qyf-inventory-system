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
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.mapper.InventoryAlertMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryAlertRepository;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class InventoryAlertServiceImplTest {

    @Mock
    private InventoryAlertRepository inventoryAlertRepository;

    @Mock
    private InventoryAlertMapper inventoryAlertMapper;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @InjectMocks
    private InventoryAlertServiceImpl inventoryAlertService;

    @Test
    void getPendingByLaboratory_validatesAccessBeforeLoadingAlerts() {
        InventoryAlert alert = InventoryAlert.builder().id(6L).build();
        InventoryAlertResponseDto response = InventoryAlertResponseDto.builder().id(6L).laboratoryId(12L).build();

        when(inventoryAlertRepository.findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(12L))
                .thenReturn(List.of(alert));
        when(inventoryAlertMapper.toResponseDto(alert)).thenReturn(response);

        List<InventoryAlertResponseDto> result = inventoryAlertService.getPendingByLaboratory(12L);

        assertThat(result).containsExactly(response);
        InOrder inOrder = inOrder(laboratoryAccessService, inventoryAlertRepository, inventoryAlertMapper);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(12L);
        inOrder.verify(inventoryAlertRepository).findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(12L);
        inOrder.verify(inventoryAlertMapper).toResponseDto(alert);
    }
}
