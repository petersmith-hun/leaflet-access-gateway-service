package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.SignUpStatus;
import hu.psprog.leaflet.lags.core.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link AuthenticationController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private static final String RECAPTCHA_SITE_KEY = "recaptcha-site-key-1";
    private static final String SIGNUP_CALLBACK = "https://dev.local:443/signup/callback";
    private static final String EXPECTED_REDIRECT_URI_TEMPLATE = "redirect:https://dev.local:443/signup/callback?signup_status=%s";

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AuthenticationConfig authenticationConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    private AuthenticationController authenticationController;

    @BeforeEach
    public void setup() {
        given(authenticationConfig.getRecaptchaSiteKey()).willReturn(RECAPTCHA_SITE_KEY);
        authenticationController = new AuthenticationController(authenticationService, authenticationConfig);
    }

    @Test
    public void shouldRenderLoginFormReturnPopulatedModelAndView() {

        // when
        ModelAndView result = authenticationController.renderLoginForm();

        // then
        assertThat(result.getViewName(), equalTo("views/login"));
    }

    @Test
    public void shouldRenderEmptySignUpForm() {

        // given
        SignUpRequestModel signUpRequestModel = new SignUpRequestModel();

        // when
        ModelAndView result = authenticationController.renderSignUpForm(signUpRequestModel);

        // then
        assertSignUpModelAndView(result, false);
    }

    @Test
    public void shouldRenderSignUpFormAfterValidationError() {

        // given
        SignUpRequestModel signUpRequestModel = new SignUpRequestModel();
        signUpRequestModel.setValidationFailed(true);

        // when
        ModelAndView result = authenticationController.renderSignUpForm(signUpRequestModel);

        // then
        assertSignUpModelAndView(result, true);
    }

    @ParameterizedTest
    @EnumSource(SignUpStatus.class)
    public void shouldProcessSignUpWithoutValidationError(SignUpStatus signUpStatus) {

        // given
        SignUpRequestModel signUpRequestModel = new SignUpRequestModel();
        SignUpResult signUpResult = new SignUpResult(SIGNUP_CALLBACK, signUpStatus);

        given(bindingResult.hasErrors()).willReturn(false);
        given(authenticationService.signUp(signUpRequestModel, request)).willReturn(signUpResult);

        // when
        ModelAndView result = authenticationController.processSignUp(signUpRequestModel, bindingResult, request);

        // then
        assertThat(result.getViewName(), equalTo(String.format(EXPECTED_REDIRECT_URI_TEMPLATE, signUpStatus)));
    }

    @Test
    public void shouldProcessSignUpWithValidationError() {

        // given
        SignUpRequestModel signUpRequestModel = new SignUpRequestModel();

        given(bindingResult.hasErrors()).willReturn(true);

        // when
        ModelAndView result = authenticationController.processSignUp(signUpRequestModel, bindingResult, request);

        // then
        assertSignUpModelAndView(result, true);
        verifyNoInteractions(authenticationService);
    }

    @Test
    public void shouldRenderPasswordResetRequestForm() {

        // given
        PasswordResetRequestModel passwordResetRequestModel = new PasswordResetRequestModel();

        // when
        ModelAndView result = authenticationController.renderPasswordResetRequestForm(passwordResetRequestModel);

        // then
        assertPasswordResetRequestModelAndView(result, false);
    }

    @Test
    public void shouldRenderPasswordResetRequestFormAfterValidationError() {

        // given
        PasswordResetRequestModel passwordResetRequestModel = new PasswordResetRequestModel();
        passwordResetRequestModel.setValidationFailed(true);

        // when
        ModelAndView result = authenticationController.renderPasswordResetRequestForm(passwordResetRequestModel);

        // then
        assertPasswordResetRequestModelAndView(result, true);
    }

    @Test
    public void shouldProcessPasswordResetRequestWithSuccess() {

        // given
        PasswordResetRequestModel passwordResetRequestModel = new PasswordResetRequestModel();
        passwordResetRequestModel.setEmail("email");

        given(bindingResult.hasErrors()).willReturn(false);

        // when
        ModelAndView result = authenticationController.processPasswordResetRequest(passwordResetRequestModel, bindingResult, request);

        // then
        assertThat(result.getViewName(), equalTo("views/pw_reset_ack"));
        assertThat(result.getModel().isEmpty(), is(true));

        verify(authenticationService).requestPasswordReset(passwordResetRequestModel, request);
    }

    @Test
    public void shouldProcessPasswordResetRequestRenderFormOnValidationError() {

        // given
        PasswordResetRequestModel passwordResetRequestModel = new PasswordResetRequestModel();
        passwordResetRequestModel.setEmail("email");
        passwordResetRequestModel.setValidationFailed(false);

        given(bindingResult.hasErrors()).willReturn(true);

        // when
        ModelAndView result = authenticationController.processPasswordResetRequest(passwordResetRequestModel, bindingResult, request);

        // then
        assertThat(passwordResetRequestModel.isValidationFailed(), is(true));
        assertPasswordResetRequestModelAndView(result, true);

        verifyNoInteractions(authenticationService);
    }

    @Test
    public void shouldRenderPasswordResetConfirmationFormWithSuccess() {

        // given
        PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel = new PasswordResetConfirmationRequestModel();
        passwordResetConfirmationRequestModel.setPassword("pw1");

        // when
        ModelAndView result = authenticationController.renderPasswordResetConfirmationForm(passwordResetConfirmationRequestModel);

        // then
        assertPasswordResetConfirmationModelAndView(result, false);
    }

    @Test
    public void shouldRenderPasswordResetConfirmationFormWithSuccessAfterValidationError() {

        // given
        PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel = new PasswordResetConfirmationRequestModel();
        passwordResetConfirmationRequestModel.setPassword("pw1");
        passwordResetConfirmationRequestModel.setValidationFailed(true);

        // when
        ModelAndView result = authenticationController.renderPasswordResetConfirmationForm(passwordResetConfirmationRequestModel);

        // then
        assertPasswordResetConfirmationModelAndView(result, true);
    }

    @Test
    public void shouldProcessPasswordResetConfirmationWithSuccess() {

        // given
        PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel = new PasswordResetConfirmationRequestModel();
        passwordResetConfirmationRequestModel.setPassword("pw1");

        // when
        ModelAndView result = authenticationController.processPasswordResetConfirmation(passwordResetConfirmationRequestModel, bindingResult, request);

        // then
        assertThat(result.getViewName(), equalTo("redirect:/login?pwreset_status=success"));
        assertThat(result.getModel().isEmpty(), is(true));

        verify(authenticationService).confirmPasswordReset(passwordResetConfirmationRequestModel, request);
    }

    @Test
    public void shouldProcessPasswordResetConfirmationRenderFormOnValidationError() {

        // given
        PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel = new PasswordResetConfirmationRequestModel();
        passwordResetConfirmationRequestModel.setPassword("pw1");

        given(bindingResult.hasErrors()).willReturn(true);

        // when
        ModelAndView result = authenticationController.processPasswordResetConfirmation(passwordResetConfirmationRequestModel, bindingResult, request);

        // then
        assertPasswordResetConfirmationModelAndView(result, true);

        verifyNoInteractions(authenticationService);
    }

    private void assertSignUpModelAndView(ModelAndView result, boolean expectValidationError) {

        assertThat(result.getViewName(), equalTo("views/signup"));
        assertCommonModelEntries(result, expectValidationError);
    }

    private void assertPasswordResetRequestModelAndView(ModelAndView result, boolean expectValidationError) {

        assertThat(result.getViewName(), equalTo("views/pw_reset_request"));
        assertCommonModelEntries(result, expectValidationError);
    }

    private void assertPasswordResetConfirmationModelAndView(ModelAndView result, boolean expectValidationError) {

        assertThat(result.getViewName(), equalTo("views/pw_reset_confirm"));
        assertCommonModelEntries(result, expectValidationError);
    }

    private void assertCommonModelEntries(ModelAndView result, boolean expectValidationError) {

        assertThat(result.getModel().size(), equalTo(2));
        assertThat(result.getModel().get("recaptchaSiteKey"), equalTo(RECAPTCHA_SITE_KEY));
        assertThat(result.getModel().get("validationError"), is(expectValidationError));
    }
}
