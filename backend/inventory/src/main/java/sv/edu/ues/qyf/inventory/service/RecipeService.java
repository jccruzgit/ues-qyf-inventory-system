package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.RecipeItemRequestDto;
import sv.edu.ues.qyf.inventory.dto.RecipeResponseDto;
import sv.edu.ues.qyf.inventory.dto.RecipeRequestDto;

public interface RecipeService {

    RecipeResponseDto create(RecipeRequestDto request);

    List<RecipeResponseDto> getAll();

    RecipeResponseDto getById(Long id);

    RecipeResponseDto update(Long id, RecipeRequestDto request);

    RecipeResponseDto addItem(Long recipeId, RecipeItemRequestDto request);

    RecipeResponseDto deleteItem(Long recipeId, Long itemId);
}
