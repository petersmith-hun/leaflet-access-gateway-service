package hu.psprog.leaflet.lags.acceptance.utility;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for HTTP call related operations..
 *
 * @author Peter Smith
 */
public class HTTPUtility {

    private HTTPUtility() {
    }

    /**
     * Extracts the given query parameter from the given URI.
     *
     * @param uri {@link URI} to extract query parameter from
     * @param queryParameterName query parameter name as {@link TestConstants.Attribute}
     * @return extracted query parameter value as {@link String} or {@code null} if not present
     */
    public static String getQueryParameter(URI uri, TestConstants.Attribute queryParameterName) {

        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .get(queryParameterName.getValue())
                .stream()
                .findFirst()
                .map(value -> URLDecoder.decode(value, StandardCharsets.UTF_8))
                .orElse(null);
    }

    /**
     * Extracts the value of the {@code Set-Cookie} header from the currently stored {@link ResponseEntity} object.
     *
     * @return value of the {@code Set-Cookie} header
     */
    public static String extractCookieFromResponse() {

        return ThreadLocalDataRegistry.getResponseEntity()
                .getHeaders()
                .getFirst("Set-Cookie");
    }
}
