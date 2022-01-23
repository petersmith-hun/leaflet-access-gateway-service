package hu.psprog.leaflet.lags.web.rest.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link AuthenticationController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authenticationController;

    @Test
    public void shouldRenderLoginFormReturnPopulatedModelAndView() {

        // when
        ModelAndView result = authenticationController.renderLoginForm();

        // then
        assertThat(result.getViewName(), equalTo("login"));
    }
}
