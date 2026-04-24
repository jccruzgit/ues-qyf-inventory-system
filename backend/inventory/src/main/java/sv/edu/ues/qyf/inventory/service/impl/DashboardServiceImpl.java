package sv.edu.ues.qyf.inventory.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.DashboardLaboratorySummaryDto;
import sv.edu.ues.qyf.inventory.dto.DashboardMovementSeriesPointDto;
import sv.edu.ues.qyf.inventory.dto.DashboardRecentMovementDto;
import sv.edu.ues.qyf.inventory.dto.DashboardSummaryResponseDto;
import sv.edu.ues.qyf.inventory.dto.InventoryStockResponseDto;
import sv.edu.ues.qyf.inventory.entity.InventoryMovement;
import sv.edu.ues.qyf.inventory.entity.Laboratory;
import sv.edu.ues.qyf.inventory.repository.InventoryMovementRepository;
import sv.edu.ues.qyf.inventory.repository.LaboratoryRepository;
import sv.edu.ues.qyf.inventory.repository.ProductBatchRepository;
import sv.edu.ues.qyf.inventory.repository.ProductRepository;
import sv.edu.ues.qyf.inventory.service.DashboardService;
import sv.edu.ues.qyf.inventory.service.InventoryStockService;
import sv.edu.ues.qyf.inventory.service.LaboratoryAccessService;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int MOVEMENT_SERIES_DAYS = 7;
    private static final int RECENT_MOVEMENTS_LIMIT = 5;
    private static final int EXPIRING_BATCH_WINDOW_DAYS = 30;

    private final ProductRepository productRepository;
    private final ProductBatchRepository productBatchRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final InventoryStockService inventoryStockService;
    private final LaboratoryAccessService laboratoryAccessService;

    public DashboardServiceImpl(
            ProductRepository productRepository,
            ProductBatchRepository productBatchRepository,
            InventoryMovementRepository inventoryMovementRepository,
            LaboratoryRepository laboratoryRepository,
            InventoryStockService inventoryStockService,
            LaboratoryAccessService laboratoryAccessService) {
        this.productRepository = productRepository;
        this.productBatchRepository = productBatchRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.laboratoryRepository = laboratoryRepository;
        this.inventoryStockService = inventoryStockService;
        this.laboratoryAccessService = laboratoryAccessService;
    }

    @Override
    public DashboardSummaryResponseDto getSummary() {
        List<Laboratory> laboratories = getAccessibleLaboratories();
        Map<Long, List<InventoryStockResponseDto>> stockByLaboratory = loadStockByLaboratory(laboratories);
        LocalDate today = LocalDate.now();
        List<InventoryMovement> lastSevenDaysMovements = findMovements(
                laboratories,
                today.minusDays(MOVEMENT_SERIES_DAYS - 1).atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        List<InventoryMovement> recentMovements = findMovements(laboratories, null, null);

        return DashboardSummaryResponseDto.builder()
                .totalActiveProducts((long)
                        productRepository.findByActiveTrue(Sort.by("name")).size())
                .lowStockProducts(countLowStockProducts(stockByLaboratory))
                .expiringBatches(countExpiringBatches(laboratories, today))
                .accessibleLaboratories((long) laboratories.size())
                .movementsLastSevenDays((long) lastSevenDaysMovements.size())
                .movementSeries(buildMovementSeries(lastSevenDaysMovements, today))
                .recentMovements(buildRecentMovements(recentMovements))
                .inventoryByLaboratory(buildLaboratorySummaries(laboratories, stockByLaboratory, today))
                .build();
    }

    private List<Laboratory> getAccessibleLaboratories() {
        List<Laboratory> laboratories = laboratoryAccessService.hasAccessToAllLaboratories()
                ? laboratoryRepository.findByActiveTrue()
                : laboratoryRepository.findByIdInAndActiveTrue(laboratoryAccessService.getAccessibleLaboratoryIds());

        return laboratories.stream()
                .sorted(java.util.Comparator.comparing(Laboratory::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Map<Long, List<InventoryStockResponseDto>> loadStockByLaboratory(List<Laboratory> laboratories) {
        Map<Long, List<InventoryStockResponseDto>> stockByLaboratory = new LinkedHashMap<>();
        for (Laboratory laboratory : laboratories) {
            stockByLaboratory.put(laboratory.getId(), inventoryStockService.getStock(null, laboratory.getId(), null));
        }
        return stockByLaboratory;
    }

    private List<InventoryMovement> findMovements(
            List<Laboratory> laboratories, LocalDateTime performedFrom, LocalDateTime performedTo) {
        if (laboratories.isEmpty()) {
            return List.of();
        }

        if (laboratoryAccessService.hasAccessToAllLaboratories()) {
            return inventoryMovementRepository.search(null, null, null, performedFrom, performedTo);
        }

        return inventoryMovementRepository.searchByLaboratoryIds(
                laboratories.stream().map(Laboratory::getId).toList(),
                null,
                null,
                performedFrom,
                performedTo);
    }

    private long countLowStockProducts(Map<Long, List<InventoryStockResponseDto>> stockByLaboratory) {
        return stockByLaboratory.values().stream()
                .flatMap(List::stream)
                .filter(item -> Boolean.TRUE.equals(item.getLowStock()))
                .map(InventoryStockResponseDto::getProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();
    }

    private long countExpiringBatches(List<Laboratory> laboratories, LocalDate today) {
        return laboratories.stream()
                .flatMap(laboratory -> productBatchRepository
                        .findByLaboratoryIdAndExpirationDateLessThanEqualAndActiveTrueOrderByExpirationDateAsc(
                                laboratory.getId(), today.plusDays(EXPIRING_BATCH_WINDOW_DAYS))
                        .stream())
                .filter(batch -> batch.getExpirationDate() != null && !batch.getExpirationDate().isBefore(today))
                .map(batch -> batch.getId())
                .distinct()
                .count();
    }

    private List<DashboardMovementSeriesPointDto> buildMovementSeries(
            List<InventoryMovement> movements, LocalDate today) {
        Map<LocalDate, MovementPointAccumulator> points = new LinkedHashMap<>();

        for (int dayOffset = MOVEMENT_SERIES_DAYS - 1; dayOffset >= 0; dayOffset--) {
            LocalDate date = today.minusDays(dayOffset);
            points.put(date, new MovementPointAccumulator(date, dayLabel(date.getDayOfWeek())));
        }

        for (InventoryMovement movement : movements) {
            MovementPointAccumulator accumulator = points.get(movement.getPerformedAt().toLocalDate());
            if (accumulator == null) {
                continue;
            }
            accumulator.apply(movement);
        }

        return points.values().stream()
                .map(MovementPointAccumulator::toDto)
                .toList();
    }

    private List<DashboardRecentMovementDto> buildRecentMovements(List<InventoryMovement> recentMovements) {
        return recentMovements.stream()
                .limit(RECENT_MOVEMENTS_LIMIT)
                .map(movement -> DashboardRecentMovementDto.builder()
                        .id(movement.getId())
                        .movementType(movement.getMovementType())
                        .laboratoryId(movement.getLaboratory() != null ? movement.getLaboratory().getId() : null)
                        .laboratoryName(movement.getLaboratory() != null ? movement.getLaboratory().getName() : null)
                        .performedByUsername(movement.getPerformedBy() != null
                                ? movement.getPerformedBy().getUsername()
                                : "Sistema")
                        .performedAt(movement.getPerformedAt())
                        .primaryProductName(movement.getLines().isEmpty()
                                ? null
                                : movement.getLines().get(0).getProduct().getName())
                        .lineCount((long) movement.getLines().size())
                        .totalQuantity(movement.getLines().stream()
                                .map(line -> line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .toList();
    }

    private List<DashboardLaboratorySummaryDto> buildLaboratorySummaries(
            List<Laboratory> laboratories,
            Map<Long, List<InventoryStockResponseDto>> stockByLaboratory,
            LocalDate today) {
        return laboratories.stream()
                .map(laboratory -> {
                    List<InventoryStockResponseDto> stockItems =
                            stockByLaboratory.getOrDefault(laboratory.getId(), List.of());
                    long visibleProducts = stockItems.stream()
                            .map(InventoryStockResponseDto::getProductId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .count();
                    long lowStockProducts = stockItems.stream()
                            .filter(item -> Boolean.TRUE.equals(item.getLowStock()))
                            .map(InventoryStockResponseDto::getProductId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .count();
                    long expiringBatches = productBatchRepository
                            .findByLaboratoryIdAndExpirationDateLessThanEqualAndActiveTrueOrderByExpirationDateAsc(
                                    laboratory.getId(), today.plusDays(EXPIRING_BATCH_WINDOW_DAYS))
                            .stream()
                            .filter(batch -> batch.getExpirationDate() != null
                                    && !batch.getExpirationDate().isBefore(today))
                            .count();
                    BigDecimal quantityAvailable = stockItems.stream()
                            .map(item -> item.getQuantityAvailable() != null
                                    ? item.getQuantityAvailable()
                                    : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return DashboardLaboratorySummaryDto.builder()
                            .laboratoryId(laboratory.getId())
                            .laboratoryCode(laboratory.getCode())
                            .laboratoryName(laboratory.getName())
                            .visibleProducts(visibleProducts)
                            .lowStockProducts(lowStockProducts)
                            .expiringBatches(expiringBatches)
                            .quantityAvailable(quantityAvailable)
                            .build();
                })
                .toList();
    }

    private String dayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "LUN";
            case TUESDAY -> "MAR";
            case WEDNESDAY -> "MIE";
            case THURSDAY -> "JUE";
            case FRIDAY -> "VIE";
            case SATURDAY -> "SAB";
            case SUNDAY -> "DOM";
        };
    }

    private static final class MovementPointAccumulator {

        private final LocalDate date;
        private final String dayLabel;
        private BigDecimal entryQuantity = BigDecimal.ZERO;
        private BigDecimal exitQuantity = BigDecimal.ZERO;
        private long entryMovements;
        private long exitMovements;

        private MovementPointAccumulator(LocalDate date, String dayLabel) {
            this.date = date;
            this.dayLabel = dayLabel;
        }

        private void apply(InventoryMovement movement) {
            BigDecimal totalQuantity = movement.getLines().stream()
                    .map(line -> line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (movement.getMovementType() == sv.edu.ues.qyf.inventory.entity.MovementType.ENTRY) {
                entryQuantity = entryQuantity.add(totalQuantity);
                entryMovements++;
                return;
            }

            exitQuantity = exitQuantity.add(totalQuantity);
            exitMovements++;
        }

        private DashboardMovementSeriesPointDto toDto() {
            return DashboardMovementSeriesPointDto.builder()
                    .date(date)
                    .dayLabel(dayLabel)
                    .entryQuantity(entryQuantity)
                    .exitQuantity(exitQuantity)
                    .entryMovements(entryMovements)
                    .exitMovements(exitMovements)
                    .build();
        }
    }
}
