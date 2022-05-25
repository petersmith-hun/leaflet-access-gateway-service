package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.request.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.response.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataIntegrityViolationException;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_SIGNUP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link SignUpAccountRequestHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class SignUpAccountRequestHandlerTest {

    private static final long USER_ID = 1234L;
    private static final String USERNAME = "Local User 1";
    private static final String EMAIL = "user@dev.local";
    private static final SignUpRequestModel SIGN_UP_REQUEST_MODEL = new SignUpRequestModel();
    private static final SignUpConfirmation EXPECTED_SIGN_UP_CONFIRMATION = SignUpConfirmation.builder()
            .username(USERNAME)
            .email(EMAIL)
            .build();
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
    private NotificationAdapter notificationAdapter;

    @InjectMocks
    private SignUpAccountRequestHandler signUpAccountRequestHandler;

    @Test
    public void shouldProcessAccountRequestHandleSignUpWithSuccess() {

        // given
        given(conversionService.convert(SIGN_UP_REQUEST_MODEL, User.class)).willReturn(CONVERTED_USER);

        // when
        SignUpResult result = signUpAccountRequestHandler.processAccountRequest(SIGN_UP_REQUEST_MODEL);

        // then
        assertThat(result.getRedirectURI(), equalTo(PATH_LOGIN));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.SUCCESS));

        verify(userDAO).save(CONVERTED_USER);
        verify(notificationAdapter).signUpConfirmation(EXPECTED_SIGN_UP_CONFIRMATION);
    }

    @Test
    public void shouldProcessAccountRequestReturnAddressAlreadyInUseStatusIfEmailAddressIsAlreadyUsed() {

        // given
        given(conversionService.convert(SIGN_UP_REQUEST_MODEL, User.class)).willReturn(CONVERTED_USER);
        doThrow(DataIntegrityViolationException.class).when(userDAO).save(CONVERTED_USER);

        // when
        SignUpResult result = signUpAccountRequestHandler.processAccountRequest(SIGN_UP_REQUEST_MODEL);

        // then
        assertThat(result.getRedirectURI(), equalTo(PATH_SIGNUP));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.ADDRESS_IN_USE));

        verifyNoInteractions(notificationAdapter);
    }

    @Test
    public void shouldProcessAccountRequestReturnFailureStatusOnUnexpectedException() {

        // given
        given(conversionService.convert(SIGN_UP_REQUEST_MODEL, User.class)).willReturn(CONVERTED_USER);
        doThrow(IllegalArgumentException.class).when(userDAO).save(CONVERTED_USER);

        // when
        SignUpResult result = signUpAccountRequestHandler.processAccountRequest(SIGN_UP_REQUEST_MODEL);

        // then
        assertThat(result.getRedirectURI(), equalTo(PATH_SIGNUP));
        assertThat(result.getSignUpStatus(), equalTo(SignUpStatus.FAILURE));

        verifyNoInteractions(notificationAdapter);
    }

}
