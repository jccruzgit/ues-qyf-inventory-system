package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.UserLaboratoryRequestDto;
import sv.edu.ues.qyf.inventory.dto.UserLaboratoryResponseDto;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.entity.UserLaboratory;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.UserLaboratoryMapper;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.UserLaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.UserRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.UserLaboratoryService;

@Service
@Transactional
public class UserLaboratoryServiceImpl implements UserLaboratoryService {

    private static final String TABLE_NAME = "user_laboratories";
    private static final String ACTION_ASSIGN = "ASSIGN";

    private final UserLaboratoryRepository userLaboratoryRepository;
    private final UserRepository userRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final UserLaboratoryMapper userLaboratoryMapper;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public UserLaboratoryServiceImpl(
            UserLaboratoryRepository userLaboratoryRepository,
            UserRepository userRepository,
            LaboratoryRepository laboratoryRepository,
            UserLaboratoryMapper userLaboratoryMapper,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.userLaboratoryRepository = userLaboratoryRepository;
        this.userRepository = userRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.userLaboratoryMapper = userLaboratoryMapper;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserLaboratoryResponseDto assign(UserLaboratoryRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        Laboratory laboratory = laboratoryRepository.findById(request.getLaboratoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Laboratory not found with id: " + request.getLaboratoryId()));

        UserLaboratory assignment = userLaboratoryRepository.findByUserIdAndLaboratoryId(user.getId(), laboratory.getId())
                .orElseGet(() -> UserLaboratory.builder()
                        .user(user)
                        .laboratory(laboratory)
                        .build());

        String oldValues = assignment.getId() != null ? serializeState(assignment) : null;

        assignment.setUser(user);
        assignment.setLaboratory(laboratory);
        assignment.setAssignedAt(request.getAssignedAt() != null ? request.getAssignedAt() : LocalDateTime.now());
        assignment.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);

        UserLaboratory savedAssignment = userLaboratoryRepository.save(assignment);
        auditLogService.logAction(
                TABLE_NAME,
                savedAssignment.getId(),
                ACTION_ASSIGN,
                laboratory.getId(),
                oldValues,
                serializeState(savedAssignment),
                "User assigned to laboratory");

        return userLaboratoryMapper.toResponseDto(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserLaboratoryResponseDto> getActiveAssignmentsByUser(Long userId) {
        return userLaboratoryRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(userLaboratoryMapper::toResponseDto)
                .toList();
    }

    private String serializeState(UserLaboratory userLaboratory) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", userLaboratory.getId());
        state.put("userId", userLaboratory.getUser() != null ? userLaboratory.getUser().getId() : null);
        state.put("laboratoryId", userLaboratory.getLaboratory() != null ? userLaboratory.getLaboratory().getId() : null);
        state.put("assignedAt", userLaboratory.getAssignedAt());
        state.put("active", userLaboratory.getActive());
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize user laboratory audit state", exception);
        }
    }
}
