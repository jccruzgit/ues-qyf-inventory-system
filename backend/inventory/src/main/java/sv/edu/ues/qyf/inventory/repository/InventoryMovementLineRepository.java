package sv.edu.ues.qyf.inventory.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.MovementType;

public interface InventoryMovementLineRepository extends JpaRepository<InventoryMovementLine, Long> {

    List<InventoryMovementLine> findByMovementIdOrderByIdAsc(Long movementId);

    @Query(
            """
            select coalesce(sum(
                case
                    when m.movementType = :entryType then l.quantity
                    else -l.quantity
                end
            ), 0)
            from InventoryMovementLine l
            join l.movement m
            where l.productBatch.id = :productBatchId
            """)
    BigDecimal calculateCurrentStockByBatchId(
            @Param("productBatchId") Long productBatchId, @Param("entryType") MovementType entryType);

    @Query(
            """
            select distinct l.product.id
            from InventoryMovementLine l
            join l.movement m
            where m.laboratory.id = :laboratoryId
            """)
    List<Long> findDistinctProductIdsByLaboratoryId(@Param("laboratoryId") Long laboratoryId);
}
