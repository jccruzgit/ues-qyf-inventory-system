package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;
import sv.edu.ues.qyf.inventory.service.ProductBatchService;

@RestController
@RequestMapping("/api/product-batches")
@Tag(name = "Product Batches")
@SecurityRequirement(name = "bearerAuth")
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    public ProductBatchController(ProductBatchService productBatchService) {
        this.productBatchService = productBatchService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProductBatchResponseDto>>> getByProductAndLaboratory(
            @RequestParam Long productId,
            @RequestParam Long laboratoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product batches retrieved successfully",
                productBatchService.getActiveByProductAndLaboratory(productId, laboratoryId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductBatchResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product batch retrieved successfully",
                productBatchService.getById(id)));
    }
}
