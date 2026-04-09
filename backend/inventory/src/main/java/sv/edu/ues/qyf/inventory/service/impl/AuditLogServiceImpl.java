package sv.edu.ues.qyf.inventory.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.entity.AuditLog;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.repository.AuditLogRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private static final String ACTION_SOFT_DELETE = "SOFT_DELETE";
    private static final String ACTION_RESTORE = "RESTORE";

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public void logAction(
            String tableName,
            Long recordId,
            String action,
            Long laboratoryId,
            String oldValues,
            String newValues,
            String description) {
        saveAuditLog(tableName, recordId, action, laboratoryId, oldValues, newValues, description);
    }

    @Override
    public void logSoftDelete(
            String tableName,
            Long recordId,
            Long laboratoryId,
            String oldValues,
            String newValues,
            String description) {
        saveAuditLog(tableName, recordId, ACTION_SOFT_DELETE, laboratoryId, oldValues, newValues, description);
    }

    @Override
    public void logRestore(
            String tableName,
            Long recordId,
            Long laboratoryId,
            String oldValues,
            String newValues,
            String description) {
        saveAuditLog(tableName, recordId, ACTION_RESTORE, laboratoryId, oldValues, newValues, description);
    }

    private void saveAuditLog(
            String tableName,
            Long recordId,
            String action,
            Long laboratoryId,
            String oldValues,
            String newValues,
            String description) {
        try {
            User changedBy = currentUserService.getAuthenticatedUserOrNull();
            if (changedBy == null) {
                LOGGER.debug(
                        "Skipping audit log for table={} recordId={} action={} because there is no authenticated user",
                        tableName,
                        recordId,
                        action);
                return;
            }

            AuditLog auditLog = AuditLog.builder()
                    .tableName(tableName)
                    .recordId(recordId)
                    .action(action)
                    .changedBy(changedBy)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .description(description)
                    .laboratory(laboratoryId != null ? Laboratory.builder().id(laboratoryId).build() : null)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception exception) {
            LOGGER.warn(
                    "Audit log registration failed for table={} recordId={} action={}",
                    tableName,
                    recordId,
                    action,
                    exception);
        }
    }
}
