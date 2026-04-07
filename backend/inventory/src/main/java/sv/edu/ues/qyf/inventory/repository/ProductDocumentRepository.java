package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.ProductDocument;

public interface ProductDocumentRepository extends JpaRepository<ProductDocument, Long> {

    List<ProductDocument> findByProductIdAndActiveTrueOrderByUploadedAtDesc(Long productId);

    Optional<ProductDocument> findByIdAndActiveTrue(Long id);
}
