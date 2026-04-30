package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

    private LaboratoryMapper laboratoryMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    private ObjectMapper objectMapper;

    private LaboratoryServiceImpl laboratoryService;

    @BeforeEach
    void setUp() {
        laboratoryMapper = new LaboratoryMapper();
        objectMapper = new ObjectMapper();
        laboratoryService = new LaboratoryServiceImpl(
                laboratoryRepository,
                laboratoryMapper,
                currentUserService,
                auditLogService,
                laboratoryAccessService,
                objectMapper);
    }

    @Test
    void getAll_limitsRestrictedUsersToAccessibleLaboratories() {
        Laboratory laboratory = Laboratory.builder()
                .id(2L)
                .code("LAB-02")
                .name("Laboratorio 2")
                .active(Boolean.TRUE)
                .build();

        when(laboratoryAccessService.hasAccessToAllLaboratories()).thenReturn(false);
        when(laboratoryAccessService.getAccessibleLaboratoryIds()).thenReturn(List.of(2L, 4L));
        when(laboratoryRepository.findByIdInAndActiveTrue(List.of(2L, 4L))).thenReturn(List.of(laboratory));

        List<LaboratoryResponseDto> result = laboratoryService.getAll();

        assertThat(result).hasSize(1);
        LaboratoryResponseDto response = result.get(0);
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getCode()).isEqualTo("LAB-02");
        assertThat(response.getName()).isEqualTo("Laboratorio 2");
        assertThat(response.getActive()).isTrue();
        verify(laboratoryAccessService).getAccessibleLaboratoryIds();
        verify(laboratoryRepository).findByIdInAndActiveTrue(List.of(2L, 4L));
    }
}
