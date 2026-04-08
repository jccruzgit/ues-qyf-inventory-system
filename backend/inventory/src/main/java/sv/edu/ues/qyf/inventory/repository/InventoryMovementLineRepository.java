package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;

public interface InventoryMovementLineRepository extends JpaRepository<InventoryMovementLine, Long> {

    List<InventoryMovementLine> findByMovementIdOrderByIdAsc(Long movementId);
}
