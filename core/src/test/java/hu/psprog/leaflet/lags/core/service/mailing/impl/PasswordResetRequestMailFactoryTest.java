package hu.psprog.leaflet.lags.core.service.mailing.impl;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequest;
import hu.psprog.leaflet.mail.domain.Mail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link PasswordResetRequestMailFactory}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
public class PasswordResetRequestMailFactoryTest {

    private static final String SUBJECT = "mail.user.pwreset.demand.subject";
    private static final String TRANSLATED_SUBJECT = "Password reset requested";
    private static final String TEMPLATE = "pw_reset_request.html";
    private static final String RECIPIENT = "test@local.dev";

    private static final String GENERATED_AT = "generatedAt";
    private static final String EXPIRATION = "expiration";
    private static final String USERNAME = "username";
    private static final String TOKEN = "token";
    private static final String RESET_LINK = "resetLink";

    private static final String RESET_URL = "http://localhost:9000/pwreset";
    private static final Locale FORCED_LOCALE = Locale.ENGLISH;

    private static final AuthenticationConfig.PasswordResetConfig PASSWORD_RESET_CONFIG = new AuthenticationConfig.PasswordResetConfig();

    static {
        PASSWORD_RESET_CONFIG.setReturnUrl(RESET_URL);
    }

    @Mock
    private MessageSource messageSource;

    @Mock
    private AuthenticationConfig authenticationConfig;

    @InjectMocks
    private PasswordResetRequestMailFactory passwordResetRequestMailFactory;

    @BeforeEach
    public void setup() {
        passwordResetRequestMailFactory.setForcedLocale(FORCED_LOCALE);
    }

    @Test
    public void shouldBuildMail() {

        // given
        PasswordResetRequest passwordResetRequest = preparePasswordResetRequest();
        given(messageSource.getMessage(SUBJECT, null, SUBJECT, FORCED_LOCALE)).willReturn(TRANSLATED_SUBJECT);
        given(authenticationConfig.getPasswordReset()).willReturn(PASSWORD_RESET_CONFIG);

        // when
        Mail result = passwordResetRequestMailFactory.buildMail(passwordResetRequest, RECIPIENT);

        // then
        assertGeneratedMail(result, passwordResetRequest);
    }

    @Test
    public void shouldThrowExceptionOnNullRecipient() {

        // given
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> passwordResetRequestMailFactory.buildMail(PasswordResetRequest.builder().build()));

        // then
        // exception expected
    }

    @Test
    public void shouldThrowExceptionOnMultipleRecipients() {

        // given
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> passwordResetRequestMailFactory.buildMail(PasswordResetRequest.builder().build(), RECIPIENT, RECIPIENT));

        // then
        // exception expected
    }

    private void assertGeneratedMail(Mail result, PasswordResetRequest passwordResetRequest) {

        assertThat(result, notNullValue());
        assertThat(result.getRecipient(), equalTo(RECIPIENT));
        assertThat(result.getSubject(), equalTo(TRANSLATED_SUBJECT));
        assertThat(result.getTemplate(), equalTo(TEMPLATE));
        assertThat(result.getContentMap().get(TOKEN), equalTo(passwordResetRequest.getToken()));
        assertThat(result.getContentMap().get(USERNAME), equalTo(passwordResetRequest.getUsername()));
        assertThat(result.getContentMap().get(EXPIRATION), equalTo(passwordResetRequest.getExpiration()));
        assertThat(result.getContentMap().get(RESET_LINK), equalTo(RESET_URL));
        assertThat(result.getContentMap().get(GENERATED_AT), notNullValue());
    }

    private PasswordResetRequest preparePasswordResetRequest() {

        return PasswordResetRequest.builder()
                .token("token")
                .expiration(1)
                .username("username")
                .build();
    }
}