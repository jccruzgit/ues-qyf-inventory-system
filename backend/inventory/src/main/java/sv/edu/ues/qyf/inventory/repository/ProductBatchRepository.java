package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    List<ProductBatch> findByProductIdAndLaboratoryIdAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(
            Long productId, Long laboratoryId);

    List<ProductBatch> findByLaboratoryIdAndActiveTrueOrderByExpirationDateAsc(Long laboratoryId);

    List<ProductBatch> findByLaboratoryIdAndExpirationDateLessThanEqualAndActiveTrueOrderByExpirationDateAsc(
            Long laboratoryId, java.time.LocalDate expirationDate);

    List<ProductBatch> findByLaboratoryIdAndExpirationDateIsNotNullAndActiveTrueOrderByExpirationDateAsc(Long laboratoryId);

    Optional<ProductBatch> findByProductIdAndLaboratoryIdAndBatchCode(Long productId, Long laboratoryId, String batchCode);

    Optional<ProductBatch> findByIdAndActiveTrue(Long id);
}
