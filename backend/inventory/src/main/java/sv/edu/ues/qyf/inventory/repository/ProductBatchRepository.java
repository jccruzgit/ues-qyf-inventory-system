package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    @EntityGraph(attributePaths = {"product", "product.baseUnit", "product.location", "laboratory"})
    List<ProductBatch> findByActiveTrueOrderByExpirationDateAscBatchCodeAsc();

    @EntityGraph(attributePaths = {"product", "product.baseUnit", "product.location", "laboratory"})
    List<ProductBatch> findByProductIdAndLaboratoryIdAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(
            Long productId, Long laboratoryId);

    @EntityGraph(attributePaths = {"product", "product.baseUnit", "product.location", "laboratory"})
    List<ProductBatch> findByLaboratoryIdAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(Long laboratoryId);

    @EntityGraph(attributePaths = {"product", "product.baseUnit", "product.location", "laboratory"})
    List<ProductBatch> findByLaboratoryIdInAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(List<Long> laboratoryIds);

    List<ProductBatch> findByLaboratoryIdAndExpirationDateLessThanEqualAndActiveTrueOrderByExpirationDateAsc(
            Long laboratoryId, java.time.LocalDate expirationDate);

    List<ProductBatch> findByLaboratoryIdAndExpirationDateIsNotNullAndActiveTrueOrderByExpirationDateAsc(Long laboratoryId);

    Optional<ProductBatch> findByProductIdAndLaboratoryIdAndBatchCode(Long productId, Long laboratoryId, String batchCode);

    Optional<ProductBatch> findByIdAndActiveTrue(Long id);
}
