package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;

public interface ManufacturedProductRepository extends JpaRepository<ManufacturedProduct, Long> {

    Optional<ManufacturedProduct> findByIdAndActiveTrue(Long id);

    Optional<ManufacturedProduct> findByCode(String code);

    List<ManufacturedProduct> findByActiveTrue(Sort sort);
}
