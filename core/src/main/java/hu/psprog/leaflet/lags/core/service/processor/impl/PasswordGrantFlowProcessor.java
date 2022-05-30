package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link AbstractGrantFlowProcessor} implementation for resource owner password grant OAuth2 authorization flow processing.
 *
 * Flow specific verification includes:
 *  - validations of username and password fields (both must be present in the request to continue processing).
 *  - authentication of the specified resource owner user;
 *  - checking and updating the scope if necessary (when scope is not specified, default user role specified scope set is applied);
 *  - updates the security context to include the authentication object of the authenticated resource owner user (for further processing later);
 *  - updating the subject claim to include the user ID and adding some further JWT claims (username, role, user display name, user ID).
 *
 * @author Peter Smith
 */
@Component
public class PasswordGrantFlowProcessor extends AbstractGrantFlowProcessor {

    private final AuthenticationProvider authenticationProvider;

    @Autowired
    public PasswordGrantFlowProcessor(@Qualifier("localUserAuthenticationProvider") AuthenticationProvider authenticationProvider,
                                      OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry) {
        super(oAuthRequestVerifierRegistry);
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public GrantType forGrantType() {
        return GrantType.PASSWORD;
    }

    @Override
    protected void doFlowSpecificTokenRequestContextProcessing(OAuthTokenRequestContext context) {

        Authentication authentication = authenticateResourceOwnerUser(context.getRequest());
        if (!authentication.isAuthenticated()) {
            throw new OAuthTokenRequestException(OAuthErrorCode.ACCESS_DENIED, String.format("Failed to authenticate user [%s]", context.getRequest().getUsername()));
        }

        updateScope(context.getRequest(), (UserDetails) authentication.getPrincipal());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected TokenClaims.TokenClaimsBuilder generateCustomClaims(OAuthTokenRequestContext context) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(userDetails) || !(userDetails instanceof ExtendedUser)) {
            throw new OAuthTokenRequestException(OAuthErrorCode.INVALID_CLIENT, "Missing user details in security context for password grant flow");
        }

        return super.generateCustomClaims(context)
                .subject(formatSubject(context, (ExtendedUser) userDetails))
                .email(userDetails.getUsername())
                .role(((ExtendedUser) userDetails).getRole())
                .username(((ExtendedUser) userDetails).getName())
                .userID(((ExtendedUser) userDetails).getId());
    }

    private Authentication authenticateResourceOwnerUser(OAuthTokenRequest oAuthTokenRequest) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(oAuthTokenRequest.getUsername(), oAuthTokenRequest.getPassword());

        return authenticationProvider.authenticate(authenticationToken);
    }

    private void updateScope(OAuthTokenRequest oAuthTokenRequest, UserDetails userDetails) {

        if (oAuthTokenRequest.getScope().isEmpty()) {
            oAuthTokenRequest.getScope().addAll(userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }
    }

    private String formatSubject(OAuthTokenRequestContext context, ExtendedUser userDetails) {
        return String.format("%s|uid=%s", context.getSourceClient().getClientId(), userDetails.getId());
    }
}
