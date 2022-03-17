package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.SignUpStatus;
import hu.psprog.leaflet.lags.core.domain.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.util.NotificationAdapter;
import hu.psprog.leaflet.lags.core.service.util.ReCaptchaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataIntegrityViolationException;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link AuthenticationServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final long USER_ID = 1234L;
    private static final String USERNAME = "Local User 1";
    private static final String EMAIL = "user@dev.local";
    private static final String REDIRECT_URI = "https://dev.local:443/signup/callback";
    private static final SignUpRequestModel SIGN_UP_REQUEST_MODEL = new SignUpRequestModel();
    private static final SignUpConfirmation EXPECTED_SIGN_UP_CONFIRMATION = new SignUpConfirmation(USERNAME, EMAIL);
    private static final User CONVERTED_USER = User.builder()
            .id(USER_ID)
            .username(USERNAME)
            .build();

    static {
        SIGN_UP_REQUEST_MODEL.setUsername(USERNAME);
        SIGN_UP_REQUEST_MODEL.setEmail(EMAIL);
    }

    @Mock
    private UserDAO userDAO;

    @Mock
    private ConversionService conversionService;

    @Mock
    private ReCaptchaValidator reCaptchaValidator;

    @Mock
    private NotificationAdapter notificationAdapter;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    public void shouldHandleSignUpWithSuccess() {

        // given
        given(reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request)).willReturn(true);
        given(conversionService.convert(SIGN_UP_REQUEST_MODEL, User.class)).willReturn(CONVERTED_USER);
        given(request.getParameter("redirect_uri")).willReturn(REDIRECT_URI);

        // when
        SignUpResult result = authenticationService.signUp(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result.getRedirectURI(), equalTo(REDIRECT_URI));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.SUCCESS));

        verify(userDAO).save(CONVERTED_USER);
        verify(notificationAdapter).signUpConfirmation(EXPECTED_SIGN_UP_CONFIRMATION);
    }

    @Test
    public void shouldSignUpReturnReCaptchaVerificationFailure() {

        // given
        given(reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request)).willReturn(false);
        given(request.getParameter("redirect_uri")).willReturn(REDIRECT_URI);

        // when
        SignUpResult result = authenticationService.signUp(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result.getRedirectURI(), equalTo(REDIRECT_URI));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.RE_CAPTCHA_VERIFICATION_FAILED));

        verifyNoInteractions(userDAO, notificationAdapter);
    }

    @Test
    public void shouldSignUpReturnAddressAlreadyInUseStatusIfEmailAddressIsAlreadyUsed() {

        // given
        given(reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request)).willReturn(true);
        given(conversionService.convert(SIGN_UP_REQUEST_MODEL, User.class)).willReturn(CONVERTED_USER);
        given(request.getParameter("redirect_uri")).willReturn(REDIRECT_URI);
        doThrow(DataIntegrityViolationException.class).when(userDAO).save(CONVERTED_USER);

        // when
        SignUpResult result = authenticationService.signUp(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result.getRedirectURI(), equalTo(REDIRECT_URI));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.ADDRESS_IN_USE));

        verifyNoInteractions(notificationAdapter);
    }

    @Test
    public void shouldSignUpReturnFailureStatusOnUnexpectedException() {

        // given
        given(reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request)).willReturn(true);
        given(conversionService.convert(SIGN_UP_REQUEST_MODEL, User.class)).willReturn(CONVERTED_USER);
        given(request.getParameter("redirect_uri")).willReturn(REDIRECT_URI);
        doThrow(IllegalArgumentException.class).when(userDAO).save(CONVERTED_USER);

        // when
        SignUpResult result = authenticationService.signUp(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result.getRedirectURI(), equalTo(REDIRECT_URI));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.FAILURE));

        verifyNoInteractions(notificationAdapter);
    }
}
