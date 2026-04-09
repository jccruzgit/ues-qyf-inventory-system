package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.HealthResponseDto;

@RestController
@RequestMapping("/api")
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponseDto>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                "Service is healthy",
                new HealthResponseDto("UP", "UES QYF Inventory System")));
    }
}
