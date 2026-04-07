package sv.edu.ues.qyf.inventory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import sv.edu.ues.qyf.inventory.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByTableNameAndRecordIdOrderByChangedAtDesc(String tableName, Long recordId);

    List<AuditLog> findByChangedByIdOrderByChangedAtDesc(Long userId);

    List<AuditLog> findByLaboratoryIdOrderByChangedAtDesc(Long laboratoryId);
}
