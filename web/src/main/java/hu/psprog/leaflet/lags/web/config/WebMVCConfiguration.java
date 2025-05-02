package hu.psprog.leaflet.lags.web.config;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.web.model.AuthServerMetaInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_OAUTH_USERINFO;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_AUTHORIZE;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_INTROSPECT;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_TOKEN;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_WELL_KNOWN_JWKS;

/**
 * Spring Web MVC configuration.
 *
 * @author Peter Smith
 */
@Configuration
public class WebMVCConfiguration implements WebMvcConfigurer {

    private final static Map<String, String> MVC_RESOURCES = Map.of(
            "/css/**", "classpath:/webapp/resources/css/",
            "/images/**", "classpath:/webapp/resources/images/",
            "/js/**", "classpath:/webapp/resources/js/"
    );
    private static final List<String> TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED =
            Arrays.asList("client_secret_post", "client_secret_basic");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        MVC_RESOURCES.forEach((folder, path) -> registry
                .addResourceHandler(folder)
                .addResourceLocations(path));
    }

    @Bean
    public AuthServerMetaInfo authServerMetaInfo(OAuthConfigurationProperties oAuthConfigurationProperties) {

        String issuer = oAuthConfigurationProperties.getToken().getIssuer();

        return AuthServerMetaInfo.builder()
                .issuer(issuer)
                .authorizationEndpoint(issuer + PATH_OAUTH_AUTHORIZE)
                .tokenEndpoint(issuer + PATH_OAUTH_TOKEN)
                .jwksURI(issuer + PATH_WELL_KNOWN_JWKS)
                .tokenIntrospectionEndpoint(issuer + PATH_OAUTH_INTROSPECT)
                .userinfoEndpoint(issuer + PATH_OAUTH_USERINFO)
                .grantTypesSupported(getSupportedItems(GrantType.class, GrantType::getGrantTypeName))
                .responseTypesSupported(getSupportedItems(AuthorizationResponseType.class, AuthorizationResponseType::getResponseTypeName))
                .tokenEndpointAuthMethodsSupported(TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED)
                .build();
    }

    private <T extends Enum<T>> List<String> getSupportedItems(Class<T> sourceEnum, Function<T, String> itemNameFunction) {

        return Stream.of(sourceEnum.getEnumConstants())
                .map(itemNameFunction)
                .collect(Collectors.toList());
    }
}
