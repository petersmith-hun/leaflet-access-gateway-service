package hu.psprog.leaflet.lags.core.service.registry;

import java.security.PrivateKey;
import java.security.PublicKey;

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

    /**
     * Returns the registered publey key as {@link PublicKey} object.
     *
     * @return the registered public key
     */
    PublicKey getPublicKey();
}
