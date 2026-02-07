package hu.psprog.leaflet.lags.core.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Entity class representing a role.
 *
 * @author Peter Smith
 */
@Data
@Entity
@Table(name = DatabaseConstants.TABLE_ROLES, uniqueConstraints = {
        @UniqueConstraint(columnNames = DatabaseConstants.COLUMN_NAME, name = DatabaseConstants.UK_ROLE_NAME)
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name = DatabaseConstants.COLUMN_LOCAL_DEFAULT)
    private boolean localDefault;

    @Column(name = DatabaseConstants.COLUMN_EXTERNAL_DEFAULT)
    private boolean externalDefault;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinTable(
            foreignKey = @ForeignKey(name = DatabaseConstants.FK_NM_ROLE_PERMISSIONS_ROLE_ID),
            inverseForeignKey = @ForeignKey(name = DatabaseConstants.FK_NM_ROLE_PERMISSIONS_PERMISSION_ID))
    @Builder.Default
    private List<Permission> permissions = Collections.emptyList();

    @CreatedDate
    @Column(name = DatabaseConstants.COLUMN_DATE_CREATED, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(name = DatabaseConstants.COLUMN_DATE_LAST_MODIFIED)
    private Date updatedAt;

    @Column
    @Builder.Default
    private boolean enabled = true;
}
