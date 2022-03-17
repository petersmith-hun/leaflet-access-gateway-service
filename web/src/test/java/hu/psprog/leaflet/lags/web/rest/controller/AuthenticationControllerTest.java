package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
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

    private void assertSignUpModelAndView(ModelAndView result, boolean expectValidationError) {

        assertThat(result.getViewName(), equalTo("views/signup"));
        assertThat(result.getModel().size(), equalTo(2));
        assertThat(result.getModel().get("recaptchaSiteKey"), equalTo(RECAPTCHA_SITE_KEY));
        assertThat(result.getModel().get("validationError"), is(expectValidationError));
    }
}
