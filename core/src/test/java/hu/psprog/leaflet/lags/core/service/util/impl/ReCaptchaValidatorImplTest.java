package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.request.SignUpRequestModel;
import hu.psprog.leaflet.recaptcha.api.client.ReCaptchaClient;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaErrorCode;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaRequest;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for {@link ReCaptchaValidatorImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ReCaptchaValidatorImplTest {

    private static final SignUpRequestModel SIGN_UP_REQUEST_MODEL = new SignUpRequestModel();
    private static final String RECAPTCHA_RESPONSE = "test-response";
    private static final String RECAPTCHA_SECRET = "recaptcha-secret";
    private static final String REMOTE_IP = "127.0.0.1";
    private static final ReCaptchaRequest RE_CAPTCHA_REQUEST = ReCaptchaRequest.getBuilder()
            .withResponse(RECAPTCHA_RESPONSE)
            .withSecret(RECAPTCHA_SECRET)
            .withRemoteIp(REMOTE_IP)
            .build();

    static {
        SIGN_UP_REQUEST_MODEL.setRecaptchaToken(RECAPTCHA_RESPONSE);
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private ReCaptchaClient reCaptchaClient;

    @Mock
    private AuthenticationConfig authenticationConfig;

    @InjectMocks
    private ReCaptchaValidatorImpl reCaptchaValidator;

    @Test
    public void shouldValidationReturnTrue() throws CommunicationFailureException {

        // given
        given(authenticationConfig.getRecaptchaSecret()).willReturn(RECAPTCHA_SECRET);
        given(request.getRemoteAddr()).willReturn(REMOTE_IP);
        given(reCaptchaClient.validate(RE_CAPTCHA_REQUEST)).willReturn(ReCaptchaResponse.getBuilder()
                .withSuccessful(true)
                .build());

        // when
        boolean result = reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void shouldValidationReturnFalseIfTokenIsInvalid() throws CommunicationFailureException {

        // given
        given(authenticationConfig.getRecaptchaSecret()).willReturn(RECAPTCHA_SECRET);
        given(request.getRemoteAddr()).willReturn(REMOTE_IP);
        given(reCaptchaClient.validate(RE_CAPTCHA_REQUEST)).willReturn(ReCaptchaResponse.getBuilder()
                .withSuccessful(false)
                .withErrorCodes(Collections.singletonList(ReCaptchaErrorCode.TIMEOUT_OR_DUPLICATE))
                .build());

        // when
        boolean result = reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void shouldValidationReturnFalseOnCommunicationError() throws CommunicationFailureException {

        // given
        given(authenticationConfig.getRecaptchaSecret()).willReturn(RECAPTCHA_SECRET);
        given(request.getRemoteAddr()).willReturn(REMOTE_IP);
        doThrow(CommunicationFailureException.class).when(reCaptchaClient).validate(RE_CAPTCHA_REQUEST);

        // when
        boolean result = reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result, is(false));
    }
}
