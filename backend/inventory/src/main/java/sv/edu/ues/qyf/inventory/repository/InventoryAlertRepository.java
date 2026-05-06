package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.InventoryAlert;
import sv.edu.ues.qyf.inventory.entity.InventoryAlertType;

public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Long> {

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByAcknowledgedAtIsNull();

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByAlertTypeAndAcknowledgedAtIsNull(InventoryAlertType alertType);

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByAcknowledgedById(Long userId);

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByLaboratoryIdAndAcknowledgedAtIsNullOrderByIdDesc(Long laboratoryId);

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByLaboratoryIdAndAlertTypeAndAcknowledgedAtIsNullOrderByIdDesc(
            Long laboratoryId, InventoryAlertType alertType);

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByLaboratoryIdOrderByTriggeredAtDescIdDesc(Long laboratoryId);

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByLaboratoryIdInAndAcknowledgedAtIsNullOrderByTriggeredAtDescIdDesc(List<Long> laboratoryIds);

    @EntityGraph(attributePaths = {"laboratory", "product", "product.location", "productBatch"})
    List<InventoryAlert> findByLaboratoryIdInOrderByTriggeredAtDescIdDesc(List<Long> laboratoryIds);

    Optional<InventoryAlert> findFirstByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
            Long laboratoryId, InventoryAlertType alertType, Long productId);

    Optional<InventoryAlert> findFirstByLaboratoryIdAndAlertTypeAndProductBatchIdAndAcknowledgedAtIsNull(
            Long laboratoryId, InventoryAlertType alertType, Long productBatchId);

    void deleteByLaboratoryIdAndAlertTypeAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
            Long laboratoryId, InventoryAlertType alertType, Long productId);

    void deleteByLaboratoryIdAndAlertTypeInAndProductIdAndProductBatchIsNullAndAcknowledgedAtIsNull(
            Long laboratoryId, List<InventoryAlertType> alertTypes, Long productId);

    void deleteByLaboratoryIdAndAlertTypeInAndProductBatchIdAndAcknowledgedAtIsNull(
            Long laboratoryId, List<InventoryAlertType> alertTypes, Long productBatchId);
}
