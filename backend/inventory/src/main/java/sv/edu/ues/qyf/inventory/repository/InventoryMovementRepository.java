package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByAttachmentDocumentId(Long documentId);

    List<InventoryMovement> findByLaboratoryIdOrderByIdDesc(Long laboratoryId);
}
