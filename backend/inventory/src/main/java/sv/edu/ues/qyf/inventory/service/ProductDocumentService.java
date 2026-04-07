package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductDocumentResponseDto;

public interface ProductDocumentService {

    ProductDocumentResponseDto create(ProductDocumentRequestDto request);

    List<ProductDocumentResponseDto> getActiveByProduct(Long productId);

    ProductDocumentResponseDto getById(Long id);

    ProductDocumentResponseDto deactivate(Long id);

    ProductDocumentResponseDto restore(Long id);
}
