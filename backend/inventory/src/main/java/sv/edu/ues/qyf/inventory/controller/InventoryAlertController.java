package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.InventoryAlertResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;
import sv.edu.ues.qyf.inventory.service.InventoryAlertService;

@RestController
@RequestMapping("/api/inventory-alerts")
@Tag(name = "Inventory Alerts")
@SecurityRequirement(name = "bearerAuth")
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    public InventoryAlertController(InventoryAlertService inventoryAlertService) {
        this.inventoryAlertService = inventoryAlertService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InventoryAlertResponseDto>>> getAlerts(
            @RequestParam Long laboratoryId,
            @RequestParam(required = false) InventoryAlertType alertType,
            @RequestParam(defaultValue = "true") Boolean pendingOnly) {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory alerts retrieved successfully",
                inventoryAlertService.getAlerts(laboratoryId, alertType, pendingOnly)));
    }
}
