package sv.edu.ues.qyf.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sv.edu.ues.qyf.inventory.entity.UnitType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureResponseDto {

    private Long id;
    private String name;
    private String symbol;
    private UnitType type;
    private Boolean active;
}
