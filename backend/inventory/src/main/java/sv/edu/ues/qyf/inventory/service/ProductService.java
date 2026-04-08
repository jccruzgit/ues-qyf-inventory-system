package sv.edu.ues.qyf.inventory.service;

import java.util.List;
import sv.edu.ues.qyf.inventory.dto.ProductRequestDto;
import sv.edu.ues.qyf.inventory.dto.ProductResponseDto;
import sv.edu.ues.qyf.inventory.dto.ProductUpdateRequestDto;

public interface ProductService {

    ProductResponseDto create(ProductRequestDto request);

    List<ProductResponseDto> getAll();

    ProductResponseDto getById(Long id);

    ProductResponseDto update(Long id, ProductUpdateRequestDto request);

    ProductResponseDto deactivate(Long id);

    ProductResponseDto restore(Long id);
}
