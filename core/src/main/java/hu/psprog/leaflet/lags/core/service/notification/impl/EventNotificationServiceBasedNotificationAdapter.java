package hu.psprog.leaflet.lags.core.service.notification.impl;

import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import hu.psprog.leaflet.lens.api.domain.MailContent;
import hu.psprog.leaflet.lens.api.domain.MailRequestWrapper;
import hu.psprog.leaflet.lens.api.domain.PasswordResetRequest;
import hu.psprog.leaflet.lens.api.domain.PasswordResetSuccess;
import hu.psprog.leaflet.lens.api.domain.SignUpConfirmation;
import hu.psprog.leaflet.lens.api.domain.SystemStartup;
import hu.psprog.leaflet.lens.client.EventNotificationServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Leaflet Event Notification Service based implementation of {@link NotificationAdapter}.
 * Mails are sent to LENS via LENS Bridge Client.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class EventNotificationServiceBasedNotificationAdapter implements NotificationAdapter {

    private static final String APPLICATION_NAME = "Leaflet Access Gateway";
    private static final String SYSTEM_STARTUP_SUBJECT_KEY = "mail.system.event.startup.subject.lags";

    private final EventNotificationServiceClient eventNotificationServiceClient;

    @Autowired
    public EventNotificationServiceBasedNotificationAdapter(EventNotificationServiceClient eventNotificationServiceClient) {
        this.eventNotificationServiceClient = eventNotificationServiceClient;
    }

    @Override
    public void signUpConfirmation(hu.psprog.leaflet.lags.core.domain.notification.SignUpConfirmation signUpConfirmation) {

        var signUpConfirmationMail = MailRequestWrapper.<SignUpConfirmation>builder()
                .recipients(signUpConfirmation.getEmail())
                .content(new SignUpConfirmation(signUpConfirmation.getUsername()))
                .build();

        submit(signUpConfirmationMail);
    }

    @Override
    public void passwordResetRequested(hu.psprog.leaflet.lags.core.domain.notification.PasswordResetRequest passwordResetRequest) {

        var passwordResetRequestMail = MailRequestWrapper.<PasswordResetRequest>builder()
                .recipients(passwordResetRequest.getRecipient())
                .content(PasswordResetRequest.builder()
                        .username(passwordResetRequest.getUsername())
                        .resetLink(passwordResetRequest.getResetLink())
                        .token(passwordResetRequest.getToken())
                        .expiration(passwordResetRequest.getExpiration())
                        .build())
                .build();

        submit(passwordResetRequestMail);
    }

    @Override
    public void successfulPasswordReset(hu.psprog.leaflet.lags.core.domain.notification.PasswordResetSuccess passwordResetSuccess) {

        var passwordResetSuccessMail = MailRequestWrapper.<PasswordResetSuccess>builder()
                .recipients(passwordResetSuccess.getRecipient())
                .content(new PasswordResetSuccess(passwordResetSuccess.getUsername()))
                .build();

        submit(passwordResetSuccessMail);
    }

    @Override
    public void startupFinished(String version) {

        var systemStartupMail = MailRequestWrapper.<SystemStartup>builder()
                .overrideSubjectKey(SYSTEM_STARTUP_SUBJECT_KEY)
                .content(SystemStartup.builder()
                        .applicationName(APPLICATION_NAME)
                        .version(version)
                        .build())
                .build();

        submit(systemStartupMail);
    }

    private void submit(MailRequestWrapper<? extends MailContent> mailRequestWrapper) {

        try {
            eventNotificationServiceClient.requestMailNotification(mailRequestWrapper);
            log.info("Submitted mail of type [{}]", mailRequestWrapper.content().getMailContentType());
        } catch (CommunicationFailureException exception) {
            log.error("Failed to submit mail request", exception);
        }
    }
}
