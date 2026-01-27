package hu.psprog.leaflet.lags.core.domain.entity;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Entity class representing an OAuth client registration.
 *
 * @author Peter Smith
 */
@Data
@Entity
@Table(name = DatabaseConstants.TABLE_OAUTH_APPLICATIONS, uniqueConstraints = {
        @UniqueConstraint(columnNames = DatabaseConstants.COLUMN_NAME, name = DatabaseConstants.UK_OAUTH_APPLICATION_NAME),
        @UniqueConstraint(columnNames = DatabaseConstants.COLUMN_CLIENT_ID, name = DatabaseConstants.UK_OAUTH_APPLICATION_CLIENT_ID),
        @UniqueConstraint(columnNames = DatabaseConstants.COLUMN_AUDIENCE, name = DatabaseConstants.UK_OAUTH_APPLICATION_AUDIENCE),
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OAuthApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;

    @Column(unique = true)
    private String clientId;

    private String clientSecret;

    @Column(unique = true)
    private String audience;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinTable(
            foreignKey = @ForeignKey(name = DatabaseConstants.FK_NM_REQUIRED_PERMISSIONS_APP_ID),
            inverseForeignKey = @ForeignKey(name = DatabaseConstants.FK_NM_REQUIRED_PERMISSIONS_PERMISSION_ID))
    private List<Permission> requiredPermissions;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinTable(
            foreignKey = @ForeignKey(name = DatabaseConstants.FK_NM_REGISTERED_PERMISSIONS_APP_ID),
            inverseForeignKey = @ForeignKey(name = DatabaseConstants.FK_NM_REGISTERED_PERMISSIONS_PERMISSION_ID))
    private List<Permission> registeredPermissions;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(
            referencedColumnName = DatabaseConstants.COLUMN_ID,
            name = DatabaseConstants.COLUMN_OAUTH_APPLICATION_ID,
            foreignKey = @ForeignKey(name = DatabaseConstants.FK_OAUTH_CALLBACK_OAUTH_APPLICATION_ID))
    private List<OAuthCallback> callbacks;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(
            referencedColumnName = DatabaseConstants.COLUMN_ID, name =
            DatabaseConstants.COLUMN_OAUTH_APPLICATION_ID,
            foreignKey = @ForeignKey(name = DatabaseConstants.FK_OAUTH_ALLOWED_CLIENT_SELF_APPLICATION_ID))
    private List<OAuthAllowedClient> allowedClients;

    @CreatedDate
    @Column(name = DatabaseConstants.COLUMN_DATE_CREATED, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(name = DatabaseConstants.COLUMN_DATE_LAST_MODIFIED)
    private Date updatedAt;

    @Column
    private boolean enabled;
}
