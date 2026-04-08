package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product"})
    List<InventoryMovement> findByAttachmentDocumentId(Long documentId);

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product"})
    List<InventoryMovement> findByLaboratoryIdOrderByPerformedAtDescIdDesc(Long laboratoryId);

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product"})
    List<InventoryMovement> findAllByOrderByPerformedAtDescIdDesc();

    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product"})
    List<InventoryMovement> findByLaboratoryIdInOrderByPerformedAtDescIdDesc(List<Long> laboratoryIds);

    @Override
    @EntityGraph(attributePaths = {"laboratory", "performedBy", "attachmentDocument", "lines", "lines.product"})
    Optional<InventoryMovement> findById(Long id);
}
