package sv.edu.ues.qyf.inventory.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.ues.qyf.inventory.dto.ApiResponse;
import sv.edu.ues.qyf.inventory.dto.RecipeItemRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipeRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipeResponseDto;
import sv.edu.ues.qyf.inventory.service.RecipeService;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipes")
@SecurityRequirement(name = "bearerAuth")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RecipeResponseDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Recipes retrieved successfully", recipeService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Recipe retrieved successfully", recipeService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> create(@Valid @RequestBody RecipeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recipe created successfully", recipeService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Recipe updated successfully", recipeService.update(id, request)));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody RecipeItemRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recipe item added successfully", recipeService.addItem(id, request)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> deleteItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Recipe item removed successfully",
                recipeService.deleteItem(id, itemId)));
    }
}
