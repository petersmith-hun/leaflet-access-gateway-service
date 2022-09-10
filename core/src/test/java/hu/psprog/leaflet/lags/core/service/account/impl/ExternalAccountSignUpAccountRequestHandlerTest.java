package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link ExternalAccountSignUpAccountRequestHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ExternalAccountSignUpAccountRequestHandlerTest {

    private static final String EMAIL = "externaluser1@dev.local";
    private static final ExternalUserDefinition<Long> EXTERNAL_USER_DEFINITION = prepareExternalUserDefinition();

    @Mock
    private UserDAO userDAO;

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ExternalAccountSignUpAccountRequestHandler externalAccountSignUpAccountRequestHandler;

    @Test
    public void shouldProcessAccountRequestSuccessfullySaveNewExternalUser() {

        // given
        User externalUser = prepareUser(AccountType.GITHUB);

        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.empty());
        given(conversionService.convert(EXTERNAL_USER_DEFINITION, User.class)).willReturn(externalUser);

        // when
        SignUpStatus result = externalAccountSignUpAccountRequestHandler.processAccountRequest(EXTERNAL_USER_DEFINITION);

        // then
        assertThat(result, equalTo(SignUpStatus.SUCCESS));

        verify(userDAO).save(externalUser);
    }

    @Test
    public void shouldProcessAccountRequestSuccessfullyVerifyExistingExternalUser() {

        // given
        User externalUser = prepareUser(AccountType.GITHUB);

        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(externalUser));

        // when
        SignUpStatus result = externalAccountSignUpAccountRequestHandler.processAccountRequest(EXTERNAL_USER_DEFINITION);

        // then
        assertThat(result, equalTo(SignUpStatus.SUCCESS));

        verifyNoMoreInteractions(userDAO);
        verifyNoInteractions(conversionService);
    }

    @Test
    public void shouldProcessAccountRequestFailToSaveNewExternalUser() {

        // given
        User externalUser = prepareUser(AccountType.GITHUB);

        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.empty());
        given(conversionService.convert(EXTERNAL_USER_DEFINITION, User.class)).willReturn(externalUser);
        doThrow(RuntimeException.class).when(userDAO).save(externalUser);

        // when
        SignUpStatus result = externalAccountSignUpAccountRequestHandler.processAccountRequest(EXTERNAL_USER_DEFINITION);

        // then
        assertThat(result, equalTo(SignUpStatus.FAILURE));
    }

    @Test
    public void shouldProcessAccountRequestFailToVerifyExistingExternalUser() {

        // given
        User externalUser = prepareUser(AccountType.LOCAL);

        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(externalUser));

        // when
        SignUpStatus result = externalAccountSignUpAccountRequestHandler.processAccountRequest(EXTERNAL_USER_DEFINITION);

        // then
        assertThat(result, equalTo(SignUpStatus.ADDRESS_IN_USE));
    }

    private static ExternalUserDefinition<Long> prepareExternalUserDefinition() {

        return ExternalUserDefinition.<Long>builder()
                .email(EMAIL)
                .accountType(AccountType.GITHUB)
                .build();
    }

    private User prepareUser(AccountType accountType) {

        User user = new User();
        user.setEmail(EMAIL);
        user.setAccountType(accountType);

        return user;
    }
}
