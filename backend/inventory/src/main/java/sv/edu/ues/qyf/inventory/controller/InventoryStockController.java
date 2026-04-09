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
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;

@RestController
@RequestMapping("/api/inventory-stock")
@Tag(name = "Inventory Stock")
@SecurityRequirement(name = "bearerAuth")
public class InventoryStockController {

    private final InventoryStockService inventoryStockService;

    public InventoryStockController(InventoryStockService inventoryStockService) {
        this.inventoryStockService = inventoryStockService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InventoryStockResponseDto>>> getStock(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long laboratoryId,
            @RequestParam(required = false) Long productBatchId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory stock retrieved successfully",
                inventoryStockService.getStock(productId, laboratoryId, productBatchId)));
    }
}
