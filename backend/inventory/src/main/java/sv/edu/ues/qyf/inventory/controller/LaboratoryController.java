package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.LaboratoryResponseDto;
import sv.edu.ues.qyf.inventory.service.LaboratoryService;

@RestController
@RequestMapping("/api/laboratories")
@Tag(name = "Laboratories")
@SecurityRequirement(name = "bearerAuth")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    public LaboratoryController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LaboratoryResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(
                "Laboratories retrieved successfully",
                laboratoryService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LaboratoryResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Laboratory retrieved successfully",
                laboratoryService.getById(id)));
    }
}
