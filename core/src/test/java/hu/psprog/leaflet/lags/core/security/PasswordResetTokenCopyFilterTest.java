package hu.psprog.leaflet.lags.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link PasswordResetTokenCopyFilter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetTokenCopyFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Captor
    private ArgumentCaptor<HttpServletRequest> captor;

    @InjectMocks
    private PasswordResetTokenCopyFilter filter;

    @Test
    public void shouldDoFilterInternalChainImmediatelyForAnyRequestOtherThanPasswordResetConfirmation() throws ServletException, IOException {

        // given
        given(request.getRequestURI()).willReturn("/access-management/permissions");

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        verifyNoMoreInteractions(request);

        assertThat(captor.getValue().getHeader("Authorization"), nullValue());
    }

    @Test
    public void shouldDoFilterInternalReturnAuthorizationHeaderFromQuery() throws ServletException, IOException {

        // given
        given(request.getRequestURI()).willReturn("/password-reset/confirmation");
        given(request.getParameter("token")).willReturn("access-token");

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        verifyNoMoreInteractions(request);

        assertThat(captor.getValue().getHeader("Authorization"), equalTo("Bearer access-token"));
    }

    @Test
    public void shouldDoFilterInternalReturnAnyOtherHeaderWithoutAnychange() throws ServletException, IOException {

        // given
        given(request.getRequestURI()).willReturn("/password-reset/confirmation");
        given(request.getHeader("Content-Type")).willReturn("application/json");

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        verify(chain).doFilter(captor.capture(), eq(response));
        verifyNoMoreInteractions(request);

        assertThat(captor.getValue().getHeader("Content-Type"), equalTo("application/json"));
    }
}
