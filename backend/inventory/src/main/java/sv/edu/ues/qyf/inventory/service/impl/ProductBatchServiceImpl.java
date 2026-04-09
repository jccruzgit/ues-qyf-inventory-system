package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.ProductBatchMapper;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;
import sv.edu.ues.qyf.inventory.service.ProductBatchService;

@Service
@Transactional
public class ProductBatchServiceImpl implements ProductBatchService {

    private static final String TABLE_NAME = "product_batches";

    private final ProductBatchRepository productBatchRepository;
    private final ProductBatchMapper productBatchMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final LaboratoryAccessService laboratoryAccessService;
    private final ObjectMapper objectMapper;

    public ProductBatchServiceImpl(
            ProductBatchRepository productBatchRepository,
            ProductBatchMapper productBatchMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            LaboratoryAccessService laboratoryAccessService,
            ObjectMapper objectMapper) {
        this.productBatchRepository = productBatchRepository;
        this.productBatchMapper = productBatchMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.laboratoryAccessService = laboratoryAccessService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchResponseDto> getActiveByProductAndLaboratory(Long productId, Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        return productBatchRepository
                .findByProductIdAndLaboratoryIdAndActiveTrueOrderByExpirationDateAscBatchCodeAsc(productId, laboratoryId)
                .stream()
                .map(productBatchMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBatchResponseDto getById(Long id) {
        ProductBatch productBatch = getActiveProductBatch(id);
        laboratoryAccessService.validateAccessToLaboratory(resolveLaboratoryId(productBatch));
        return productBatchMapper.toResponseDto(productBatch);
    }

    @Override
    public ProductBatchResponseDto deactivate(Long id) {
        ProductBatch productBatch = getActiveProductBatch(id);
        laboratoryAccessService.validateAccessToLaboratory(resolveLaboratoryId(productBatch));
        User currentUser = currentUserService.getAuthenticatedUser();
        String oldValues = serializeState(productBatch);

        productBatch.setActive(Boolean.FALSE);
        productBatch.setDeletedAt(LocalDateTime.now());
        productBatch.setDeletedBy(currentUser);

        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);
        auditLogService.logSoftDelete(
                TABLE_NAME,
                savedProductBatch.getId(),
                savedProductBatch.getLaboratory() != null ? savedProductBatch.getLaboratory().getId() : null,
                oldValues,
                serializeState(savedProductBatch),
                "Product batch soft deleted");

        return productBatchMapper.toResponseDto(savedProductBatch);
    }

    @Override
    public ProductBatchResponseDto restore(Long id) {
        ProductBatch productBatch = getProductBatch(id);
        laboratoryAccessService.validateAccessToLaboratory(resolveLaboratoryId(productBatch));
        String oldValues = serializeState(productBatch);

        productBatch.setActive(Boolean.TRUE);
        productBatch.setDeletedAt(null);
        productBatch.setDeletedBy(null);

        ProductBatch savedProductBatch = productBatchRepository.save(productBatch);
        auditLogService.logRestore(
                TABLE_NAME,
                savedProductBatch.getId(),
                savedProductBatch.getLaboratory() != null ? savedProductBatch.getLaboratory().getId() : null,
                oldValues,
                serializeState(savedProductBatch),
                "Product batch restored");

        return productBatchMapper.toResponseDto(savedProductBatch);
    }

    private ProductBatch getActiveProductBatch(Long id) {
        return productBatchRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + id));
    }

    private ProductBatch getProductBatch(Long id) {
        return productBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + id));
    }

    private String serializeState(ProductBatch productBatch) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", productBatch.getId());
        state.put("productId", productBatch.getProduct() != null ? productBatch.getProduct().getId() : null);
        state.put("laboratoryId", productBatch.getLaboratory() != null ? productBatch.getLaboratory().getId() : null);
        state.put("batchCode", productBatch.getBatchCode());
        state.put("status", productBatch.getStatus());
        state.put("expirationDate", productBatch.getExpirationDate());
        state.put("createdAt", productBatch.getCreatedAt());
        state.put("updatedAt", productBatch.getUpdatedAt());
        state.put("active", productBatch.getActive());
        state.put("deletedAt", productBatch.getDeletedAt());
        state.put("deletedById", productBatch.getDeletedBy() != null ? productBatch.getDeletedBy().getId() : null);
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize product batch audit state", exception);
        }
    }

    private Long resolveLaboratoryId(ProductBatch productBatch) {
        return productBatch.getLaboratory() != null ? productBatch.getLaboratory().getId() : null;
    }
}
