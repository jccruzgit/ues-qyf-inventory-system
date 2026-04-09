package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;

public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Long> {

    List<InventoryAlert> findByAcknowledgedAtIsNull();

    List<InventoryAlert> findByAlertTypeAndAcknowledgedAtIsNull(InventoryAlertType alertType);

    List<InventoryAlert> findByAcknowledgedById(Long userId);

    List<InventoryAlert> findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(Long laboratoryId);

    List<InventoryAlert> findByLaboratoryIdAndAlertTypeAndAcknowledgedAtIsNullOrderByIdDesc(
            Long laboratoryId, InventoryAlertType alertType);

    List<InventoryAlert> findByLaboratoryIdOrderByTriggeredAtDescIdDesc(Long laboratoryId);

    Optional<InventoryAlert> findFirstByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
            Long laboratoryId, InventoryAlertType alertType, Long productId);

    Optional<InventoryAlert> findFirstByLaboratoryIdAndAlertTypeAndProductBatchIdAndAcknowledgedAtIsNull(
            Long laboratoryId, InventoryAlertType alertType, Long productBatchId);

    void deleteByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
            Long laboratoryId, InventoryAlertType alertType, Long productId);

    void deleteByLaboratoryIdAndAlertTypeInAndProductBatchIdAndAcknowledgedAtIsNull(
            Long laboratoryId, List<InventoryAlertType> alertTypes, Long productBatchId);
}
