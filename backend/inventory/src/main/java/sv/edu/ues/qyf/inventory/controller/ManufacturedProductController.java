package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductResponseDto;
import sv.edu.ues.qyf.inventory.service.ManufacturedProductService;

@RestController
@RequestMapping("/api/manufactured-products")
@Tag(name = "Manufactured Products")
@SecurityRequirement(name = "bearerAuth")
public class ManufacturedProductController {

    private final ManufacturedProductService manufacturedProductService;

    public ManufacturedProductController(ManufacturedProductService manufacturedProductService) {
        this.manufacturedProductService = manufacturedProductService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ManufacturedProductResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Manufactured products retrieved successfully",
                manufacturedProductService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ManufacturedProductResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Manufactured product retrieved successfully",
                manufacturedProductService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ManufacturedProductResponseDto>> create(
            @Valid @RequestBody ManufacturedProductRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Manufactured product created successfully",
                        manufacturedProductService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<ManufacturedProductResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ManufacturedProductRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Manufactured product updated successfully",
                manufacturedProductService.update(id, request)));
    }
}
