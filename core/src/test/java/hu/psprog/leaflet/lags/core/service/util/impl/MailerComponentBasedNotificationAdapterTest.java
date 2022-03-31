package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.mailing.impl.MailFactoryRegistry;
import hu.psprog.leaflet.lags.core.service.mailing.impl.SignUpConfirmationMailFactory;
import hu.psprog.leaflet.lags.core.service.mailing.observer.impl.LoggingMailObserverHandler;
import hu.psprog.leaflet.mail.client.MailClient;
import hu.psprog.leaflet.mail.domain.Mail;
import hu.psprog.leaflet.mail.domain.MailDeliveryInfo;
import io.reactivex.Observable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link MailerComponentBasedNotificationAdapter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class MailerComponentBasedNotificationAdapterTest {

    private static final Observable<MailDeliveryInfo> DELIVERY_INFO_OBSERVABLE = Observable.empty();

    @Mock
    private MailClient mailClient;

    @Mock
    private LoggingMailObserverHandler loggingMailObserverHandler;

    @Mock
    private MailFactoryRegistry mailFactoryRegistry;

    @Mock
    private SignUpConfirmationMailFactory signUpConfirmationMailFactory;

    @Mock
    private Mail mockMail;

    @InjectMocks
    private MailerComponentBasedNotificationAdapter mailerComponentBasedNotificationAdapter;

    @Test
    public void shouldSendSignUpConfirmation() {

        // given
        SignUpConfirmation signUpConfirmation = new SignUpConfirmation("username", "email");

        given(mailFactoryRegistry.getFactory(SignUpConfirmationMailFactory.class)).willReturn(signUpConfirmationMailFactory);
        given(signUpConfirmationMailFactory.buildMail(signUpConfirmation, signUpConfirmation.getEmail())).willReturn(mockMail);
        given(mailClient.sendMail(mockMail)).willReturn(DELIVERY_INFO_OBSERVABLE);

        // when
        mailerComponentBasedNotificationAdapter.signUpConfirmation(signUpConfirmation);

        // then
        assertMailSentAndObserverAttached();
    }

    private void assertMailSentAndObserverAttached() {
        verify(mailClient).sendMail(mockMail);
        verify(loggingMailObserverHandler).attachObserver(DELIVERY_INFO_OBSERVABLE);
    }
}
