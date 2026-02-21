package hu.psprog.leaflet.lags.core.config;

import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.config.OAuthTokenSettings;
import hu.psprog.leaflet.lags.core.security.ExternalSignUpAuthenticationFailureHandler;
import hu.psprog.leaflet.lags.core.security.OAuthAuthenticationEntryPoint;
import hu.psprog.leaflet.lags.core.security.PasswordResetTokenCopyFilter;
import hu.psprog.leaflet.lags.core.security.RequestSavingLogoutSuccessHandler;
import hu.psprog.leaflet.lags.core.security.ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler;
import hu.psprog.leaflet.lags.core.service.UserManagementService;
import hu.psprog.leaflet.lags.core.service.registry.KeyRegistry;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import hu.psprog.leaflet.lags.core.service.token.validator.PasswordResetTokenValidator;
import hu.psprog.leaflet.lags.core.service.token.validator.TrackedStatusTokenValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_ACCESS_DENIED;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_SIGNUP;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_UNKNOWN_ERROR;

/**
 * OAuth2 security configuration.
 *
 * @author Peter Smith
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String PATH_OAUTH_ROOT = "/oauth/**";
    private static final String PATH_ACCESS_MANAGEMENT_ROOT = "/access-management/**";
    private static final String PATH_LOGIN_FAILURE = "/login?auth=fail";
    private static final String PATH_LOGIN_EXTERNAL = "/login/external";
    private static final String PATH_LOGOUT = "/logout";
    private static final String PATH_WELL_KNOWN_ROOT = "/.well-known/**";
    private static final String PATH_ACTUATOR_HEALTH = "/actuator/health";
    private static final String PATH_ACTUATOR_INFO = "/actuator/info";
    private static final String USERNAME_PARAMETER = "email";
    private static final String RESOURCE_IMAGES = "/images/**";
    private static final String RESOURCE_CSS = "/css/**";
    private static final String RESOURCE_JS = "/js/**";

    private static final String[] PUBLIC_PATHS = {
            PATH_LOGIN,
            PATH_SIGNUP,
            PATH_PASSWORD_RESET,
            PATH_UNKNOWN_ERROR,
            PATH_ACCESS_DENIED,
            PATH_WELL_KNOWN_ROOT,
            PATH_ACTUATOR_HEALTH,
            PATH_ACTUATOR_INFO,
            RESOURCE_IMAGES,
            RESOURCE_CSS,
            RESOURCE_JS
    };

    private static final String[] PRIVATE_PATHS = {
            PATH_OAUTH_ROOT,
            PATH_ACCESS_MANAGEMENT_ROOT,
            PATH_PASSWORD_RESET_CONFIRMATION
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider oAuthClientAuthenticationProvider(@Qualifier("OAuthClientUserDetailsService") UserDetailsService oAuthClientUserDetailsService,
                                                                    PasswordEncoder passwordEncoder) {
        return createAuthenticationProvider(passwordEncoder, oAuthClientUserDetailsService);
    }

    @Bean
    public AuthenticationProvider localUserAuthenticationProvider(@Qualifier("nonExternalLocalUserUserDetailsService") UserDetailsService localUserUserDetailsService,
                                                                  PasswordEncoder passwordEncoder) {
        return createAuthenticationProvider(passwordEncoder, localUserUserDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider oAuthClientAuthenticationProvider,
                                                       AuthenticationProvider localUserAuthenticationProvider) {
        return new ProviderManager(oAuthClientAuthenticationProvider, localUserAuthenticationProvider);
    }

    @Bean
    public JWKSet jwkSet(KeyRegistry keyRegistry, OAuthConfigurationProperties oAuthConfigurationProperties) {

        OAuthTokenSettings tokenSettings = oAuthConfigurationProperties.getToken();
        RSAKey.Builder rsaKeyBuilder = new RSAKey.Builder((RSAPublicKey) keyRegistry.getPublicKey())
                .keyID(tokenSettings.getKeyID())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(tokenSettings.getSignatureAlgorithm());

        return new JWKSet(rsaKeyBuilder.build());
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyRegistry keyRegistry, TokenTracker tokenTracker,
                                 AuthenticationConfig authenticationConfig) {

        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
                .withPublicKey((RSAPublicKey) keyRegistry.getPublicKey())
                .build();

        nimbusJwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.of(
                new JwtTimestampValidator(Duration.ZERO),
                new TrackedStatusTokenValidator(tokenTracker),
                new PasswordResetTokenValidator(authenticationConfig)
        )));

        return nimbusJwtDecoder;
    }

    @Bean
    public JWSSigner jwsSigner(KeyRegistry keyRegistry) {
        return new RSASSASigner(keyRegistry.getPrivateKey());
    }

    @Bean
    @Primary
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler(UserManagementService userManagementService) {
        return new ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler(userManagementService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
                                                   SavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {

        return http
                .addFilterBefore(new PasswordResetTokenCopyFilter(), UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(registry -> registry
                        .requestMatchers(PRIVATE_PATHS)
                            .fullyAuthenticated()
                        .requestMatchers(PUBLIC_PATHS)
                            .permitAll())

                .httpBasic(httpBasic -> {})

                .formLogin(formLogin -> formLogin
                        .loginPage(PATH_LOGIN)
                        .failureUrl(PATH_LOGIN_FAILURE)
                        .usernameParameter(USERNAME_PARAMETER)
                        .successHandler(authenticationSuccessHandler))

                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage(PATH_LOGIN_EXTERNAL)
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(new ExternalSignUpAuthenticationFailureHandler())
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService)))

                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(new OAuthAuthenticationEntryPoint())
                        .jwt(Customizer.withDefaults()))

                .logout(logout -> logout
                        .logoutUrl(PATH_LOGOUT)
                        .logoutSuccessUrl(PATH_LOGIN)
                        .logoutSuccessHandler(getLogoutSuccessHandler()))

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(PATH_OAUTH_ROOT))

                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.NEVER))

                .build();
    }

    private RequestSavingLogoutSuccessHandler getLogoutSuccessHandler() {

        RequestSavingLogoutSuccessHandler requestSavingLogoutSuccessHandler = new RequestSavingLogoutSuccessHandler();
        requestSavingLogoutSuccessHandler.setDefaultTargetUrl(PATH_LOGIN);

        return requestSavingLogoutSuccessHandler;
    }

    private AuthenticationProvider createAuthenticationProvider(PasswordEncoder passwordEncoder,
                                                                UserDetailsService userDetailsService) {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);

        return authenticationProvider;
    }
}
