package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.ProductBatchOverviewResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductBatchResponseDto;

public interface ProductBatchService {

    List<ProductBatchOverviewResponseDto> getOverview(Long productId, Long laboratoryId);

    List<ProductBatchResponseDto> getActiveByProductAndLaboratory(Long productId, Long laboratoryId);

    ProductBatchResponseDto getById(Long id);

    ProductBatchResponseDto deactivate(Long id);

    ProductBatchResponseDto restore(Long id);
}
