package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link ExternalUserDefinitionToUserConverter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ExternalUserDefinitionToUserConverterTest {

    private static final long USER_ID = 1234L;
    private static final String USERNAME = "External User";
    private static final String EMAIL = "externauser@dev.local";
    private static final boolean USER_ENABLED_BY_DEFAULT = true;
    private static final SupportedLocale DEFAULT_LOCALE = SupportedLocale.EN;
    private static final ExternalUserDefinition<Long> EXTERNAL_USER_DEFINITION = prepareExternalUserDefinition();

    @Mock
    private AuthenticationConfig authenticationConfig;

    @InjectMocks
    private ExternalUserDefinitionToUserConverter externalUserDefinitionToUserConverter;

    @Test
    public void shouldConvertSignUpRequestToLocalUserObject() {

        // given
        given(authenticationConfig.isUserEnabledByDefault()).willReturn(USER_ENABLED_BY_DEFAULT);
        given(authenticationConfig.getDefaultLocale()).willReturn(DEFAULT_LOCALE);

        // when
        User result = externalUserDefinitionToUserConverter.convert(EXTERNAL_USER_DEFINITION);

        // then
        assertThat(result, notNullValue());
        assertThat(result.getUsername(), equalTo(USERNAME));
        assertThat(result.getEmail(), equalTo(EMAIL));
        assertThat(result.isEnabled(), is(USER_ENABLED_BY_DEFAULT));
        assertThat(result.getDefaultLocale(), equalTo(DEFAULT_LOCALE));
        assertThat(result.getRole(), equalTo(Role.EXTERNAL_USER));
        assertThat(result.getAccountType(), equalTo(AccountType.GITHUB));
        assertThat(result.getExternalID(), equalTo("provider=github|ext_uid=1234"));
        assertThat(System.currentTimeMillis() - result.getCreated().getTime() < 100, is(true));
        assertThat(result.getLastLogin(), nullValue());
        assertThat(result.getLastModified(), nullValue());
        assertThat(result.getPassword(), nullValue());
    }

    private static ExternalUserDefinition<Long> prepareExternalUserDefinition() {

        return ExternalUserDefinition.<Long>builder()
                .username(USERNAME)
                .email(EMAIL)
                .accountType(AccountType.GITHUB)
                .userID(USER_ID)
                .build();
    }
}
