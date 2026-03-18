package sv.edu.ues.qyf.inventory.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.UnitConversionRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitConversionResponseDto;
import sv.edu.ues.qyf.inventory.entity.UnitConversion;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.exception.BadRequestException;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.UnitConversionMapper;
import sv.edu.ues.qyf.inventory.repository.UnitConversionRepository;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.UnitConversionService;

@Service
@Transactional
public class UnitConversionServiceImpl implements UnitConversionService {

    private final UnitConversionRepository unitConversionRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final UnitConversionMapper unitConversionMapper;

    public UnitConversionServiceImpl(
            UnitConversionRepository unitConversionRepository,
            UnitOfMeasureRepository unitOfMeasureRepository,
            UnitConversionMapper unitConversionMapper) {
        this.unitConversionRepository = unitConversionRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.unitConversionMapper = unitConversionMapper;
    }

    @Override
    public UnitConversionResponseDto create(UnitConversionRequestDto request) {
        UnitOfMeasure sourceUnit = getUnitOfMeasure(request.getSourceUnitId(), "Source unit");
        UnitOfMeasure targetUnit = getUnitOfMeasure(request.getTargetUnitId(), "Target unit");
        validateUnits(sourceUnit, targetUnit);
        validateFactor(request.getConversionFactor());
        validateDuplicateConversion(sourceUnit.getId(), targetUnit.getId(), null);

        UnitConversion unitConversion = unitConversionMapper.toEntity(request);
        unitConversion.setSourceUnit(sourceUnit);
        unitConversion.setTargetUnit(targetUnit);
        unitConversion.setActive(resolveActive(request.getActive(), Boolean.TRUE));

        return unitConversionMapper.toResponseDto(unitConversionRepository.save(unitConversion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitConversionResponseDto> getAll() {
        return unitConversionRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(unitConversionMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitConversionResponseDto getById(Long id) {
        return unitConversionMapper.toResponseDto(getUnitConversion(id));
    }

    @Override
    public UnitConversionResponseDto update(Long id, UnitConversionRequestDto request) {
        UnitConversion unitConversion = getUnitConversion(id);
        UnitOfMeasure sourceUnit = getUnitOfMeasure(request.getSourceUnitId(), "Source unit");
        UnitOfMeasure targetUnit = getUnitOfMeasure(request.getTargetUnitId(), "Target unit");
        validateUnits(sourceUnit, targetUnit);
        validateFactor(request.getConversionFactor());
        validateDuplicateConversion(sourceUnit.getId(), targetUnit.getId(), id);

        unitConversionMapper.updateEntity(unitConversion, request);
        unitConversion.setSourceUnit(sourceUnit);
        unitConversion.setTargetUnit(targetUnit);
        unitConversion.setActive(resolveActive(request.getActive(), unitConversion.getActive()));

        return unitConversionMapper.toResponseDto(unitConversionRepository.save(unitConversion));
    }

    @Override
    public UnitConversionResponseDto deactivate(Long id) {
        UnitConversion unitConversion = getUnitConversion(id);
        unitConversion.setActive(Boolean.FALSE);
        return unitConversionMapper.toResponseDto(unitConversionRepository.save(unitConversion));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal convert(Long sourceUnitId, Long targetUnitId, BigDecimal quantity) {
        if (quantity == null) {
            throw new BadRequestException("Quantity is required");
        }

        if (sourceUnitId.equals(targetUnitId)) {
            return quantity;
        }

        UnitConversion conversion = unitConversionRepository
                .findBySourceUnitIdAndTargetUnitId(sourceUnitId, targetUnitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Unit conversion not found for source unit id " + sourceUnitId
                                + " and target unit id " + targetUnitId));

        return quantity.multiply(conversion.getConversionFactor());
    }

    private UnitConversion getUnitConversion(Long id) {
        return unitConversionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit conversion not found with id: " + id));
    }

    private UnitOfMeasure getUnitOfMeasure(Long id, String label) {
        return unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found with id: " + id));
    }

    private void validateUnits(UnitOfMeasure sourceUnit, UnitOfMeasure targetUnit) {
        if (sourceUnit.getId().equals(targetUnit.getId())) {
            throw new BadRequestException("Source unit and target unit cannot be the same");
        }

        if (sourceUnit.getType() != targetUnit.getType()) {
            throw new BadRequestException("Source unit and target unit must belong to the same unit type");
        }
    }

    private void validateFactor(BigDecimal conversionFactor) {
        if (conversionFactor == null || conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Conversion factor must be greater than 0");
        }
    }

    private void validateDuplicateConversion(Long sourceUnitId, Long targetUnitId, Long currentId) {
        unitConversionRepository.findBySourceUnitIdAndTargetUnitId(sourceUnitId, targetUnitId)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Unit conversion already exists for source unit id "
                                    + sourceUnitId
                                    + " and target unit id "
                                    + targetUnitId);
                });
    }

    private Boolean resolveActive(Boolean requestedActive, Boolean currentValue) {
        return requestedActive != null ? requestedActive : currentValue;
    }
}
