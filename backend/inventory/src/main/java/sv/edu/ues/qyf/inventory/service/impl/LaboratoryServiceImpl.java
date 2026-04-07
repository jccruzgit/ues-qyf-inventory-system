package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.LaboratoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.LaboratoryMapper;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;
import sv.edu.ues.qyf.inventory.service.LaboratoryService;

@Service
@Transactional
public class LaboratoryServiceImpl implements LaboratoryService {

    private static final String TABLE_NAME = "laboratories";

    private final LaboratoryRepository laboratoryRepository;
    private final LaboratoryMapper laboratoryMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final LaboratoryAccessService laboratoryAccessService;
    private final ObjectMapper objectMapper;

    public LaboratoryServiceImpl(
            LaboratoryRepository laboratoryRepository,
            LaboratoryMapper laboratoryMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            LaboratoryAccessService laboratoryAccessService,
            ObjectMapper objectMapper) {
        this.laboratoryRepository = laboratoryRepository;
        this.laboratoryMapper = laboratoryMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.laboratoryAccessService = laboratoryAccessService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratoryResponseDto> getAll() {
        List<Laboratory> laboratories = laboratoryAccessService.hasAccessToAllLaboratories()
                ? laboratoryRepository.findByActiveTrue()
                : laboratoryRepository.findByIdInAndActiveTrue(laboratoryAccessService.getAccessibleLaboratoryIds());

        return laboratories.stream()
                .map(laboratoryMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryResponseDto getById(Long id) {
        laboratoryAccessService.validateAccessToLaboratory(id);
        return laboratoryMapper.toResponseDto(getActiveLaboratory(id));
    }

    @Override
    public LaboratoryResponseDto deactivate(Long id) {
        laboratoryAccessService.validateAccessToLaboratory(id);
        Laboratory laboratory = getActiveLaboratory(id);
        User currentUser = currentUserService.getAuthenticatedUser();
        String oldValues = serializeState(laboratory);

        laboratory.setActive(Boolean.FALSE);
        laboratory.setDeletedAt(LocalDateTime.now());
        laboratory.setDeletedBy(currentUser);

        Laboratory savedLaboratory = laboratoryRepository.save(laboratory);
        auditLogService.logSoftDelete(
                TABLE_NAME,
                savedLaboratory.getId(),
                savedLaboratory.getId(),
                oldValues,
                serializeState(savedLaboratory),
                "Laboratory soft deleted");

        return laboratoryMapper.toResponseDto(savedLaboratory);
    }

    @Override
    public LaboratoryResponseDto restore(Long id) {
        Laboratory laboratory = getLaboratory(id);
        laboratoryAccessService.validateAccessToLaboratory(laboratory.getId());
        String oldValues = serializeState(laboratory);

        laboratory.setActive(Boolean.TRUE);
        laboratory.setDeletedAt(null);
        laboratory.setDeletedBy(null);

        Laboratory savedLaboratory = laboratoryRepository.save(laboratory);
        auditLogService.logRestore(
                TABLE_NAME,
                savedLaboratory.getId(),
                savedLaboratory.getId(),
                oldValues,
                serializeState(savedLaboratory),
                "Laboratory restored");

        return laboratoryMapper.toResponseDto(savedLaboratory);
    }

    private Laboratory getActiveLaboratory(Long id) {
        return laboratoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory not found with id: " + id));
    }

    private Laboratory getLaboratory(Long id) {
        return laboratoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Laboratory not found with id: " + id));
    }

    private String serializeState(Laboratory laboratory) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", laboratory.getId());
        state.put("active", laboratory.getActive());
        state.put("deletedAt", laboratory.getDeletedAt());
        state.put("deletedById", laboratory.getDeletedBy() != null ? laboratory.getDeletedBy().getId() : null);
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize laboratory audit state", exception);
        }
    }
}
