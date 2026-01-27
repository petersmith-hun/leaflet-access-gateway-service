package hu.psprog.leaflet.lags.core.service.token.validator;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link PasswordResetTokenValidator}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetTokenValidatorTest {

    @Mock
    private Jwt jwt;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AuthenticationConfig authenticationConfig;

    @InjectMocks
    private PasswordResetTokenValidator passwordResetTokenValidator;

    @Test
    public void shouldValidateReturnSuccessForRequestsOtherThanPasswordReset() {

        // given
        given(jwt.getSubject()).willReturn("app|uid=1");

        // when
        var result = passwordResetTokenValidator.validate(jwt);

        // then
        assertSuccess(result);
    }

    @Test
    public void shouldValidateReturnSuccessForPasswordResetRequest() {

        // given
        given(authenticationConfig.getPasswordReset().getAudience()).willReturn("lags");
        given(jwt.getSubject()).willReturn("password-reset|uid=1");
        given(jwt.getClaim("scope")).willReturn("write:reclaim");
        given(jwt.getClaimAsStringList("aud")).willReturn(List.of("lags"));

        // when
        var result = passwordResetTokenValidator.validate(jwt);

        // then
        assertSuccess(result);
    }

    @Test
    public void shouldValidateReturnFailureForTooBroadScope() {

        // given
        given(jwt.getSubject()).willReturn("password-reset|uid=1");
        given(jwt.getClaim("scope")).willReturn("write:reclaim write:admin");

        // when
        var result = passwordResetTokenValidator.validate(jwt);

        // then
        assertFailure(result);
    }

    @Test
    public void shouldValidateReturnFailureForInvalidAudience() {

        // given
        given(authenticationConfig.getPasswordReset().getAudience()).willReturn("lags");
        given(jwt.getSubject()).willReturn("password-reset|uid=1");
        given(jwt.getClaim("scope")).willReturn("write:reclaim");
        given(jwt.getClaimAsStringList("aud")).willReturn(List.of("something-else"));

        // when
        var result = passwordResetTokenValidator.validate(jwt);

        // then
        assertFailure(result);
    }

    private void assertSuccess(OAuth2TokenValidatorResult result) {
        assertThat(result, equalTo(OAuth2TokenValidatorResult.success()));
    }

    private void assertFailure(OAuth2TokenValidatorResult result) {

        assertThat(result.getErrors().size(), equalTo(1));
        assertThat(result.getErrors().stream().findFirst().isPresent(), is(true));
        assertThat(result.getErrors().stream().findFirst().get().getErrorCode(), equalTo("invalid_token"));
    }
}
