package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;

public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Long> {

    List<InventoryAlert> findByAcknowledgedAtIsNull();

    List<InventoryAlert> findByAcknowledgedById(Long userId);

    List<InventoryAlert> findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(Long laboratoryId);
}
