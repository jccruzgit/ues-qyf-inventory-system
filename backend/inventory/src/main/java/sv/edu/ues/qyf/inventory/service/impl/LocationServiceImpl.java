package sv.edu.ues.qyf.inventory.service.impl;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.ues.qyf.inventory.dto.LocationRequestDto;
import sv.edu.ues.qyf.inventory.dto.LocationResponseDto;
import sv.edu.ues.qyf.inventory.entity.Location;
import sv.edu.ues.qyf.inventory.exception.DuplicateResourceException;
import sv.edu.ues.qyf.inventory.exception.ResourceNotFoundException;
import sv.edu.ues.qyf.inventory.mapper.LocationMapper;
import sv.edu.ues.qyf.inventory.repository.LocationRepository;
import sv.edu.ues.qyf.inventory.service.LocationService;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    @Override
    public LocationResponseDto create(LocationRequestDto request) {
        String name = normalize(request.getName());
        validateDuplicateName(name, null);

        Location location = locationMapper.toEntity(request);
        location.setName(name);
        location.setDescription(normalizeNullable(request.getDescription()));
        location.setActive(resolveActive(request.getActive(), Boolean.TRUE));

        return locationMapper.toResponseDto(locationRepository.save(location));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponseDto> getAll() {
        return locationRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(locationMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponseDto getById(Long id) {
        return locationMapper.toResponseDto(getLocation(id));
    }

    @Override
    public LocationResponseDto update(Long id, LocationRequestDto request) {
        Location location = getLocation(id);
        String name = normalize(request.getName());
        validateDuplicateName(name, id);

        locationMapper.updateEntity(location, request);
        location.setName(name);
        location.setDescription(normalizeNullable(request.getDescription()));
        location.setActive(resolveActive(request.getActive(), location.getActive()));

        return locationMapper.toResponseDto(locationRepository.save(location));
    }

    @Override
    public LocationResponseDto deactivate(Long id) {
        Location location = getLocation(id);
        location.setActive(Boolean.FALSE);
        return locationMapper.toResponseDto(locationRepository.save(location));
    }

    private Location getLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }

    private void validateDuplicateName(String name, Long currentId) {
        locationRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Location name already exists: " + name);
                });
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Boolean resolveActive(Boolean requestedActive, Boolean currentValue) {
        return requestedActive != null ? requestedActive : currentValue;
    }
}
