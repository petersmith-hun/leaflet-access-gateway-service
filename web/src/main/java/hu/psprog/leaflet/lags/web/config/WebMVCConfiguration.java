package hu.psprog.leaflet.lags.web.config;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.config.OAuthTokenSettings;
import hu.psprog.leaflet.lags.core.service.registry.KeyRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        MVC_RESOURCES.forEach((folder, path) -> registry
                .addResourceHandler(folder)
                .addResourceLocations(path));
    }

    @Bean
    @Autowired
    public JWKSet jwkSet(KeyRegistry keyRegistry, OAuthConfigurationProperties oAuthConfigurationProperties) {

        OAuthTokenSettings tokenSettings = oAuthConfigurationProperties.getToken();
        RSAKey.Builder rsaKeyBuilder = new RSAKey.Builder((RSAPublicKey) keyRegistry.getPublicKey())
                .keyID(tokenSettings.getKeyID())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(Algorithm.parse(tokenSettings.getSignatureAlgorithm().name()));

        return new JWKSet(rsaKeyBuilder.build());
    }
}
