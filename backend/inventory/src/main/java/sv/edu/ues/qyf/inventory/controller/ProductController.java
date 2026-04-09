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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductUpdateRequestDto;
import sv.edu.ues.qyf.inventory.service.ProductService;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> create(@Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", productService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", productService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> update(
            @PathVariable Long id, @Valid @RequestBody ProductUpdateRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", productService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponseDto>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product deactivated successfully", productService.deactivate(id)));
    }
}
