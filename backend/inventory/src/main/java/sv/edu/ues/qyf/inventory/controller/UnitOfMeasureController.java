package sv.edu.ues.qyf.inventory.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureResponseDto;
import sv.edu.ues.qyf.inventory.service.UnitOfMeasureService;

@RestController
@RequestMapping("/api/units")
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;

    public UnitOfMeasureController(UnitOfMeasureService unitOfMeasureService) {
        this.unitOfMeasureService = unitOfMeasureService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponseDto>> create(
            @Valid @RequestBody UnitOfMeasureRequestDto request) {
        UnitOfMeasureResponseDto response = unitOfMeasureService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Unit of measure created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UnitOfMeasureResponseDto>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Units of measure retrieved successfully", unitOfMeasureService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit of measure retrieved successfully", unitOfMeasureService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponseDto>> update(
            @PathVariable Long id, @Valid @RequestBody UnitOfMeasureRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit of measure updated successfully", unitOfMeasureService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<UnitOfMeasureResponseDto>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit of measure deactivated successfully", unitOfMeasureService.deactivate(id)));
    }
}
