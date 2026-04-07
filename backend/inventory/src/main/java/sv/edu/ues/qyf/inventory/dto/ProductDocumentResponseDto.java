package sv.edu.ues.qyf.inventory.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocumentResponseDto {

    private Long id;
    private Long productId;
    private String fileName;
    private String originalName;
    private String fileType;
    private String filePath;
    private String description;
    private Long uploadedById;
    private LocalDateTime uploadedAt;
    private Boolean active;
    private LocalDateTime deletedAt;
    private Long deletedById;
}
