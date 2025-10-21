package hu.psprog.leaflet.lags.core.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entity class representing an OAuth callback URL.
 *
 * @author Peter Smith
 */
@Data
@Entity
@Table(name = DatabaseConstants.TABLE_OAUTH_CALLBACKS)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String url;
}
