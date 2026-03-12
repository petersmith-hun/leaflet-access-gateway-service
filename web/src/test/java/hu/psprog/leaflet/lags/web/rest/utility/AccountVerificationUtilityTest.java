package hu.psprog.leaflet.lags.web.rest.utility;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.UserProfileService;
import hu.psprog.leaflet.lags.web.exception.NonLocalAccountEditAttemptException;
import hu.psprog.leaflet.lags.web.utility.AccountVerificationUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link AccountVerificationUtility}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AccountVerificationUtilityTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountVerificationUtility accountVerificationUtility;

    @Test
    public void shouldAssertLocalAccountWithSuccess() {

        // given
        var userDetails = UserDetailsResponse.builder()
                .accountType(AccountType.LOCAL)
                .build();

        given(userProfileService.getUserDetails(authentication)).willReturn(userDetails);

        // when
        accountVerificationUtility.assertLocalAccount(authentication);

        // then
        // silent fallthrough expected
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class, mode = EnumSource.Mode.EXCLUDE, names = "LOCAL")
    public void shouldAssertLocalAccountFailWithException(AccountType accountType) {

        // given
        var userDetails = UserDetailsResponse.builder()
                .accountType(accountType)
                .build();

        given(userProfileService.getUserDetails(authentication)).willReturn(userDetails);

        // when
        assertThrows(NonLocalAccountEditAttemptException.class,
                () -> accountVerificationUtility.assertLocalAccount(authentication));

        // then
        // exception expected
    }

    @Test
    public void shouldIsLocalAccountReturnTrue() {

        // given
        var userDetails = UserDetailsResponse.builder()
                .accountType(AccountType.LOCAL)
                .build();

        given(userProfileService.getUserDetails(authentication)).willReturn(userDetails);

        // when
        var result = accountVerificationUtility.isLocalAccount(authentication);

        // then
        assertThat(result, is(true));
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class, mode = EnumSource.Mode.EXCLUDE, names = "LOCAL")
    public void shouldIsLocalAccountReturnFalse(AccountType accountType) {

        // given
        var userDetails = UserDetailsResponse.builder()
                .accountType(accountType)
                .build();

        given(userProfileService.getUserDetails(authentication)).willReturn(userDetails);

        // when
        var result = accountVerificationUtility.isLocalAccount(authentication);

        // then
        assertThat(result, is(false));
    }
}
