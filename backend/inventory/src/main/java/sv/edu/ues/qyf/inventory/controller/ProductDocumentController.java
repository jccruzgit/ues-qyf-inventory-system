package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentResponseDto;
import sv.edu.ues.qyf.inventory.service.ProductDocumentService;

@RestController
@RequestMapping("/api/product-documents")
@Tag(name = "Product Documents")
@SecurityRequirement(name = "bearerAuth")
public class ProductDocumentController {

    private final ProductDocumentService productDocumentService;

    public ProductDocumentController(ProductDocumentService productDocumentService) {
        this.productDocumentService = productDocumentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductDocumentResponseDto>> create(
            @Valid @RequestBody ProductDocumentRequestDto request) {
        ProductDocumentResponseDto response = productDocumentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product document registered successfully", response));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProductDocumentResponseDto>>> getActiveByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product documents retrieved successfully",
                productDocumentService.getActiveByProduct(productId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductDocumentResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product document retrieved successfully",
                productDocumentService.getById(id)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductDocumentResponseDto>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product document deactivated successfully",
                productDocumentService.deactivate(id)));
    }
}
