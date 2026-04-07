package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.LaboratoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.mapper.LaboratoryMapper;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class LaboratoryServiceImplTest {

    @Mock
    private LaboratoryRepository laboratoryRepository;

    @Mock
    private LaboratoryMapper laboratoryMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LaboratoryServiceImpl laboratoryService;

    @Test
    void getAll_limitsRestrictedUsersToAccessibleLaboratories() {
        Laboratory laboratory = Laboratory.builder().id(2L).active(Boolean.TRUE).build();
        LaboratoryResponseDto response = LaboratoryResponseDto.builder().id(2L).active(Boolean.TRUE).build();

        when(laboratoryAccessService.hasAccessToAllLaboratories()).thenReturn(false);
        when(laboratoryAccessService.getAccessibleLaboratoryIds()).thenReturn(List.of(2L, 4L));
        when(laboratoryRepository.findByIdInAndActiveTrue(List.of(2L, 4L))).thenReturn(List.of(laboratory));
        when(laboratoryMapper.toResponseDto(laboratory)).thenReturn(response);

        List<LaboratoryResponseDto> result = laboratoryService.getAll();

        assertThat(result).containsExactly(response);
        verify(laboratoryAccessService).getAccessibleLaboratoryIds();
        verify(laboratoryRepository).findByIdInAndActiveTrue(List.of(2L, 4L));
    }
}
