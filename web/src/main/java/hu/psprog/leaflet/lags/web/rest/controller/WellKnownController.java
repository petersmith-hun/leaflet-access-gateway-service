package hu.psprog.leaflet.lags.web.rest.controller;

import com.nimbusds.jose.jwk.JWKSet;
import hu.psprog.leaflet.lags.web.model.AuthServerMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_WELL_KNOWN_JWKS;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_WELL_KNOWN_OAUTH_AUTHORIZATION_SERVER;

/**
 * Controller for publicly available authorization server meta-information (.well-known) endpoints.
 *
 * @author Peter Smith
 */
@RestController
@Slf4j
public class WellKnownController {

    private final JWKSet jwkSet;
    private final AuthServerMetaInfo authServerMetaInfo;

    @Autowired
    public WellKnownController(JWKSet jwkSet, AuthServerMetaInfo authServerMetaInfo) {
        this.jwkSet = jwkSet;
        this.authServerMetaInfo = authServerMetaInfo;
    }

    /**
     * GET /.well-known/jwks
     * Returns the public RSA key in JWK format for token signature verification.
     *
     * @return JWK set response as JSON document
     */
    @GetMapping(PATH_WELL_KNOWN_JWKS)
    public ResponseEntity<Map<String, Object>> getJWKs() {

        log.info("JWK Set requested.");

        return ResponseEntity
                .ok(jwkSet.toJSONObject());
    }

    /**
     * GET /.well-known/oauth-authorization-server
     * Returns the publicly available meta-information of the authorization server.
     *
     * @return server meta-information
     */
    @GetMapping(PATH_WELL_KNOWN_OAUTH_AUTHORIZATION_SERVER)
    public ResponseEntity<AuthServerMetaInfo> getServerMetaInfo() {

        log.info("Server meta-information requested.");

        return ResponseEntity
                .ok(authServerMetaInfo);
    }
}
