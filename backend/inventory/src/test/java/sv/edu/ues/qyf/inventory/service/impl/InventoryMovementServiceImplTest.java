package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementLineRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.AccessScope;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.Role;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.mapper.InventoryMovementMapper;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceImplTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LaboratoryRepository laboratoryRepository;

    @Mock
    private LaboratoryAccessService laboratoryAccessService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private InventoryMovementMapper inventoryMovementMapper = new InventoryMovementMapper();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InventoryMovementServiceImpl inventoryMovementService;

    @Test
    void create_entryAddsStockAndPersistsMovement() {
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.ENTRY,
                3L,
                "Entrada inicial",
                List.of(new InventoryMovementLineRequestDto(9L, new BigDecimal("100"), "Carga inicial")));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .name("Reactivo")
                .currentStock(BigDecimal.ZERO)
                .active(Boolean.TRUE)
                .build();
        User currentUser = User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(15L);
            movement.getLines().get(0).setId(30L);
            return movement;
        });

        InventoryMovementResponseDto response = inventoryMovementService.create(request);

        assertThat(product.getCurrentStock()).isEqualByComparingTo("100");
        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(response.getLines()).hasSize(1);
        assertThat(response.getLines().get(0).getQuantity()).isEqualByComparingTo("100");
        verify(inventoryMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.ENTRY
                        && movement.getLaboratory().getId().equals(3L)
                        && movement.getPerformedBy().getId().equals(2L)
                        && movement.getLines().size() == 1
                        && movement.getLines().get(0).getProduct().getId().equals(9L)));
        verify(auditLogService).logAction(
                org.mockito.ArgumentMatchers.eq("inventory_movements"),
                org.mockito.ArgumentMatchers.eq(15L),
                org.mockito.ArgumentMatchers.eq("CREATE"),
                org.mockito.ArgumentMatchers.eq(3L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.contains("\"movementType\":\"ENTRY\""),
                org.mockito.ArgumentMatchers.eq("Inventory movement registered"));
    }

    @Test
    void create_exitWithInsufficientStockThrowsControlledError() {
        InventoryMovementRequestDto request = new InventoryMovementRequestDto(
                MovementType.EXIT,
                3L,
                "Salida de laboratorio",
                List.of(new InventoryMovementLineRequestDto(9L, new BigDecimal("25"), null)));
        Laboratory laboratory = Laboratory.builder().id(3L).active(Boolean.TRUE).build();
        Product product = Product.builder()
                .id(9L)
                .code("PRD-9")
                .currentStock(new BigDecimal("10"))
                .active(Boolean.TRUE)
                .build();
        User currentUser = User.builder()
                .id(2L)
                .username("tech")
                .accessScope(AccessScope.ALL_LABS)
                .role(Role.builder().id(1L).name("LAB_TECHNICIAN").description("role").build())
                .active(Boolean.TRUE)
                .build();

        when(laboratoryRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(laboratory));
        when(productRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.of(product));
        when(currentUserService.getAuthenticatedUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> inventoryMovementService.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product PRD-9");
    }

    @Test
    void getByLaboratory_validatesAccessBeforeLoadingMovements() {
        InventoryMovement movement = InventoryMovement.builder()
                .id(4L)
                .laboratory(Laboratory.builder().id(3L).build())
                .movementType(MovementType.ENTRY)
                .build();

        when(inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(3L))
                .thenReturn(List.of(movement));

        List<InventoryMovementResponseDto> result = inventoryMovementService.getByLaboratory(3L);

        assertThat(result).hasSize(1);
        InOrder inOrder = inOrder(laboratoryAccessService, inventoryMovementRepository);
        inOrder.verify(laboratoryAccessService).validateAccessToLaboratory(3L);
        inOrder.verify(inventoryMovementRepository).findByLaboratoryIdOrderByPerformedAtDescIdDesc(3L);
        verify(inventoryMovementMapper).toResponseDto(movement);
    }
}
