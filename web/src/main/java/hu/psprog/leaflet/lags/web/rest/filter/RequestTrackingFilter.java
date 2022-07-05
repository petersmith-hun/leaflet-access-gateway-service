package hu.psprog.leaflet.lags.web.rest.filter;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Request tracking filter utilizing the SLF4J MDC API to store an identifier unique to each request.
 *
 * @author Peter Smith
 */
@Component
@Order(Integer.MIN_VALUE)
public class RequestTrackingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "request_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            registerRequestInMDC();
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void registerRequestInMDC() {
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
    }
}
