package hu.psprog.leaflet.lags.web.rest.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RequestTrackingFilter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class RequestTrackingFilterTest {

    private static final String REQUEST_ID = "request_id";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RequestTrackingFilter requestTrackingFilter;

    @Test
    public void shouldDoFilterInternalRegisterRequestInMDCAndClearAfterwards() throws ServletException, IOException {

        try (MockedStatic<MDC> mockedMDC = Mockito.mockStatic(MDC.class)) {

            // when
            requestTrackingFilter.doFilterInternal(request, response, filterChain);

            // then
            mockedMDC.verify(() -> MDC.put(eq(REQUEST_ID), notNull()));
            mockedMDC.verify(MDC::clear);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Test
    public void shouldDoFilterInternalRegisterRequestInMDCAndClearEvenAfterException() throws ServletException, IOException {

        try (MockedStatic<MDC> mockedMDC = Mockito.mockStatic(MDC.class)) {

            // given
            doThrow(RuntimeException.class).when(filterChain).doFilter(request, response);

            // when
            assertThrows(RuntimeException.class, () -> requestTrackingFilter.doFilterInternal(request, response, filterChain));

            // then
            mockedMDC.verify(() -> MDC.put(eq(REQUEST_ID), notNull()));
            mockedMDC.verify(MDC::clear);
            verify(filterChain).doFilter(request, response);
        }
    }
}
