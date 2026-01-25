package hu.psprog.leaflet.lags.core.service.util;

/**
 * Implementations of this interface must be able to generate secure random secrets (e.g. for auto-generating OAuth client secrets).
 *
 * @author Peter Smith
 */
public interface SecretGenerator {

    /**
     * Creates a secure random string.
     *
     * @return generated secure random string
     */
    String generateSecret();
}
