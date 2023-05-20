package hu.psprog.leaflet.lags.core.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link RequestSavingLogoutSuccessHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class RequestSavingLogoutSuccessHandlerTest {

    private final HttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RequestSavingLogoutSuccessHandler requestSavingLogoutSuccessHandler;

    @Test
    public void shouldOnLogoutSuccessSaveRequestBeforeFinalizingLogout() throws ServletException, IOException {

        // when
        requestSavingLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);

        // then
        assertThat(new HttpSessionRequestCache().getRequest(request, response) instanceof DefaultSavedRequest, is(true));
        assertThat(response.getStatus(), equalTo(302));
    }
}
