package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.mapper.ProductBatchMapper;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class ProductBatchServiceImplTest {

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private ProductBatchMapper productBatchMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProductBatchServiceImpl productBatchService;

    @Test
    void getById_validatesLaboratoryResolvedFromBatchBeforeReturningData() {
        ProductBatch batch = ProductBatch.builder()
                .id(21L)
                .laboratory(Laboratory.builder().id(8L).build())
                .active(Boolean.TRUE)
                .build();
        ProductBatchResponseDto response = ProductBatchResponseDto.builder()
                .id(21L)
                .laboratoryId(8L)
                .build();

        when(productBatchRepository.findByIdAndActiveTrue(21L)).thenReturn(Optional.of(batch));
        when(productBatchMapper.toResponseDto(batch)).thenReturn(response);

        ProductBatchResponseDto result = productBatchService.getById(21L);

        assertThat(result).isSameAs(response);
        InOrder inOrder = inOrder(productBatchRepository, laboratoryAccessService, productBatchMapper);
        inOrder.verify(productBatchRepository).findByIdAndActiveTrue(21L);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(8L);
        inOrder.verify(productBatchMapper).toResponseDto(batch);
        verify(laboratoryAccessService).validateAccessToLaboratory(8L);
    }
}
