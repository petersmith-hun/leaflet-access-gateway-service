package hu.psprog.leaflet.lags.core.service.mailing.observer.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import hu.psprog.leaflet.mail.config.MailProcessorConfigurationProperties;
import hu.psprog.leaflet.mail.domain.Mail;
import hu.psprog.leaflet.mail.domain.MailDeliveryInfo;
import hu.psprog.leaflet.mail.domain.MailDeliveryStatus;
import io.reactivex.Observable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link LoggingMailObserverHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class LoggingMailObserverHandlerTest {

    @Mock
    private MailProcessorConfigurationProperties configurationProperties;

    @Mock
    private Appender<ILoggingEvent> appender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    @InjectMocks
    private LoggingMailObserverHandler loggingMailObserverHandler;

    @BeforeEach
    public void setup() {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(appender);
    }

    @Test
    public void shouldAttachObserverWithRecipient() {

        // given
        MailDeliveryInfo mailDeliveryInfo = prepareMailDeliveryInfo(true, false);

        // when
        call(mailDeliveryInfo);

        // then
        assertLogMessage("Mail delivery status: [DELIVERED] 'Test mail' -> test@local.dev (0 constraint violations)");
    }

    @Test
    public void shouldAttachObserverWithoutRecipient() {

        // given
        MailDeliveryInfo mailDeliveryInfo = prepareMailDeliveryInfo(false, true);
        given(configurationProperties.getAdminNotificationAddress()).willReturn("admin@local.dev");

        // when
        call(mailDeliveryInfo);

        // then
        assertLogMessage("Mail delivery status: [DELIVERED] 'Test mail' -> admin@local.dev (1 constraint violations)");
    }

    private void assertLogMessage(String expectedMessage) {
        verify(appender, atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        assertThat(loggingEventArgumentCaptor.getAllValues().stream()
                .map(LoggingEvent::getMessage)
                .anyMatch(expectedMessage::equals), is(true));
    }

    private void call(MailDeliveryInfo mailDeliveryInfo) {
        Observable<MailDeliveryInfo> observable = Observable.just(mailDeliveryInfo);
        loggingMailObserverHandler.attachObserver(observable);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assertions.fail("Interrupted");
        }
    }

    private MailDeliveryInfo prepareMailDeliveryInfo(boolean withRecipient, boolean withConstraintViolations) {
        return MailDeliveryInfo.getBuilder()
                .withMailDeliveryStatus(MailDeliveryStatus.DELIVERED)
                .withConstraintViolations(withConstraintViolations ? prepareConstraintViolations() : null)
                .withMail(Mail.getBuilder()
                        .withSubject("Test mail")
                        .withRecipient(withRecipient ? "test@local.dev" : null)
                        .build())
                .build();
    }

    private Map<String, String> prepareConstraintViolations() {

        Map<String, String> constraintViolations = new HashMap<>();
        constraintViolations.put("field", "error");

        return constraintViolations;
    }
}
