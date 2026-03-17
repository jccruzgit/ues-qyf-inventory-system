package sv.edu.ues.qyf.inventory.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
