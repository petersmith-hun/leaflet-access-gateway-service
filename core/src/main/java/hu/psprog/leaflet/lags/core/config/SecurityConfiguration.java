package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.security.OAuthAccessTokenAuthenticationFilter;
import hu.psprog.leaflet.lags.core.security.RequestSavingLogoutSuccessHandler;
import hu.psprog.leaflet.lags.core.security.ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.AUTHORIZATION_HEADER;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_ACCESS_DENIED;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_OAUTH_USERINFO;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_UNKNOWN_ERROR;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.QUERY_PARAMETER_TOKEN;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.RECLAIM_AUTHORITY;

/**
 * OAuth2 security configuration.
 *
 * @author Peter Smith
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuthConfigurationProperties.class)
public class SecurityConfiguration {

    private static final String PATH_OAUTH_ROOT = "/oauth/**";
    private static final String PATH_LOGIN_FAILURE = "/login?auth=fail";
    private static final String PATH_LOGIN_EXTERNAL = "/login/external";
    private static final String PATH_LOGOUT = "/logout";
    private static final String PATH_WELL_KNOWN_ROOT = "/.well-known/**";
    private static final String USERNAME_PARAMETER = "email";
    private static final String RESOURCE_IMAGES = "/images/**";
    private static final String RESOURCE_CSS = "/css/**";
    private static final String RESOURCE_JS = "/js/**";

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
    public AuthenticationProvider localUserAuthenticationProvider(@Qualifier("localUserUserDetailsService") UserDetailsService localUserUserDetailsService,
                                                                  PasswordEncoder passwordEncoder) {
        return createAuthenticationProvider(passwordEncoder, localUserUserDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider oAuthClientAuthenticationProvider,
                                                       AuthenticationProvider localUserAuthenticationProvider,
                                                       AuthenticationProvider accessTokenAuthenticationProvider) {
        return new ProviderManager(oAuthClientAuthenticationProvider, localUserAuthenticationProvider, accessTokenAuthenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
                                                   TokenHandler tokenHandler, OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService,
                                                   AuthenticationFailureHandler externalSignUpAuthenticationFailureHandler,
                                                   ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler returnToAuthorizationAfterLogoutAuthenticationSuccessHandler) throws Exception {

        return http
                .addFilterBefore(passwordResetAuthenticationFilter(authenticationManager, tokenHandler), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(userInfoAuthenticationFilter(authenticationManager, tokenHandler), UsernamePasswordAuthenticationFilter.class)

                .authorizeRequests()
                    .antMatchers(PATH_OAUTH_ROOT)
                        .fullyAuthenticated()
                    .antMatchers(PATH_PASSWORD_RESET_CONFIRMATION)
                        .hasAuthority(RECLAIM_AUTHORITY.getAuthority())
                    .antMatchers(PATH_LOGIN, PATH_PASSWORD_RESET, RESOURCE_IMAGES, RESOURCE_CSS, RESOURCE_JS, PATH_UNKNOWN_ERROR, PATH_ACCESS_DENIED, PATH_WELL_KNOWN_ROOT)
                        .permitAll()
                    .and()

                .httpBasic()
                    .and()

                .formLogin()
                    .loginPage(PATH_LOGIN)
                    .failureUrl(PATH_LOGIN_FAILURE)
                    .usernameParameter(USERNAME_PARAMETER)
                    .successHandler(returnToAuthorizationAfterLogoutAuthenticationSuccessHandler)
                    .and()

                .oauth2Login()
                    .loginPage(PATH_LOGIN_EXTERNAL)
                    .successHandler(returnToAuthorizationAfterLogoutAuthenticationSuccessHandler)
                    .failureHandler(externalSignUpAuthenticationFailureHandler)
                    .userInfoEndpoint()
                        .userService(oAuth2UserService)
                        .and()
                    .and()

                .logout()
                    .logoutUrl(PATH_LOGOUT)
                    .logoutSuccessUrl(PATH_LOGIN)
                    .logoutSuccessHandler(getLogoutSuccessHandler())
                    .and()

                .csrf()
                    .ignoringAntMatchers(PATH_OAUTH_ROOT)
                    .and()

                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER)
                    .and()

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

    private OAuthAccessTokenAuthenticationFilter passwordResetAuthenticationFilter(AuthenticationManager authenticationManager, TokenHandler tokenHandler) {

        OAuthAccessTokenAuthenticationFilter filter = new OAuthAccessTokenAuthenticationFilter(tokenHandler, PATH_PASSWORD_RESET_CONFIRMATION,
                request -> request.getParameter(QUERY_PARAMETER_TOKEN));
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationFailureHandler(new ForwardAuthenticationFailureHandler(PATH_ACCESS_DENIED));

        return filter;
    }

    private OAuthAccessTokenAuthenticationFilter userInfoAuthenticationFilter(AuthenticationManager authenticationManager, TokenHandler tokenHandler) {

        OAuthAccessTokenAuthenticationFilter filter = new OAuthAccessTokenAuthenticationFilter(tokenHandler, PATH_OAUTH_USERINFO,
                request -> request.getHeader(AUTHORIZATION_HEADER));
        filter.setAuthenticationManager(authenticationManager);

        return filter;
    }
}
