package hu.psprog.leaflet.lags.core.service.notification.impl;

import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.lags.core.domain.notification.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.domain.notification.PasswordResetSuccess;
import hu.psprog.leaflet.lags.core.domain.notification.SignUpConfirmation;
import hu.psprog.leaflet.lens.api.domain.MailRequestWrapper;
import hu.psprog.leaflet.lens.api.domain.SystemStartup;
import hu.psprog.leaflet.lens.client.EventNotificationServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link EventNotificationServiceBasedNotificationAdapter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class EventNotificationServiceBasedNotificationAdapterTest {

    private static final String USERNAME = "user1";
    private static final String EMAIL = "email@dev.local";
    private static final String TOKEN = "token-1";
    private static final int EXPIRATION = 300;
    private static final String RESET_LINK = "http://localhost:9999/reset";
    private static final String VERSION = "1.0.0";
    private static final String APPLICATION_NAME = "Leaflet Access Gateway";
    private static final String SYSTEM_STARTUP_SUBJECT_KEY = "mail.system.event.startup.subject.lags";

    @Mock
    private EventNotificationServiceClient eventNotificationServiceClient;

    @InjectMocks
    private EventNotificationServiceBasedNotificationAdapter eventNotificationServiceBasedNotificationAdapter;

    @Test
    public void shouldSendSignUpConfirmation() throws CommunicationFailureException {

        // given
        SignUpConfirmation signUpConfirmation = SignUpConfirmation.builder()
                .username(USERNAME)
                .email(EMAIL)
                .build();
        var expectedWrapper = MailRequestWrapper.<hu.psprog.leaflet.lens.api.domain.SignUpConfirmation>builder()
                .recipients(EMAIL)
                .content(new hu.psprog.leaflet.lens.api.domain.SignUpConfirmation(USERNAME))
                .build();

        // when
        eventNotificationServiceBasedNotificationAdapter.signUpConfirmation(signUpConfirmation);

        // then
        verify(eventNotificationServiceClient).requestMailNotification(expectedWrapper);
    }

    @Test
    public void shouldSendPasswordResetRequestedNotification() throws CommunicationFailureException {

        // given
        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .recipient(EMAIL)
                .username(USERNAME)
                .token(TOKEN)
                .expiration(EXPIRATION)
                .resetLink(RESET_LINK)
                .build();
        var expectedWrapper = MailRequestWrapper.<hu.psprog.leaflet.lens.api.domain.PasswordResetRequest>builder()
                .recipients(EMAIL)
                .content(hu.psprog.leaflet.lens.api.domain.PasswordResetRequest.builder()
                        .username(USERNAME)
                        .token(TOKEN)
                        .expiration(EXPIRATION)
                        .resetLink(RESET_LINK)
                        .build())
                .build();

        // when
        eventNotificationServiceBasedNotificationAdapter.passwordResetRequested(passwordResetRequest);

        // then
        verify(eventNotificationServiceClient).requestMailNotification(expectedWrapper);
    }

    @Test
    public void shouldSendPasswordResetSuccessfulNotification() throws CommunicationFailureException {

        // given
        PasswordResetSuccess passwordResetSuccess = PasswordResetSuccess.builder()
                .username(USERNAME)
                .recipient(EMAIL)
                .build();
        var expectedWrapper = MailRequestWrapper.<hu.psprog.leaflet.lens.api.domain.PasswordResetSuccess>builder()
                .recipients(EMAIL)
                .content(new hu.psprog.leaflet.lens.api.domain.PasswordResetSuccess(USERNAME))
                .build();

        // when
        eventNotificationServiceBasedNotificationAdapter.successfulPasswordReset(passwordResetSuccess);

        // then
        verify(eventNotificationServiceClient).requestMailNotification(expectedWrapper);
    }

    @Test
    public void shouldSendStartupFinishedNotification() throws CommunicationFailureException {

        // given
        var expectedWrapper = MailRequestWrapper.<SystemStartup>builder()
                .overrideSubjectKey(SYSTEM_STARTUP_SUBJECT_KEY)
                .content(SystemStartup.builder()
                        .applicationName(APPLICATION_NAME)
                        .version(VERSION)
                        .build())
                .build();

        // when
        eventNotificationServiceBasedNotificationAdapter.startupFinished(VERSION);

        // then
        verify(eventNotificationServiceClient).requestMailNotification(expectedWrapper);
    }

    @Test
    public void shouldFailSilentlyOnCommunicationFailure() throws CommunicationFailureException {

        // given
        doThrow(CommunicationFailureException.class).when(eventNotificationServiceClient).requestMailNotification(any(MailRequestWrapper.class));

        // when
        eventNotificationServiceBasedNotificationAdapter.startupFinished(VERSION);

        // then
        // fail silently
    }
}
