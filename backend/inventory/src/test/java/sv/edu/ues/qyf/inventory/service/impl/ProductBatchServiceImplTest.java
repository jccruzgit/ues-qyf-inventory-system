package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.ProductBatchOverviewResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.mapper.ProductBatchMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class ProductBatchServiceImplTest {

    @Mock
    private ProductBatchRepository productBatchRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

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
                inventoryMovementRepository,
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

    @Test
    void getOverview_returnsZeroStockBatchWithLatestEntryPrice() {
        UnitOfMeasure unit = UnitOfMeasure.builder().id(3L).name("Mililitro").symbol("mL").build();
        Product product = Product.builder()
                .id(11L)
                .code("QYF-011")
                .name("Acetona")
                .baseUnit(unit)
                .location(Location.builder().id(6L).name("Bodega central").build())
                .build();
        Laboratory laboratory = Laboratory.builder().id(8L).code("LAB-Q").name("Laboratorio Q").build();
        ProductBatch batch = ProductBatch.builder()
                .id(21L)
                .product(product)
                .laboratory(laboratory)
                .batchCode("LOT-21")
                .expirationDate(LocalDate.of(2026, 8, 15))
                .notes("Observacion del lote")
                .active(Boolean.TRUE)
                .build();

        InventoryMovement latestExit = InventoryMovement.builder()
                .id(101L)
                .movementType(MovementType.EXIT)
                .laboratory(laboratory)
                .build();
        latestExit.getLines().add(InventoryMovementLine.builder()
                .movement(latestExit)
                .product(product)
                .productBatch(batch)
                .quantity(new BigDecimal("5"))
                .build());

        InventoryMovement latestEntry = InventoryMovement.builder()
                .id(100L)
                .movementType(MovementType.ENTRY)
                .laboratory(laboratory)
                .build();
        latestEntry.getLines().add(InventoryMovementLine.builder()
                .movement(latestEntry)
                .product(product)
                .productBatch(batch)
                .quantity(new BigDecimal("5"))
                .unitPrice(new BigDecimal("14.50"))
                .priceUnit(unit)
                .build());

        when(laboratoryAccessService.hasAccessToAllLaboratories()).thenReturn(true);
        when(productBatchRepository.findByActiveTrueOrderByExpirationDateAscBatchCodeAsc()).thenReturn(List.of(batch));
        when(inventoryMovementRepository.findAllByOrderByPerformedAtDescIdDesc())
                .thenReturn(List.of(latestExit, latestEntry));

        List<ProductBatchOverviewResponseDto> result = productBatchService.getOverview(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(21L);
        assertThat(result.get(0).getQuantityAvailable()).isEqualByComparingTo("0");
        assertThat(result.get(0).getUnitPrice()).isEqualByComparingTo("14.50");
        assertThat(result.get(0).getPriceUnitName()).isEqualTo("Mililitro");
        assertThat(result.get(0).getLocationName()).isEqualTo("Bodega central");
        assertThat(result.get(0).getNotes()).isEqualTo("Observacion del lote");
    }
}
