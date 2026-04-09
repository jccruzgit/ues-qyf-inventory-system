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
import sv.edu.ues.qyf.inventory.dto.LocationRequestDto;
import sv.edu.ues.qyf.inventory.dto.LocationResponseDto;
import sv.edu.ues.qyf.inventory.service.LocationService;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<LocationResponseDto>> create(@Valid @RequestBody LocationRequestDto request) {
        LocationResponseDto response = locationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LocationResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Locations retrieved successfully", locationService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LocationResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Location retrieved successfully", locationService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<LocationResponseDto>> update(
            @PathVariable Long id, @Valid @RequestBody LocationRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", locationService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<LocationResponseDto>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Location deactivated successfully", locationService.deactivate(id)));
    }
}
