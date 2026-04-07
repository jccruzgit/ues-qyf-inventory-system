package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.UserLaboratory;

public interface UserLaboratoryRepository extends JpaRepository<UserLaboratory, Long> {

    List<UserLaboratory> findByUserIdAndActiveTrue(Long userId);

    List<UserLaboratory> findByLaboratoryIdAndActiveTrue(Long laboratoryId);

    Optional<UserLaboratory> findByUserIdAndLaboratoryId(Long userId, Long laboratoryId);

    boolean existsByUserIdAndLaboratoryIdAndActiveTrue(Long userId, Long laboratoryId);
}
