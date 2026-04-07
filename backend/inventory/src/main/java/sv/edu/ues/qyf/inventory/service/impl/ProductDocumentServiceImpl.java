package sv.edu.ues.qyf.inventory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentResponseDto;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.entity.ProductDocument;
import sv.edu.ues.qyf.inventory.entity.User;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.ProductDocumentMapper;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.repository.ProductDocumentRepository;
import sv.edu.ues.qyf.inventory.repository.UserRepository;
import sv.edu.ues.qyf.inventory.service.AuditLogService;
import sv.edu.ues.qyf.inventory.service.CurrentUserService;
import sv.edu.ues.qyf.inventory.service.ProductDocumentService;

@Service
@Transactional
public class ProductDocumentServiceImpl implements ProductDocumentService {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of("PDF", "JPG", "JPEG", "PNG");
    private static final String ACTION_CREATE = "CREATE";
    private static final String TABLE_NAME = "product_documents";

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final UserRepository userRepository;
    private final ProductDocumentMapper productDocumentMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public ProductDocumentServiceImpl(
            ProductRepository productRepository,
            ProductDocumentRepository productDocumentRepository,
            UserRepository userRepository,
            ProductDocumentMapper productDocumentMapper,
            CurrentUserService currentUserService,
            AuditLogService auditLogService,
            ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
        this.userRepository = userRepository;
        this.productDocumentMapper = productDocumentMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductDocumentResponseDto create(ProductDocumentRequestDto request) {
        Product product = productRepository.findByIdAndActiveTrue(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        User uploadedBy = userRepository.findById(request.getUploadedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUploadedById()));
        String fileType = normalizeFileType(request.getFileType());

        ProductDocument productDocument = ProductDocument.builder()
                .product(product)
                .fileName(request.getFileName().trim())
                .originalName(request.getOriginalName().trim())
                .fileType(fileType)
                .filePath(request.getFilePath().trim())
                .description(normalizeNullable(request.getDescription()))
                .uploadedBy(uploadedBy)
                .active(request.getActive() != null ? request.getActive() : Boolean.TRUE)
                .build();

        ProductDocument savedProductDocument = productDocumentRepository.save(productDocument);
        auditLogService.logAction(
                TABLE_NAME,
                savedProductDocument.getId(),
                ACTION_CREATE,
                null,
                null,
                serializeState(savedProductDocument),
                "Product document uploaded");

        return productDocumentMapper.toResponseDto(savedProductDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDocumentResponseDto> getActiveByProduct(Long productId) {
        return productDocumentRepository.findByProductIdAndActiveTrueOrderByUploadedAtDesc(productId).stream()
                .map(productDocumentMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDocumentResponseDto getById(Long id) {
        return productDocumentMapper.toResponseDto(getActiveProductDocument(id));
    }

    @Override
    public ProductDocumentResponseDto deactivate(Long id) {
        ProductDocument productDocument = getActiveProductDocument(id);
        User currentUser = currentUserService.getAuthenticatedUser();
        String oldValues = serializeState(productDocument);

        productDocument.setActive(Boolean.FALSE);
        productDocument.setDeletedAt(LocalDateTime.now());
        productDocument.setDeletedBy(currentUser);

        ProductDocument savedProductDocument = productDocumentRepository.save(productDocument);
        auditLogService.logSoftDelete(
                TABLE_NAME,
                savedProductDocument.getId(),
                null,
                oldValues,
                serializeState(savedProductDocument),
                "Product document soft deleted");

        return productDocumentMapper.toResponseDto(savedProductDocument);
    }

    @Override
    public ProductDocumentResponseDto restore(Long id) {
        ProductDocument productDocument = getProductDocument(id);
        String oldValues = serializeState(productDocument);

        productDocument.setActive(Boolean.TRUE);
        productDocument.setDeletedAt(null);
        productDocument.setDeletedBy(null);

        ProductDocument savedProductDocument = productDocumentRepository.save(productDocument);
        auditLogService.logRestore(
                TABLE_NAME,
                savedProductDocument.getId(),
                null,
                oldValues,
                serializeState(savedProductDocument),
                "Product document restored");

        return productDocumentMapper.toResponseDto(savedProductDocument);
    }

    private ProductDocument getActiveProductDocument(Long id) {
        return productDocumentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product document not found with id: " + id));
    }

    private ProductDocument getProductDocument(Long id) {
        return productDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product document not found with id: " + id));
    }

    private String serializeState(ProductDocument productDocument) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("id", productDocument.getId());
        state.put("productId", productDocument.getProduct() != null ? productDocument.getProduct().getId() : null);
        state.put("fileName", productDocument.getFileName());
        state.put("active", productDocument.getActive());
        state.put("deletedAt", productDocument.getDeletedAt());
        state.put("deletedById", productDocument.getDeletedBy() != null ? productDocument.getDeletedBy().getId() : null);
        return writeJson(state);
    }

    private String writeJson(Map<String, Object> state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize product document audit state", exception);
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeFileType(String fileType) {
        String normalized = fileType.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }

        if (!ALLOWED_FILE_TYPES.contains(normalized)) {
            throw new BadRequestException("File type not allowed. Allowed types: PDF, JPG, JPEG, PNG");
        }

        return normalized;
    }
}
