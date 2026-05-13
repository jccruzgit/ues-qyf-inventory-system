package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @EntityGraph(attributePaths = {
        "manufacturedProduct",
        "items",
        "items.product",
        "items.product.baseUnit",
        "items.product.location",
        "items.unitOfMeasure"
    })
    Optional<Recipe> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {
        "manufacturedProduct",
        "items",
        "items.product",
        "items.product.baseUnit",
        "items.product.location",
        "items.unitOfMeasure"
    })
    Optional<Recipe> findById(Long id);

    Optional<Recipe> findByCode(String code);

    @EntityGraph(attributePaths = {
        "manufacturedProduct",
        "items",
        "items.product",
        "items.product.baseUnit",
        "items.product.location",
        "items.unitOfMeasure"
    })
    List<Recipe> findByActiveTrue(Sort sort);
}
