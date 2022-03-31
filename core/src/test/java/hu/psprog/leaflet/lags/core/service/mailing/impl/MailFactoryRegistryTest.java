package hu.psprog.leaflet.lags.core.service.mailing.impl;

import hu.psprog.leaflet.lags.core.service.mailing.MailFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link MailFactoryRegistry}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
public class MailFactoryRegistryTest {

    @Mock
    private MailFactory<?> mailFactory1;

    @Mock
    private MessageSource messageSource;

    private SignUpConfirmationMailFactory signUpConfirmationMailFactory;

    private MailFactoryRegistry mailFactoryRegistry;

    @BeforeEach
    public void setup() {
        signUpConfirmationMailFactory = new SignUpConfirmationMailFactory(messageSource);
        mailFactoryRegistry = new MailFactoryRegistry(Arrays.asList(mailFactory1, signUpConfirmationMailFactory));
    }

    @Test
    public void shouldReturnKnownFactory() {

        // when
        MailFactory<?> result = mailFactoryRegistry.getFactory(SignUpConfirmationMailFactory.class);

        // then
        assertThat(result, notNullValue());
        assertThat(result, equalTo(signUpConfirmationMailFactory));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForUnknownFactory() {

        // when
        Assertions.assertThrows(IllegalArgumentException.class, () -> mailFactoryRegistry.getFactory(MailFactory.class));

        // then
        // exception expected
    }
}
