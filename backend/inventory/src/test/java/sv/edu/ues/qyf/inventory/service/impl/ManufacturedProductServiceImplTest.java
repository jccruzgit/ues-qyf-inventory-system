package sv.edu.ues.qyf.inventory.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductResponseDto;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;
import sv.edu.ues.qyf.inventory.mapper.ManufacturedProductMapper;
import sv.edu.ues.qyf.inventory.repository.ManufacturedProductRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ManufacturedProductServiceImplTest {

    @Mock
    private ManufacturedProductRepository manufacturedProductRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    private ManufacturedProductServiceImpl manufacturedProductService;

    @BeforeEach
    void setUp() {
        manufacturedProductService = new ManufacturedProductServiceImpl(
                manufacturedProductRepository,
                new ManufacturedProductMapper(),
                currentUserService,
                auditLogService,
                new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void create_persistsAcademicTraceabilityAndWritesAuditLog() {
        ManufacturedProductRequestDto request = new ManufacturedProductRequestDto(
                " FAB-001 ",
                " Jabon liquido ",
                " Demo ",
                " G01M ",
                " 2026-I ",
                " LT-01 ",
                Boolean.TRUE);

        when(manufacturedProductRepository.findByCode("FAB-001")).thenReturn(Optional.empty());
        when(manufacturedProductRepository.save(any(ManufacturedProduct.class))).thenAnswer(invocation -> {
            ManufacturedProduct product = invocation.getArgument(0);
            product.setId(11L);
            product.setCreatedAt(LocalDateTime.of(2026, 6, 3, 11, 0));
            product.setUpdatedAt(LocalDateTime.of(2026, 6, 3, 11, 0));
            return product;
        });

        ManufacturedProductResponseDto response = manufacturedProductService.create(request);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getCode()).isEqualTo("FAB-001");
        assertThat(response.getName()).isEqualTo("Jabon liquido");
        assertThat(response.getGroupCode()).isEqualTo("G01M");
        assertThat(response.getCycle()).isEqualTo("2026-I");
        assertThat(response.getLotNumber()).isEqualTo("LT-01");
        verify(manufacturedProductRepository).save(argThat(product ->
                product.getCode().equals("FAB-001")
                        && product.getName().equals("Jabon liquido")
                        && product.getGroupCode().equals("G01M")
                        && product.getCycle().equals("2026-I")
                        && product.getLotNumber().equals("LT-01")));
        verify(auditLogService)
                .logAction(
                        eq("manufactured_products"),
                        eq(11L),
                        eq("CREATE"),
                        isNull(),
                        isNull(),
                        argThat(payload -> payload.contains("\"groupCode\":\"G01M\"")
                                && payload.contains("\"cycle\":\"2026-I\"")
                                && payload.contains("\"lotNumber\":\"LT-01\"")),
                        eq("Manufactured product created"));
    }
}
