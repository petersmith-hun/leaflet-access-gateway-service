package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.AccountType;
import hu.psprog.leaflet.lags.core.domain.Role;
import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link SignUpRequestModelToUserConverter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class SignUpRequestModelToUserConverterTest {

    private static final String USERNAME = "Local User";
    private static final String EMAIL = "user@dev.local";
    private static final String PASSWORD = "password1";
    private static final boolean USER_ENABLED_BY_DEFAULT = true;
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final SignUpRequestModel SIGN_UP_REQUEST_MODEL = new SignUpRequestModel();

    static {
        SIGN_UP_REQUEST_MODEL.setUsername(USERNAME);
        SIGN_UP_REQUEST_MODEL.setEmail(EMAIL);
        SIGN_UP_REQUEST_MODEL.setPassword(PASSWORD);
    }

    @Mock
    private AuthenticationConfig authenticationConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignUpRequestModelToUserConverter signUpRequestModelToUserConverter;

    @Test
    public void shouldConvertSignUpRequestToLocalUserObject() {

        // given
        given(authenticationConfig.isUserEnabledByDefault()).willReturn(USER_ENABLED_BY_DEFAULT);
        given(authenticationConfig.getDefaultLocale()).willReturn(DEFAULT_LOCALE);
        given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        User result = signUpRequestModelToUserConverter.convert(SIGN_UP_REQUEST_MODEL);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getUsername(), equalTo(USERNAME));
        assertThat(result.getEmail(), equalTo(EMAIL));
        assertThat(result.getPassword(), equalTo(ENCODED_PASSWORD));
        assertThat(result.isEnabled(), is(USER_ENABLED_BY_DEFAULT));
        assertThat(result.getDefaultLocale(), equalTo(DEFAULT_LOCALE.toString()));
        assertThat(result.getRole(), equalTo(Role.USER));
        assertThat(result.getAccountType(), equalTo(AccountType.LOCAL));
        assertThat(System.currentTimeMillis() - result.getCreated().getTime() < 100, is(true));
        assertThat(result.getLastLogin(), nullValue());
        assertThat(result.getLastModified(), nullValue());
    }
}
