package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.Laboratory;

public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {

    Optional<Laboratory> findByCode(String code);

    List<Laboratory> findByActiveTrue();

    List<Laboratory> findByIdInAndActiveTrue(List<Long> ids);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByIdAndActiveTrue(Long id);

    Optional<Laboratory> findByIdAndActiveTrue(Long id);
}
