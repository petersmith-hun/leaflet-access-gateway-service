package hu.psprog.leaflet.lags.core.service.token.validator;

import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link TrackedStatusTokenValidator}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class TrackedStatusTokenValidatorTest {

    @Mock
    private Jwt jwt;

    @Mock
    private TokenTracker tokenTracker;

    @InjectMocks
    private TrackedStatusTokenValidator validator;

    @Test
    public void shouldValidateReturnSuccessForActiveToken() {

        // given
        var tokenID = UUID.randomUUID().toString();
        var tokenInfo = prepareAccessTokenInfo();

        given(jwt.getId()).willReturn(tokenID);
        given(tokenTracker.retrieveTokenInfo(tokenID)).willReturn(Optional.of(tokenInfo));

        // when
        var result = validator.validate(jwt);

        // then
        assertThat(result.getErrors(), equalTo(Collections.emptyList()));
    }

    @Test
    public void shouldValidateReturnFailureForRevokedToken() {

        // given
        var tokenID = UUID.randomUUID().toString();
        var tokenInfo = prepareAccessTokenInfo();
        tokenInfo.setStatus(TokenStatus.REVOKED);

        given(jwt.getId()).willReturn(tokenID);
        given(tokenTracker.retrieveTokenInfo(tokenID)).willReturn(Optional.of(tokenInfo));

        // when
        var result = validator.validate(jwt);

        // then
        assertFailure(result);
    }

    @Test
    public void shouldValidateReturnFailureForMissingToken() {

        // given
        var tokenID = UUID.randomUUID().toString();

        given(jwt.getId()).willReturn(tokenID);
        given(tokenTracker.retrieveTokenInfo(tokenID)).willReturn(Optional.empty());

        // when
        var result = validator.validate(jwt);

        // then
        assertFailure(result);
    }

    private AccessTokenInfo prepareAccessTokenInfo() {
        return new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().build());
    }

    private void assertFailure(OAuth2TokenValidatorResult result) {

        assertThat(result.getErrors().size(), equalTo(1));
        assertThat(result.getErrors().stream().findFirst().isPresent(), is(true));
        assertThat(result.getErrors().stream().findFirst().get().getErrorCode(), equalTo("invalid_token"));
    }
}
