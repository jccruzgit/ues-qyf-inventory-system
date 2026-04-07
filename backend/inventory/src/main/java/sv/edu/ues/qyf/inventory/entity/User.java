package sv.edu.ues.qyf.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotNull
    @Column(nullable = false)
    private Boolean active;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "access_scope", nullable = false, length = 30)
    private AccessScope accessScope;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<UserLaboratory> laboratoryAssignments;

    @JsonIgnore
    @OneToMany(mappedBy = "changedBy")
    private Set<AuditLog> auditLogs;

    @JsonIgnore
    @OneToMany(mappedBy = "uploadedBy")
    private Set<ProductDocument> uploadedDocuments;

    @JsonIgnore
    @OneToMany(mappedBy = "deletedBy")
    private Set<ProductDocument> deletedDocuments;

    @JsonIgnore
    @OneToMany(mappedBy = "deletedBy")
    private Set<Product> deletedProducts;

    @JsonIgnore
    @OneToMany(mappedBy = "deletedBy")
    private Set<Laboratory> deletedLaboratories;

    @JsonIgnore
    @OneToMany(mappedBy = "deletedBy")
    private Set<ProductBatch> deletedProductBatches;

    @JsonIgnore
    @OneToMany(mappedBy = "acknowledgedBy")
    private Set<InventoryAlert> acknowledgedAlerts;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        if (active == null) {
            active = Boolean.TRUE;
        }
        if (accessScope == null) {
            accessScope = AccessScope.ALL_LABS;
        }
    }
}
