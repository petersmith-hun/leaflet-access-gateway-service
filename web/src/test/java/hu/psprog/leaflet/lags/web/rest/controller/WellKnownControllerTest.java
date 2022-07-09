package hu.psprog.leaflet.lags.web.rest.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link WellKnownController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class WellKnownControllerTest {

    private static final Map<String, Object> JWKS_RESPONSE = Map.of("kid", "unit-test-key");

    @Mock
    private JWKSet jwkSet;

    @InjectMocks
    private WellKnownController wellKnownController;

    @Test
    public void shouldGetJWKsReturnJWKSet() {

        // given
        given(jwkSet.toJSONObject()).willReturn(JWKS_RESPONSE);

        // when
        ResponseEntity<Map<String, Object>> result = wellKnownController.getJWKs();

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody(), notNullValue());
        assertThat(result.getBody(), equalTo(JWKS_RESPONSE));
    }
}
