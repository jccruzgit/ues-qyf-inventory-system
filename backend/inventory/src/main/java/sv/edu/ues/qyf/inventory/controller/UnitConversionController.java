package sv.edu.ues.qyf.inventory.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.UnitConversionRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitConversionResponseDto;
import sv.edu.ues.qyf.inventory.service.UnitConversionService;

@RestController
@RequestMapping("/api/conversions")
public class UnitConversionController {

    private final UnitConversionService unitConversionService;

    public UnitConversionController(UnitConversionService unitConversionService) {
        this.unitConversionService = unitConversionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<UnitConversionResponseDto>> create(
            @Valid @RequestBody UnitConversionRequestDto request) {
        UnitConversionResponseDto response = unitConversionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Unit conversion created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UnitConversionResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Unit conversions retrieved successfully", unitConversionService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UnitConversionResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit conversion retrieved successfully", unitConversionService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<UnitConversionResponseDto>> update(
            @PathVariable Long id, @Valid @RequestBody UnitConversionRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit conversion updated successfully", unitConversionService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<UnitConversionResponseDto>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit conversion deactivated successfully", unitConversionService.deactivate(id)));
    }

    @GetMapping("/convert")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BigDecimal>> convert(
            @RequestParam Long sourceUnitId,
            @RequestParam Long targetUnitId,
            @RequestParam BigDecimal quantity) {
        BigDecimal convertedQuantity = unitConversionService.convert(sourceUnitId, targetUnitId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Quantity converted successfully", convertedQuantity));
    }
}
