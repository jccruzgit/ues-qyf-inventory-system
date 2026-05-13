package sv.edu.ues.qyf.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.CascadeType;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "correction_type", length = 20)
    private CorrectionType correctionType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    private Laboratory laboratory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @NotNull
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_movement_id")
    private InventoryMovement relatedMovement;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "correction_reason")
    private String correctionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_document_id")
    private ProductDocument attachmentDocument;

    @OneToOne(mappedBy = "inventoryMovement", fetch = FetchType.LAZY)
    private ProductionRun productionRun;

    @Builder.Default
    @OneToMany(mappedBy = "movement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryMovementLine> lines = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }
}
