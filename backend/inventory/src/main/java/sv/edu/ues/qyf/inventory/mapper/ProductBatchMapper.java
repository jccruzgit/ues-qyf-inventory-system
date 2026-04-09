package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;
import sv.edu.ues.qyf.inventory.entity.ProductBatch;

@Component
public class ProductBatchMapper {

    public ProductBatchResponseDto toResponseDto(ProductBatch productBatch) {
        if (productBatch == null) {
            return null;
        }

        return ProductBatchResponseDto.builder()
                .id(productBatch.getId())
                .productId(productBatch.getProduct() != null ? productBatch.getProduct().getId() : null)
                .laboratoryId(productBatch.getLaboratory() != null ? productBatch.getLaboratory().getId() : null)
                .batchCode(productBatch.getBatchCode())
                .certificateDocumentId(productBatch.getCertificateDocument() != null
                        ? productBatch.getCertificateDocument().getId()
                        : null)
                .status(productBatch.getStatus())
                .expirationDate(productBatch.getExpirationDate())
                .notes(productBatch.getNotes())
                .active(productBatch.getActive())
                .createdAt(productBatch.getCreatedAt())
                .updatedAt(productBatch.getUpdatedAt())
                .deletedAt(productBatch.getDeletedAt())
                .deletedById(productBatch.getDeletedBy() != null ? productBatch.getDeletedBy().getId() : null)
                .build();
    }
}
