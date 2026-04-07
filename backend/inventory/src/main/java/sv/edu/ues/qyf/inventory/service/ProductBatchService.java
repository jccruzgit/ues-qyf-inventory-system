package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;

public interface ProductBatchService {

    List<ProductBatchResponseDto> getActiveByProductAndLaboratory(Long productId, Long laboratoryId);

    ProductBatchResponseDto getById(Long id);

    ProductBatchResponseDto deactivate(Long id);

    ProductBatchResponseDto restore(Long id);
}
