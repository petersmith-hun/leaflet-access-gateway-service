package hu.psprog.leaflet.lags.core.domain.internal;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authentication related constant values.
 *
 * @author Peter Smith
 */
public interface SecurityConstants {

    String PATH_LOGIN = "/login";
    String PATH_SIGNUP = "/signup";
    String PATH_PASSWORD_RESET = "/password-reset";
    String PATH_PASSWORD_RESET_CONFIRMATION = "/password-reset/confirmation";

    String QUERY_PARAMETER_TOKEN = "token";
    String RECLAIM_ROLE = "RECLAIM";
    SimpleGrantedAuthority RECLAIM_AUTHORITY = new SimpleGrantedAuthority("write:reclaim");
}
