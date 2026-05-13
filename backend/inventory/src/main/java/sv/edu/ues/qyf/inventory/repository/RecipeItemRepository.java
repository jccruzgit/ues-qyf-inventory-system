package sv.edu.ues.qyf.inventory.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.ues.qyf.inventory.entity.RecipeItem;

public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long> {

    Optional<RecipeItem> findByIdAndRecipeId(Long id, Long recipeId);

    boolean existsByRecipeIdAndProductId(Long recipeId, Long productId);

    @Query("select coalesce(max(ri.itemOrder), 0) from RecipeItem ri where ri.recipe.id = :recipeId")
    Integer findMaxItemOrderByRecipeId(@Param("recipeId") Long recipeId);
}
