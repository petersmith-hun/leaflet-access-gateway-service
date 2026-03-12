package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.response.AccountDeletionModel;
import hu.psprog.leaflet.lags.core.domain.response.PasswordUpdateModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.UserProfileService;
import hu.psprog.leaflet.lags.web.factory.ProfileViewFactory;
import hu.psprog.leaflet.lags.web.model.ProfileScreen;
import hu.psprog.leaflet.lags.web.utility.AccountVerificationUtility;
import hu.psprog.leaflet.lags.web.utility.ReturnDirectiveUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link ProfileController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private ReturnDirectiveUtility returnDirectiveUtility;

    @Mock
    private ProfileViewFactory profileViewFactory;

    @Mock
    private AccountVerificationUtility accountVerificationUtility;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Authentication authentication;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ModelAndView modelAndView;

    @InjectMocks
    private ProfileController profileController;

    @Test
    public void shouldRenderProfileMainScreen() {

        // given
        var userDetails = UserDetailsResponse.builder().username("username").build();
        var localAccountFlag = true;
        var logoutReference = "/profile?return=dev";
        var returnDefinition = new AuthenticationConfig.ReturnDefinition();

        var expectedModel = Map.of(
                "accountData", userDetails,
                "localAccount", localAccountFlag,
                "logoutRef", logoutReference,
                "returnTo", returnDefinition
        );

        given(userProfileService.getUserDetails(authentication)).willReturn(userDetails);
        given(accountVerificationUtility.isLocalAccount(userDetails)).willReturn(localAccountFlag);
        given(profileViewFactory.createLogoutReference(request)).willReturn(logoutReference);
        given(returnDirectiveUtility.getReturnDefinition(request)).willReturn(returnDefinition);
        given(profileViewFactory.createView(ProfileScreen.MAIN, expectedModel)).willReturn(modelAndView);

        // when
        var result = profileController.renderProfileMainScreen(request, authentication);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(returnDirectiveUtility).ensureReturnDirective(request);
    }

    @Test
    public void shouldRenderEditAccountScreen() {

        // given
        var profileModel = ProfileModel.builder().username("username").build();
        var expectedModel = Map.<String, Object>of("accountData", profileModel);

        given(userProfileService.getProfile(authentication)).willReturn(profileModel);
        given(profileViewFactory.createView(ProfileScreen.EDIT_ACCOUNT, expectedModel))
                .willReturn(modelAndView);

        // when
        var result = profileController.renderEditAccountScreen(request, authentication, profileModel);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(returnDirectiveUtility).ensureReturnDirective(request);
        verify(accountVerificationUtility).assertLocalAccount(authentication);
    }

    @Test
    public void shouldSaveProfileWithSuccess() {

        // given
        var profileModel = ProfileModel.builder().username("username").build();

        given(bindingResult.hasErrors()).willReturn(false);
        given(userProfileService.updateProfile(authentication, profileModel))
                .willReturn(ProfileOperationResult.SUCCESS);
        given(profileViewFactory.createRedirection(ProfileScreen.EDIT_ACCOUNT, ProfileOperationResult.SUCCESS))
                .willReturn(modelAndView);

        // when
        var result = profileController.saveProfile(authentication, profileModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(accountVerificationUtility).assertLocalAccount(authentication);
    }

    @Test
    public void shouldSaveProfileWithValidationError() {

        // given
        var profileModel = ProfileModel.builder().username("username").build();
        var expectedModel = Map.<String, Object>of("accountData", profileModel);

        given(bindingResult.hasErrors()).willReturn(true);
        given(profileViewFactory.createView(ProfileScreen.EDIT_ACCOUNT, expectedModel))
                .willReturn(modelAndView);

        // when
        var result = profileController.saveProfile(authentication, profileModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(accountVerificationUtility).assertLocalAccount(authentication);
        verifyNoInteractions(userProfileService);
    }

    @Test
    public void shouldRenderChangePasswordScreen() {

        // given
        given(profileViewFactory.createView(ProfileScreen.CHANGE_PASSWORD)).willReturn(modelAndView);

        // when
        var result = profileController.renderChangePasswordScreen(request, authentication);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(returnDirectiveUtility).ensureReturnDirective(request);
        verify(accountVerificationUtility).assertLocalAccount(authentication);
    }

    @Test
    public void shouldSavePasswordWithSuccess() {

        // given
        var passwordUpdateModel = PasswordUpdateModel.builder().currentPassword("password").build();

        given(bindingResult.hasErrors()).willReturn(false);
        given(userProfileService.updatePassword(authentication, passwordUpdateModel))
                .willReturn(ProfileOperationResult.SUCCESS);
        given(profileViewFactory.createRedirection(ProfileScreen.CHANGE_PASSWORD, ProfileOperationResult.SUCCESS))
                .willReturn(modelAndView);

        // when
        var result = profileController.savePassword(authentication, passwordUpdateModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(accountVerificationUtility).assertLocalAccount(authentication);
    }

    @Test
    public void shouldSavePasswordWithValidationError() {

        // given
        var passwordUpdateModel = PasswordUpdateModel.builder().currentPassword("password").build();

        given(bindingResult.hasErrors()).willReturn(true);
        given(profileViewFactory.createView(ProfileScreen.CHANGE_PASSWORD)).willReturn(modelAndView);

        // when
        var result = profileController.savePassword(authentication, passwordUpdateModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(accountVerificationUtility).assertLocalAccount(authentication);
        verifyNoInteractions(userProfileService);
    }

    @Test
    public void shouldRenderDeleteAccountScreen() {

        // given
        var localAccountFlag = true;
        var expectedModel = Map.<String, Object>of("localAccount", localAccountFlag);

        given(accountVerificationUtility.isLocalAccount(authentication))
                .willReturn(localAccountFlag);
        given(profileViewFactory.createView(ProfileScreen.DELETE_ACCOUNT, expectedModel))
                .willReturn(modelAndView);

        // when
        var result = profileController.renderDeleteAccountScreen(request, authentication);

        // then
        assertThat(result, equalTo(modelAndView));

        verify(returnDirectiveUtility).ensureReturnDirective(request);
    }

    @Test
    public void shouldDeleteAccount() {

        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var accountDeletionModel = AccountDeletionModel.builder().currentPassword("pw").build();

        given(bindingResult.hasErrors()).willReturn(false);
        given(userProfileService.deleteAccount(authentication, accountDeletionModel.getCurrentPassword()))
                .willReturn(ProfileOperationResult.SUCCESS);
        given(profileViewFactory.redirectTo(ProfileScreen.LOGIN, ProfileOperationResult.SUCCESS))
                .willReturn(modelAndView);

        // when
        var result = profileController.deleteAccount(authentication, accountDeletionModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
    }

    @Test
    public void shouldDeleteAccountReturnWithValidationError() {

        // given
        var accountDeletionModel = AccountDeletionModel.builder().currentPassword("pw").build();

        given(bindingResult.hasErrors()).willReturn(true);
        given(profileViewFactory.createView(ProfileScreen.DELETE_ACCOUNT)).willReturn(modelAndView);

        // when
        var result = profileController.deleteAccount(authentication, accountDeletionModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));

        verifyNoInteractions(userProfileService);
    }

    @Test
    public void shouldDeleteAccountReturnWithAnUnknownError() {

        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var accountDeletionModel = AccountDeletionModel.builder().currentPassword("pw").build();
        var operationResult = ProfileOperationResult.UNKNOWN_ERROR;

        given(bindingResult.hasErrors()).willReturn(false);
        given(userProfileService.deleteAccount(authentication, accountDeletionModel.getCurrentPassword()))
                .willReturn(operationResult);
        given(profileViewFactory.redirectTo(ProfileScreen.DELETE_ACCOUNT, operationResult)).willReturn(modelAndView);

        // when
        var result = profileController.deleteAccount(authentication, accountDeletionModel, bindingResult);

        // then
        assertThat(result, equalTo(modelAndView));
        assertThat(SecurityContextHolder.getContext().getAuthentication(), equalTo(authentication));
    }
}
