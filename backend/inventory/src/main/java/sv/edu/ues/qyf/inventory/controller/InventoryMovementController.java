package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementFilterDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementReverseRequestDto;
import sv.edu.ues.qyf.inventory.dto.InventoryMovementResponseDto;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.service.InventoryMovementService;

@RestController
@RequestMapping("/api/inventory-movements")
@Tag(name = "Inventory Movements")
@SecurityRequirement(name = "bearerAuth")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    public InventoryMovementController(InventoryMovementService inventoryMovementService) {
        this.inventoryMovementService = inventoryMovementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<InventoryMovementResponseDto>> create(
            @Valid @RequestBody InventoryMovementRequestDto request) {
        InventoryMovementResponseDto response = inventoryMovementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory movement registered successfully", response));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<InventoryMovementResponseDto>> reverse(
            @PathVariable Long id,
            @Valid @RequestBody InventoryMovementReverseRequestDto request) {
        InventoryMovementResponseDto response = inventoryMovementService.reverse(id, request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory movement reversed successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InventoryMovementResponseDto>>> getAll(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) MovementType movementType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        InventoryMovementFilterDto filter =
                new InventoryMovementFilterDto(productId, laboratoryId, movementType, dateFrom, dateTo);
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory movements retrieved successfully",
                hasFilters(filter)
                        ? inventoryMovementService.search(filter)
                        : inventoryMovementService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<InventoryMovementResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory movement retrieved successfully",
                inventoryMovementService.getById(id)));
    }

    private boolean hasFilters(InventoryMovementFilterDto filter) {
        return filter.getProductId() != null
                || filter.getLaboratoryId() != null
                || filter.getMovementType() != null
                || filter.getDateFrom() != null
                || filter.getDateTo() != null;
    }
}
