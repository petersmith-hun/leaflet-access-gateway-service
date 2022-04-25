package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.security.PasswordResetAuthenticationFilter;
import hu.psprog.leaflet.lags.core.security.RequestSavingLogoutSuccessHandler;
import hu.psprog.leaflet.lags.core.security.ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_PASSWORD_RESET;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.RECLAIM_AUTHORITY;

/**
 * OAuth2 security configuration.
 *
 * @author Peter Smith
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuthConfigurationProperties.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String PATH_OAUTH_ROOT = "/oauth/**";
    private static final String PATH_LOGIN_FAILURE = "/login?auth=fail";
    private static final String PATH_LOGOUT = "/logout";
    private static final String USERNAME_PARAMETER = "email";
    private static final String RESOURCE_IMAGES = "/images/**";
    private static final String RESOURCE_CSS = "/css/**";
    private static final String RESOURCE_JS = "/js/**";

    private final UserDetailsService localUserUserDetailsService;
    private final UserDetailsService oAuthClientUserDetailsService;
    private final AuthenticationProvider accessTokenAuthenticationProvider;
    private final TokenHandler tokenHandler;

    @Autowired
    public SecurityConfiguration(@Qualifier("localUserUserDetailsService") UserDetailsService localUserUserDetailsService,
                                 @Qualifier("OAuthClientUserDetailsService") UserDetailsService oAuthClientUserDetailsService,
                                 AuthenticationProvider accessTokenAuthenticationProvider, TokenHandler tokenHandler) {
        this.localUserUserDetailsService = localUserUserDetailsService;
        this.oAuthClientUserDetailsService = oAuthClientUserDetailsService;
        this.accessTokenAuthenticationProvider = accessTokenAuthenticationProvider;
        this.tokenHandler = tokenHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider oAuthClientAuthenticationProvider(PasswordEncoder passwordEncoder) {
        return createAuthenticationProvider(passwordEncoder, oAuthClientUserDetailsService);
    }

    @Bean
    public AuthenticationProvider localUserAuthenticationProvider(PasswordEncoder passwordEncoder) {
        return createAuthenticationProvider(passwordEncoder, localUserUserDetailsService);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {

        auth
                .authenticationProvider(oAuthClientAuthenticationProvider(passwordEncoder()))
                .authenticationProvider(localUserAuthenticationProvider(passwordEncoder()))
                .authenticationProvider(accessTokenAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .addFilterBefore(passwordResetAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                .authorizeRequests()
                    .antMatchers(PATH_OAUTH_ROOT)
                        .fullyAuthenticated()
                    .antMatchers(PATH_PASSWORD_RESET_CONFIRMATION)
                        .hasAuthority(RECLAIM_AUTHORITY.getAuthority())
                    .antMatchers(PATH_LOGIN, PATH_PASSWORD_RESET, RESOURCE_IMAGES, RESOURCE_CSS, RESOURCE_JS)
                        .permitAll()
                    .and()

                .httpBasic()
                    .and()

                .formLogin()
                    .loginPage(PATH_LOGIN)
                    .failureUrl(PATH_LOGIN_FAILURE)
                    .usernameParameter(USERNAME_PARAMETER)
                    .successHandler(new ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler())
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
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER);
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

    private PasswordResetAuthenticationFilter passwordResetAuthenticationFilter() throws Exception {

        PasswordResetAuthenticationFilter filter = new PasswordResetAuthenticationFilter(tokenHandler);
        filter.setAuthenticationManager(authenticationManagerBean());

        return filter;
    }
}
