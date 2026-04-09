package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.MovementType;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product", "lines.productBatch"})
    List<InventoryMovement> findByAttachmentDocumentId(Long documentId);

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product", "lines.productBatch"})
    List<InventoryMovement> findByLaboratoryIdOrderByPerformedAtDescIdDesc(Long laboratoryId);

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product", "lines.productBatch"})
    List<InventoryMovement> findAllByOrderByPerformedAtDescIdDesc();

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product", "lines.productBatch"})
    List<InventoryMovement> findByLaboratoryIdInOrderByPerformedAtDescIdDesc(List<Long> laboratoryIds);

    @Override
    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product", "lines.productBatch"})
    Optional<InventoryMovement> findById(Long id);

    @Query(
            """
            select distinct m
            from InventoryMovement m
            left join fetch m.laboratory
            left join fetch m.performedBy
            left join fetch m.attachmentDocument
            left join fetch m.lines l
            left join fetch l.product
            left join fetch l.productBatch
            where (:laboratoryId is null or m.laboratory.id = :laboratoryId)
              and (:movementType is null or m.movementType = :movementType)
              and (:performedFrom is null or m.performedAt >= :performedFrom)
              and (:performedTo is null or m.performedAt < :performedTo)
              and (:productId is null or exists (
                    select 1
                    from InventoryMovementLine lx
                    where lx.movement = m
                      and lx.product.id = :productId
              ))
            order by m.performedAt desc, m.id desc
            """)
    List<InventoryMovement> search(
            @Param("productId") Long productId,
            @Param("laboratoryId") Long laboratoryId,
            @Param("movementType") MovementType movementType,
            @Param("performedFrom") java.time.LocalDateTime performedFrom,
            @Param("performedTo") java.time.LocalDateTime performedTo);

    @Query(
            """
            select distinct m
            from InventoryMovement m
            left join fetch m.laboratory
            left join fetch m.performedBy
            left join fetch m.attachmentDocument
            left join fetch m.lines l
            left join fetch l.product
            left join fetch l.productBatch
            where m.laboratory.id in :laboratoryIds
              and (:movementType is null or m.movementType = :movementType)
              and (:performedFrom is null or m.performedAt >= :performedFrom)
              and (:performedTo is null or m.performedAt < :performedTo)
              and (:productId is null or exists (
                    select 1
                    from InventoryMovementLine lx
                    where lx.movement = m
                      and lx.product.id = :productId
              ))
            order by m.performedAt desc, m.id desc
            """)
    List<InventoryMovement> searchByLaboratoryIds(
            @Param("laboratoryIds") List<Long> laboratoryIds,
            @Param("productId") Long productId,
            @Param("movementType") MovementType movementType,
            @Param("performedFrom") java.time.LocalDateTime performedFrom,
            @Param("performedTo") java.time.LocalDateTime performedTo);
}
