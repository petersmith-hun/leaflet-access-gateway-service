package hu.psprog.leaflet.lags.core.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Entity class representing an allowed client of an OAuth application.
 *
 * @author Peter Smith
 */
@Data
@Entity
@Table(name = DatabaseConstants.TABLE_OAUTH_ALLOWED_CLIENTS)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthAllowedClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(foreignKey = @ForeignKey(name = DatabaseConstants.FK_OAUTH_ALLOWED_CLIENT_TARGET_APPLICATION_ID))
    private OAuthApplication targetApplication;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            foreignKey = @ForeignKey(name = DatabaseConstants.FK_NM_CLIENT_PERMISSIONS_RELATION_ID),
            inverseForeignKey = @ForeignKey(name = DatabaseConstants.FK_NM_CLIENT_PERMISSIONS_PERMISSION_ID))
    private List<Permission> permissions;
}
