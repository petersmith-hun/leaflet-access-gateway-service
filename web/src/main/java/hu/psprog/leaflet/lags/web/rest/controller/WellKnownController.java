package hu.psprog.leaflet.lags.web.rest.controller;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_WELL_KNOWN_JWKS;

/**
 * Controller for publicly available authorization server meta-information (.well-known) endpoints.
 *
 * @author Peter Smith
 */
@Controller
@Slf4j
public class WellKnownController {

    private final JWKSet jwkSet;

    @Autowired
    public WellKnownController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
    }

    /**
     * GET /.well-known/jwks.
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
}
