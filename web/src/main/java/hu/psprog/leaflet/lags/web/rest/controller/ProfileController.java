package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.response.AccountDeletionModel;
import hu.psprog.leaflet.lags.core.domain.response.PasswordUpdateModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.UserProfileService;
import hu.psprog.leaflet.lags.web.factory.ProfileViewFactory;
import hu.psprog.leaflet.lags.web.model.ProfileScreen;
import hu.psprog.leaflet.lags.web.security.Permit;
import hu.psprog.leaflet.lags.web.utility.AccountVerificationUtility;
import hu.psprog.leaflet.lags.web.utility.ReturnDirectiveUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Controller for user profile management operations.
 *
 * @author Peter Smith
 */
@Slf4j
@Controller
public class ProfileController {

    private final UserProfileService userProfileService;
    private final ReturnDirectiveUtility returnDirectiveUtility;
    private final ProfileViewFactory profileViewFactory;
    private final AccountVerificationUtility accountVerificationUtility;

    @Autowired
    public ProfileController(UserProfileService userProfileService, ReturnDirectiveUtility returnDirectiveUtility,
                             ProfileViewFactory profileViewFactory, AccountVerificationUtility accountVerificationUtility) {
        this.userProfileService = userProfileService;
        this.returnDirectiveUtility = returnDirectiveUtility;
        this.profileViewFactory = profileViewFactory;
        this.accountVerificationUtility = accountVerificationUtility;
    }

    /**
     * Renders the main screen of the profile management section.
     *
     * @param request {@link HttpServletRequest} object to extract the logout reference and the return directive
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @return populated {@link ModelAndView} object
     */
    @Permit.Read.Profile
    @GetMapping("/profile")
    public ModelAndView renderProfileMainScreen(HttpServletRequest request, Authentication authentication) {

        returnDirectiveUtility.ensureReturnDirective(request);
        UserDetailsResponse userDetails = userProfileService.getUserDetails(authentication);

        return profileViewFactory.createView(ProfileScreen.MAIN, Map.of(
                "accountData", userDetails,
                "localAccount", accountVerificationUtility.isLocalAccount(userDetails),
                "logoutRef", profileViewFactory.createLogoutReference(request),
                "returnTo", returnDirectiveUtility.getReturnDefinition(request)
        ));
    }

    /**
     * Renders the edit account screen, populated with the current profile data of the active user.
     *
     * @param request {@link HttpServletRequest} object verify the presence of the return directive
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @param profileModel {@link ProfileModel} model attribute to bind the form to
     * @return populated {@link ModelAndView} object
     */
    @Permit.Read.Profile
    @GetMapping("/profile/edit-account")
    public ModelAndView renderEditAccountScreen(HttpServletRequest request, Authentication authentication, @ModelAttribute ProfileModel profileModel) {

        returnDirectiveUtility.ensureReturnDirective(request);
        accountVerificationUtility.assertLocalAccount(authentication);

        return profileViewFactory.createView(ProfileScreen.EDIT_ACCOUNT, Map.of(
                "accountData", userProfileService.getProfile(authentication)
        ));
    }

    /**
     * Executes saving the updated profile data. Redirects to the main screen on success, or the editor screen with
     * showing the validation error messages on validation error.
     *
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @param profileModel {@link ProfileModel} model attribute the form is bound to
     * @param bindingResult {@link BindingResult} object to check if there was any validation error while processing the request
     * @return populated {@link ModelAndView} object
     */
    @Permit.Write.Profile
    @PostMapping("/profile/edit-account")
    public ModelAndView saveProfile(Authentication authentication, @Valid @ModelAttribute ProfileModel profileModel, BindingResult bindingResult) {

        accountVerificationUtility.assertLocalAccount(authentication);

        return bindingResult.hasErrors()
                ? profileViewFactory.createView(ProfileScreen.EDIT_ACCOUNT, Map.of("accountData", profileModel))
                : profileViewFactory.createRedirection(ProfileScreen.EDIT_ACCOUNT, userProfileService.updateProfile(authentication, profileModel));
    }

    /**
     * Renders the password update screen.
     *
     * @param request {@link HttpServletRequest} object verify the presence of the return directive
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @return populated {@link ModelAndView} object
     */
    @Permit.Read.Profile
    @GetMapping("/profile/change-password")
    public ModelAndView renderChangePasswordScreen(HttpServletRequest request, Authentication authentication) {

        returnDirectiveUtility.ensureReturnDirective(request);
        accountVerificationUtility.assertLocalAccount(authentication);

        return profileViewFactory.createView(ProfileScreen.CHANGE_PASSWORD);
    }

    /**
     * Executes saving the updated password. Redirects to the main screen on success, or the editor screen with showing
     * the validation error messages on validation error.
     *
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @param passwordUpdateModel {@link PasswordUpdateModel} model attribute the form is bound to
     * @param bindingResult {@link BindingResult} object to check if there was any validation error while processing the request
     * @return populated {@link ModelAndView} object
     */
    @Permit.Write.Profile
    @PostMapping("/profile/change-password")
    public ModelAndView savePassword(Authentication authentication, @ModelAttribute PasswordUpdateModel passwordUpdateModel, BindingResult bindingResult) {

        accountVerificationUtility.assertLocalAccount(authentication);

        return bindingResult.hasErrors()
                ? profileViewFactory.createView(ProfileScreen.CHANGE_PASSWORD)
                : profileViewFactory.createRedirection(ProfileScreen.CHANGE_PASSWORD, userProfileService.updatePassword(authentication, passwordUpdateModel));
    }

    /**
     * Renders the account deletion form.
     *
     * @param request {@link HttpServletRequest} object verify the presence of the return directive
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @return populated {@link ModelAndView} object
     */
    @Permit.Read.Profile
    @GetMapping("/profile/delete-account")
    public ModelAndView renderDeleteAccountScreen(HttpServletRequest request, Authentication authentication) {

        returnDirectiveUtility.ensureReturnDirective(request);

        return profileViewFactory.createView(ProfileScreen.DELETE_ACCOUNT, Map.of(
                "localAccount", accountVerificationUtility.isLocalAccount(authentication)
        ));
    }

    /**
     * Executes profile deletion. Signs the user out and redirects to the login screen on success, or the editor screen
     * with showing the validation error messages on validation error.
     *
     * @param authentication {@link Authentication} object to extract the ID of the currently active user
     * @param accountDeletionModel {@link AccountDeletionModel} model attribute the form is bound to
     * @param bindingResult {@link BindingResult} object to check if there was any validation error while processing the request
     * @return populated {@link ModelAndView} object
     */
    @Permit.Write.ProfileDelete
    @PostMapping("/profile/delete-account")
    public ModelAndView deleteAccount(Authentication authentication, @ModelAttribute AccountDeletionModel accountDeletionModel, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return profileViewFactory.createView(ProfileScreen.DELETE_ACCOUNT);
        }

        ProfileOperationResult result = userProfileService.deleteAccount(authentication, accountDeletionModel.getCurrentPassword());

        if (result == ProfileOperationResult.SUCCESS) {
            SecurityContextHolder.clearContext();
            return profileViewFactory.redirectTo(ProfileScreen.LOGIN, ProfileOperationResult.SUCCESS);
        }

        return profileViewFactory.redirectTo(ProfileScreen.DELETE_ACCOUNT, result);
    }

}
