package sv.edu.ues.qyf.inventory.mapper;

import org.springframework.stereotype.Component;
import sv.edu.ues.qyf.inventory.dto.LocationRequestDto;
import sv.edu.ues.qyf.inventory.dto.LocationResponseDto;
import sv.edu.ues.qyf.inventory.entity.Location;

@Component
public class LocationMapper {

    public Location toEntity(LocationRequestDto request) {
        if (request == null) {
            return null;
        }

        return Location.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive())
                .build();
    }

    public void updateEntity(Location location, LocationRequestDto request) {
        location.setName(request.getName());
        location.setDescription(request.getDescription());
    }

    public LocationResponseDto toResponseDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationResponseDto.builder()
                .id(location.getId())
                .name(location.getName())
                .description(location.getDescription())
                .active(location.getActive())
                .build();
    }
}
