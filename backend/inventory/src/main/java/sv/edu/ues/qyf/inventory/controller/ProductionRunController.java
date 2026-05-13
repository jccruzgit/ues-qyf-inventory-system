package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.ProductionRunRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductionRunResponseDto;
import sv.edu.ues.qyf.inventory.service.ProductionRunService;

@RestController
@RequestMapping("/api/production-runs")
@Tag(name = "Production Runs")
@SecurityRequirement(name = "bearerAuth")
public class ProductionRunController {

    private final ProductionRunService productionRunService;

    public ProductionRunController(ProductionRunService productionRunService) {
        this.productionRunService = productionRunService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<ProductionRunResponseDto>> create(
            @Valid @RequestBody ProductionRunRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Production run created successfully",
                        productionRunService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductionRunResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Production run retrieved successfully",
                productionRunService.getById(id)));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<ProductionRunResponseDto>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Production run confirmed successfully",
                productionRunService.confirm(id)));
    }
}
