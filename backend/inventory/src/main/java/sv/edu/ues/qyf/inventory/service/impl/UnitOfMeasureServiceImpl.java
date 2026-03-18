package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureRequestDto;
import sv.edu.ues.qyf.inventory.dto.UnitOfMeasureResponseDto;
import sv.edu.ues.qyf.inventory.entity.UnitOfMeasure;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.UnitOfMeasureMapper;
import sv.edu.ues.qyf.inventory.repository.UnitOfMeasureRepository;
import sv.edu.ues.qyf.inventory.service.UnitOfMeasureService;

@Service
@Transactional
public class UnitOfMeasureServiceImpl implements UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final UnitOfMeasureMapper unitOfMeasureMapper;

    public UnitOfMeasureServiceImpl(
            UnitOfMeasureRepository unitOfMeasureRepository, UnitOfMeasureMapper unitOfMeasureMapper) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.unitOfMeasureMapper = unitOfMeasureMapper;
    }

    @Override
    public UnitOfMeasureResponseDto create(UnitOfMeasureRequestDto request) {
        String name = normalize(request.getName());
        String symbol = normalize(request.getSymbol());
        validateDuplicateSymbol(symbol, null);

        UnitOfMeasure unitOfMeasure = unitOfMeasureMapper.toEntity(request);
        unitOfMeasure.setName(name);
        unitOfMeasure.setSymbol(symbol);
        unitOfMeasure.setActive(resolveActive(request.getActive(), Boolean.TRUE));

        return unitOfMeasureMapper.toResponseDto(unitOfMeasureRepository.save(unitOfMeasure));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponseDto> getAll() {
        return unitOfMeasureRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(unitOfMeasureMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasureResponseDto getById(Long id) {
        return unitOfMeasureMapper.toResponseDto(getUnitOfMeasure(id));
    }

    @Override
    public UnitOfMeasureResponseDto update(Long id, UnitOfMeasureRequestDto request) {
        UnitOfMeasure unitOfMeasure = getUnitOfMeasure(id);
        String name = normalize(request.getName());
        String symbol = normalize(request.getSymbol());
        validateDuplicateSymbol(symbol, id);

        unitOfMeasureMapper.updateEntity(unitOfMeasure, request);
        unitOfMeasure.setName(name);
        unitOfMeasure.setSymbol(symbol);
        unitOfMeasure.setActive(resolveActive(request.getActive(), unitOfMeasure.getActive()));

        return unitOfMeasureMapper.toResponseDto(unitOfMeasureRepository.save(unitOfMeasure));
    }

    @Override
    public UnitOfMeasureResponseDto deactivate(Long id) {
        UnitOfMeasure unitOfMeasure = getUnitOfMeasure(id);
        unitOfMeasure.setActive(Boolean.FALSE);
        return unitOfMeasureMapper.toResponseDto(unitOfMeasureRepository.save(unitOfMeasure));
    }

    private UnitOfMeasure getUnitOfMeasure(Long id) {
        return unitOfMeasureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit of measure not found with id: " + id));
    }

    private void validateDuplicateSymbol(String symbol, Long currentId) {
        unitOfMeasureRepository.findBySymbol(symbol)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Unit symbol already exists: " + symbol);
                });
    }

    private String normalize(String value) {
        return value.trim();
    }

    private Boolean resolveActive(Boolean requestedActive, Boolean currentValue) {
        return requestedActive != null ? requestedActive : currentValue;
    }
}
