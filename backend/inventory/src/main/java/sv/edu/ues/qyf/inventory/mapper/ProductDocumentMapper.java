package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentResponseDto;
import sv.edu.ues.qyf.inventory.entity.ProductDocument;

@Component
public class ProductDocumentMapper {

    public ProductDocumentResponseDto toResponseDto(ProductDocument productDocument) {
        if (productDocument == null) {
            return null;
        }

        return ProductDocumentResponseDto.builder()
                .id(productDocument.getId())
                .productId(productDocument.getProduct() != null ? productDocument.getProduct().getId() : null)
                .fileName(productDocument.getFileName())
                .originalName(productDocument.getOriginalName())
                .fileType(productDocument.getFileType())
                .filePath(productDocument.getFilePath())
                .description(productDocument.getDescription())
                .uploadedById(productDocument.getUploadedBy() != null ? productDocument.getUploadedBy().getId() : null)
                .uploadedAt(productDocument.getUploadedAt())
                .active(productDocument.getActive())
                .deletedAt(productDocument.getDeletedAt())
                .deletedById(productDocument.getDeletedBy() != null ? productDocument.getDeletedBy().getId() : null)
                .build();
    }
}
