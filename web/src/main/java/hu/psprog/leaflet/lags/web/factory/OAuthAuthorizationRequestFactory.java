package hu.psprog.leaflet.lags.web.factory;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Factory implementation to properly create an {@link OAuthAuthorizationRequest} from the received authorization parameters.
 *
 * @author Peter Smith
 */
@Component
public class OAuthAuthorizationRequestFactory {

    private static final List<String> MANDATORY_FIELDS = Arrays.asList(
            OAuthConstants.Request.RESPONSE_TYPE,
            OAuthConstants.Request.CLIENT_ID,
            OAuthConstants.Request.REDIRECT_URI,
            OAuthConstants.Request.STATE
    );

    /**
     * Creates an {@link OAuthAuthorizationRequest} object by extracting the necessary ones from the received
     * request parameters. Since most of these parameters are mandatory, a validation is also done here.
     *
     * @param requestParameters authorization request parameters as a single {@link Map}
     * @return created {@link OAuthAuthorizationRequest} object
     * @throws OAuthAuthorizationException in case of missing / empty mandatory parameters
     */
    public OAuthAuthorizationRequest createAuthorizationRequest(Map<String, String> requestParameters) {

        validateRequest(requestParameters);

        return OAuthAuthorizationRequest.builder()
                .responseType(AuthorizationResponseType.parseResponseType(requestParameters.get(OAuthConstants.Request.RESPONSE_TYPE)))
                .clientID(requestParameters.get(OAuthConstants.Request.CLIENT_ID))
                .redirectURI(requestParameters.get(OAuthConstants.Request.REDIRECT_URI))
                .scope(requestParameters.get(OAuthConstants.Request.SCOPE))
                .state(requestParameters.get(OAuthConstants.Request.STATE))
                .build();
    }

    private void validateRequest(Map<String, String> requestParameters) {

        MANDATORY_FIELDS.forEach(field -> {
            if (StringUtils.isEmpty(requestParameters.get(field))) {
                throw new OAuthAuthorizationException(OAuthErrorCode.INVALID_REQUEST, String.format("A mandatory field [%s] is missing from request", field));
            }
        });
    }
}
