package hu.psprog.leaflet.lags.core.service.notification.impl;

import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetSuccess;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.mailing.impl.MailFactoryRegistry;
import hu.psprog.leaflet.lags.core.service.mailing.impl.PasswordResetRequestMailFactory;
import hu.psprog.leaflet.lags.core.service.mailing.impl.PasswordResetSuccessMailFactory;
import hu.psprog.leaflet.lags.core.service.mailing.impl.SignUpConfirmationMailFactory;
import hu.psprog.leaflet.lags.core.service.mailing.observer.ObserverHandler;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import hu.psprog.leaflet.mail.client.MailClient;
import hu.psprog.leaflet.mail.domain.Mail;
import hu.psprog.leaflet.mail.domain.MailDeliveryInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Leaflet Mailer Component based implementation of {@link NotificationAdapter}.
 * As in Leaflet, this implementation sends out the notifications in email, utilizing the Mailer Component.
 * Waiting for the response of the SMTP server is not blocking execution, only a logging observer is attached.
 *
 * @author Peter Smith
 */
@Component
public class MailerComponentBasedNotificationAdapter implements NotificationAdapter {

    private final MailClient mailClient;
    private final ObserverHandler<MailDeliveryInfo> mailDeliveryInfoObserverHandler;
    private final MailFactoryRegistry mailFactoryRegistry;

    @Autowired
    public MailerComponentBasedNotificationAdapter(MailClient mailClient, ObserverHandler<MailDeliveryInfo> mailDeliveryInfoObserverHandler,
                                                   MailFactoryRegistry mailFactoryRegistry) {
        this.mailClient = mailClient;
        this.mailDeliveryInfoObserverHandler = mailDeliveryInfoObserverHandler;
        this.mailFactoryRegistry = mailFactoryRegistry;
    }

    @Override
    public void signUpConfirmation(SignUpConfirmation signUpConfirmation) {

        Mail mail = mailFactoryRegistry
                .getFactory(SignUpConfirmationMailFactory.class)
                .buildMail(signUpConfirmation, signUpConfirmation.getEmail());

        sendMailAndAttachObserver(mail);
    }

    @Override
    public void passwordResetRequested(PasswordResetRequest passwordResetRequest) {

        Mail mail = mailFactoryRegistry
                .getFactory(PasswordResetRequestMailFactory.class)
                .buildMail(passwordResetRequest, passwordResetRequest.getParticipant());

        sendMailAndAttachObserver(mail);
    }

    @Override
    public void successfulPasswordReset(PasswordResetSuccess passwordResetSuccess) {

        Mail mail = mailFactoryRegistry
                .getFactory(PasswordResetSuccessMailFactory.class)
                .buildMail(passwordResetSuccess.getUsername(), passwordResetSuccess.getParticipant());

        sendMailAndAttachObserver(mail);
    }

    private void sendMailAndAttachObserver(Mail mail) {
        mailDeliveryInfoObserverHandler.attachObserver(mailClient.sendMail(mail));
    }
}
