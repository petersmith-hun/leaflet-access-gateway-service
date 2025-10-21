package hu.psprog.leaflet.lags.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entity class representing a permission.
 *
 * @author Peter Smith
 */
@Data
@Entity
@Table(name = DatabaseConstants.TABLE_PERMISSIONS, uniqueConstraints = {
        @UniqueConstraint(columnNames = DatabaseConstants.COLUMN_NAME, name = DatabaseConstants.UK_PERMISSION_NAME)
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String name;
}
