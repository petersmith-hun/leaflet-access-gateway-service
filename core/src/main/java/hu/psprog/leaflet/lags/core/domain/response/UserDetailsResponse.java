package hu.psprog.leaflet.lags.core.domain.response;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import lombok.Builder;

import java.time.ZonedDateTime;

/**
 * Response model representing a user.
 *
 * @param id ID of the user
 * @param username registered username
 * @param email related email address
 * @param role legacy role of the user
 * @param locale default language of the user
 * @param accountType indicates what IDP provided the user's data
 * @param externalID if an external IDP provided this user, the identifier of that account
 * @param enabled flag indicating whether this role can be selected for users
 * @param created creation timestamp
 * @param lastModified last modification timestamp
 * @param lastLogin last time the user logged in
 */
@Builder
public record UserDetailsResponse(
        long id,
        String username,
        String email,
        LegacyRole role,
        SupportedLocale locale,
        AccountType accountType,
        String externalID,
        boolean enabled,
        ZonedDateTime created,
        ZonedDateTime lastModified,
        ZonedDateTime lastLogin
) { }
