package hu.psprog.leaflet.lags.core.service.util;

import java.security.PrivateKey;

/**
 * Interface for components handling encryption/decryption/signature keys.
 *
 * @author Peter Smith
 */
public interface KeyRegistry {

    /**
     * Returns the registered private key as {@link PrivateKey} object.
     *
     * @return the registered private key
     */
    PrivateKey getPrivateKey();
}
