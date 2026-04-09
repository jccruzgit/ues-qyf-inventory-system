package sv.edu.ues.qyf.inventory.service.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.InventoryMovementLine;
import sv.edu.ues.qyf.inventory.entity.MovementType;
import sv.edu.ues.qyf.inventory.entity.Product;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class InventoryStockServiceImpl implements InventoryStockService {

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final LaboratoryAccessService laboratoryAccessService;

    public InventoryStockServiceImpl(
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            InventoryMovementRepository inventoryMovementRepository,
            LaboratoryAccessService laboratoryAccessService) {
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.laboratoryAccessService = laboratoryAccessService;
    }

    @Override
    public List<InventoryStockResponseDto> getStock(Long productId, Long laboratoryId, Long productBatchId) {
        if (productBatchId != null) {
            return getBatchStock(productId, laboratoryId, productBatchId);
        }
        if (laboratoryId != null) {
            return getLaboratoryStock(productId, laboratoryId);
        }
        return getGlobalProductStock(productId);
    }

    private List<InventoryStockResponseDto> getGlobalProductStock(Long productId) {
        List<Product> products;
        if (productId != null) {
            products = List.of(productRepository.findByIdAndActiveTrue(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId)));
        } else {
            products = productRepository.findByActiveTrue(org.springframework.data.domain.Sort.by("name"));
        }

        return products.stream()
                .map(product -> InventoryStockResponseDto.builder()
                        .productId(product.getId())
                        .productCode(product.getCode())
                        .productName(product.getName())
                        .quantityAvailable(product.getCurrentStock())
                        .minimumStock(product.getMinimumStock())
                        .lowStock(isLowStock(product.getCurrentStock(), product.getMinimumStock()))
                        .build())
                .toList();
    }

    private List<InventoryStockResponseDto> getLaboratoryStock(Long productId, Long laboratoryId) {
        laboratoryAccessService.validateAccessToLaboratory(laboratoryId);
        List<InventoryMovement> movements = inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(laboratoryId);

        Map<String, InventoryStockAccumulator> stockMap = new LinkedHashMap<>();
        for (InventoryMovement movement : movements) {
            for (InventoryMovementLine line : movement.getLines()) {
                if (productId != null && !line.getProduct().getId().equals(productId)) {
                    continue;
                }
                String key = line.getProduct().getId() + ":" + movement.getLaboratory().getId() + ":"
                        + (line.getProductBatch() != null ? line.getProductBatch().getId() : "NO_BATCH");
                InventoryStockAccumulator accumulator = stockMap.computeIfAbsent(
                        key,
                        ignored -> InventoryStockAccumulator.fromLine(movement, line));
                stockMap.put(key, accumulator.apply(movement.getMovementType(), line.getQuantity()));
            }
        }

        return stockMap.values().stream()
                .filter(InventoryStockAccumulator::hasStock)
                .sorted(Comparator.comparing(InventoryStockAccumulator::productName)
                        .thenComparing(InventoryStockAccumulator::batchCode, Comparator.nullsLast(String::compareTo)))
                .map(InventoryStockAccumulator::toResponse)
                .toList();
    }

    private List<InventoryStockResponseDto> getBatchStock(Long productId, Long laboratoryId, Long productBatchId) {
        var batch = productBatchRepository.findByIdAndActiveTrue(productBatchId)
                .orElseThrow(() -> new ResourceNotFoundException("Product batch not found with id: " + productBatchId));
        laboratoryAccessService.validateAccessToLaboratory(batch.getLaboratory().getId());

        if (productId != null && !batch.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product batch does not belong to product id: " + productId);
        }
        if (laboratoryId != null && !batch.getLaboratory().getId().equals(laboratoryId)) {
            throw new ResourceNotFoundException("Product batch does not belong to laboratory id: " + laboratoryId);
        }

        InventoryStockAccumulator accumulator = InventoryStockAccumulator.fromBatch(batch);
        List<InventoryMovement> movements =
                inventoryMovementRepository.findByLaboratoryIdOrderByPerformedAtDescIdDesc(batch.getLaboratory().getId());

        for (InventoryMovement movement : movements) {
            for (InventoryMovementLine line : movement.getLines()) {
                if (line.getProductBatch() != null && line.getProductBatch().getId().equals(productBatchId)) {
                    accumulator = accumulator.apply(movement.getMovementType(), line.getQuantity());
                }
            }
        }

        if (!accumulator.hasStock()) {
            return List.of(accumulator.toResponse());
        }
        return List.of(accumulator.toResponse());
    }

    private boolean isLowStock(BigDecimal available, BigDecimal minimumStock) {
        return available != null
                && minimumStock != null
                && available.compareTo(minimumStock) < 0;
    }

    private record InventoryStockAccumulator(
            Long productId,
            String productCode,
            String productName,
            Long laboratoryId,
            String laboratoryCode,
            String laboratoryName,
            Long productBatchId,
            String batchCode,
            java.time.LocalDate expirationDate,
            BigDecimal minimumStock,
            BigDecimal quantityAvailable) {

        private static InventoryStockAccumulator fromLine(InventoryMovement movement, InventoryMovementLine line) {
            return new InventoryStockAccumulator(
                    line.getProduct().getId(),
                    line.getProduct().getCode(),
                    line.getProduct().getName(),
                    movement.getLaboratory().getId(),
                    movement.getLaboratory().getCode(),
                    movement.getLaboratory().getName(),
                    line.getProductBatch() != null ? line.getProductBatch().getId() : null,
                    line.getProductBatch() != null ? line.getProductBatch().getBatchCode() : null,
                    line.getProductBatch() != null ? line.getProductBatch().getExpirationDate() : null,
                    line.getProduct().getMinimumStock(),
                    BigDecimal.ZERO);
        }

        private static InventoryStockAccumulator fromBatch(sv.edu.ues.qyf.inventory.entity.ProductBatch batch) {
            return new InventoryStockAccumulator(
                    batch.getProduct().getId(),
                    batch.getProduct().getCode(),
                    batch.getProduct().getName(),
                    batch.getLaboratory().getId(),
                    batch.getLaboratory().getCode(),
                    batch.getLaboratory().getName(),
                    batch.getId(),
                    batch.getBatchCode(),
                    batch.getExpirationDate(),
                    batch.getProduct().getMinimumStock(),
                    BigDecimal.ZERO);
        }

        private InventoryStockAccumulator apply(MovementType movementType, BigDecimal quantity) {
            BigDecimal adjusted = movementType == MovementType.ENTRY
                    ? quantityAvailable.add(quantity)
                    : quantityAvailable.subtract(quantity);
            return new InventoryStockAccumulator(
                    productId,
                    productCode,
                    productName,
                    laboratoryId,
                    laboratoryCode,
                    laboratoryName,
                    productBatchId,
                    batchCode,
                    expirationDate,
                    minimumStock,
                    adjusted);
        }

        private boolean hasStock() {
            return quantityAvailable.compareTo(BigDecimal.ZERO) != 0;
        }

        private InventoryStockResponseDto toResponse() {
            return InventoryStockResponseDto.builder()
                    .productId(productId)
                    .productCode(productCode)
                    .productName(productName)
                    .laboratoryId(laboratoryId)
                    .laboratoryCode(laboratoryCode)
                    .laboratoryName(laboratoryName)
                    .productBatchId(productBatchId)
                    .batchCode(batchCode)
                    .expirationDate(expirationDate)
                    .quantityAvailable(quantityAvailable)
                    .minimumStock(minimumStock)
                    .lowStock(minimumStock != null && quantityAvailable.compareTo(minimumStock) < 0)
                    .build();
        }
    }
}
