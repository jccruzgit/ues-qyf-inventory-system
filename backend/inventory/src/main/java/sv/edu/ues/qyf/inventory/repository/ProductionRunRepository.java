package sv.edu.ues.qyf.inventory.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.ProductionRun;

public interface ProductionRunRepository extends JpaRepository<ProductionRun, Long> {

    @EntityGraph(attributePaths = {
        "recipe",
        "recipe.manufacturedProduct",
        "recipe.items",
        "recipe.items.product",
        "recipe.items.product.baseUnit",
        "recipe.items.product.location",
        "recipe.items.unitOfMeasure",
        "manufacturedProduct",
        "laboratory",
        "createdBy",
        "confirmedBy",
        "inventoryMovement"
    })
    Optional<ProductionRun> findById(Long id);
}
