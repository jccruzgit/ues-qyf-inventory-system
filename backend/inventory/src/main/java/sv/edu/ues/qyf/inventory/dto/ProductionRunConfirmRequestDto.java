package sv.edu.ues.qyf.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRunConfirmRequestDto {

    @Valid
    @NotEmpty(message = "At least one production run item is required")
    private List<ProductionRunConfirmItemRequestDto> items;
}
