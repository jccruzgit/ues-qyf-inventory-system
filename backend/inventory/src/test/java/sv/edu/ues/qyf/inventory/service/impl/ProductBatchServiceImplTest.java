package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
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

    private ProductBatchMapper productBatchMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    private ObjectMapper objectMapper;

    private ProductBatchServiceImpl productBatchService;

    @BeforeEach
    void setUp() {
        productBatchMapper = new ProductBatchMapper();
        objectMapper = new ObjectMapper();
        productBatchService = new ProductBatchServiceImpl(
                productBatchRepository,
                productBatchMapper,
                currentUserService,
                auditLogService,
                laboratoryAccessService,
                objectMapper);
    }

    @Test
    void getById_validatesLaboratoryResolvedFromBatchBeforeReturningData() {
        ProductBatch batch = ProductBatch.builder()
                .id(21L)
                .laboratory(Laboratory.builder().id(8L).build())
                .batchCode("LOT-21")
                .active(Boolean.TRUE)
                .build();

        when(productBatchRepository.findByIdAndActiveTrue(21L)).thenReturn(Optional.of(batch));

        ProductBatchResponseDto result = productBatchService.getById(21L);

        assertThat(result.getId()).isEqualTo(21L);
        assertThat(result.getLaboratoryId()).isEqualTo(8L);
        assertThat(result.getBatchCode()).isEqualTo("LOT-21");
        InOrder inOrder = inOrder(productBatchRepository, laboratoryAccessService);
        inOrder.verify(productBatchRepository).findByIdAndActiveTrue(21L);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(8L);
        verify(laboratoryAccessService).validateAccessToLaboratory(8L);
    }
}
