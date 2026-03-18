package sv.edu.ues.qyf.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto {

    private Long id;
    private String name;
    private String description;
    private Boolean active;
}
