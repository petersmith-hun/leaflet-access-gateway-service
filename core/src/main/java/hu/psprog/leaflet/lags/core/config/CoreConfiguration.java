package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.bridge.client.request.RequestAdapter;
import hu.psprog.leaflet.bridge.integration.request.adapter.StaticRequestAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Service layer configuration.
 *
 * @author Peter Smith
 */
@Configuration
@EnableScheduling
@EnableJpaAuditing
public class CoreConfiguration {

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService() {
        return new DefaultOAuth2UserService();
    }

    @Bean
    public RequestAdapter requestAdapter() {
        return new StaticRequestAdapter("lags");
    }
}
