package sv.edu.ues.qyf.inventory.service;

public interface AuditLogService {

    void logAction(String tableName, Long recordId, String action, Long laboratoryId, String oldValues, String newValues, String description);

    void logSoftDelete(String tableName, Long recordId, Long laboratoryId, String oldValues, String newValues, String description);

    void logRestore(String tableName, Long recordId, Long laboratoryId, String oldValues, String newValues, String description);
}
