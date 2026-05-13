package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ManufacturedProductResponseDto;
import sv.edu.ues.qyf.inventory.entity.ManufacturedProduct;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.ManufacturedProductMapper;
import sv.edu.ues.qyf.inventory.repository.ManufacturedProductRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.ManufacturedProductService;

@Service
@Transactional
public class ManufacturedProductServiceImpl implements ManufacturedProductService {

    private static final String TABLE_NAME = "manufactured_products";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";

    private final ManufacturedProductRepository manufacturedProductRepository;
    private final ManufacturedProductMapper manufacturedProductMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ManufacturedProductServiceImpl(
            ManufacturedProductRepository manufacturedProductRepository,
            ManufacturedProductMapper manufacturedProductMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.manufacturedProductRepository = manufacturedProductRepository;
        this.manufacturedProductMapper = manufacturedProductMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ManufacturedProductResponseDto create(ManufacturedProductRequestDto request) {
        String code = normalize(request.getCode());
        validateDuplicateCode(code, null);

        ManufacturedProduct manufacturedProduct = manufacturedProductMapper.toEntity(request);
        manufacturedProduct.setCode(code);
        manufacturedProduct.setName(normalize(request.getName()));
        manufacturedProduct.setDescription(normalizeNullable(request.getDescription()));
        manufacturedProduct.setActive(resolveActive(request.getActive(), Boolean.TRUE));

        ManufacturedProduct savedManufacturedProduct = manufacturedProductRepository.save(manufacturedProduct);
        auditLogService.logAction(
                TABLE_NAME,
                savedManufacturedProduct.getId(),
                ACTION_CREATE,
                null,
                null,
                serializeState(savedManufacturedProduct),
                "Manufactured product created");

        return manufacturedProductMapper.toResponseDto(savedManufacturedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturedProductResponseDto> getAll() {
        return manufacturedProductRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(manufacturedProductMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturedProductResponseDto getById(Long id) {
        return manufacturedProductMapper.toResponseDto(getActiveManufacturedProduct(id));
    }

    @Override
    public ManufacturedProductResponseDto update(Long id, ManufacturedProductRequestDto request) {
        ManufacturedProduct manufacturedProduct = getActiveManufacturedProduct(id);
        String oldValues = serializeState(manufacturedProduct);
        String code = normalize(request.getCode());
        validateDuplicateCode(code, id);

        manufacturedProductMapper.updateEntity(manufacturedProduct, request);
        manufacturedProduct.setCode(code);
        manufacturedProduct.setName(normalize(request.getName()));
        manufacturedProduct.setDescription(normalizeNullable(request.getDescription()));
        manufacturedProduct.setActive(resolveActive(request.getActive(), manufacturedProduct.getActive()));

        if (Boolean.TRUE.equals(manufacturedProduct.getActive())) {
            manufacturedProduct.setDeletedAt(null);
            manufacturedProduct.setDeletedBy(null);
        } else if (manufacturedProduct.getDeletedAt() == null) {
            manufacturedProduct.setDeletedAt(LocalDateTime.now());
            manufacturedProduct.setDeletedBy(currentUserService.getAuthenticatedUser());
        }

        ManufacturedProduct savedManufacturedProduct = manufacturedProductRepository.save(manufacturedProduct);
        auditLogService.logAction(
                TABLE_NAME,
                savedManufacturedProduct.getId(),
                ACTION_UPDATE,
                null,
                oldValues,
                serializeState(savedManufacturedProduct),
                "Manufactured product updated");

        return manufacturedProductMapper.toResponseDto(savedManufacturedProduct);
    }

    private ManufacturedProduct getActiveManufacturedProduct(Long id) {
        return manufacturedProductRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufactured product not found with id: " + id));
    }

    private void validateDuplicateCode(String code, Long currentId) {
        manufacturedProductRepository.findByCode(code)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Manufactured product code already exists: " + code);
                });
    }

    private Boolean resolveActive(Boolean requestedActive, Boolean currentValue) {
        return requestedActive != null ? requestedActive : currentValue;
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String serializeState(ManufacturedProduct manufacturedProduct) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", manufacturedProduct.getId());
        state.put("code", manufacturedProduct.getCode());
        state.put("name", manufacturedProduct.getName());
        state.put("description", manufacturedProduct.getDescription());
        state.put("active", manufacturedProduct.getActive());
        state.put("deletedAt", manufacturedProduct.getDeletedAt());
        state.put("deletedById", manufacturedProduct.getDeletedBy() != null
                ? manufacturedProduct.getDeletedBy().getId()
                : null);
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize manufactured product audit state", exception);
        }
    }
}
