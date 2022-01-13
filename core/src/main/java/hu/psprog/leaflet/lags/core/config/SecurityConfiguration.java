package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private static final String PATH_LOGIN = "/login";
    private static final String PATH_LOGIN_FAILURE = "/login?auth=fail";
    private static final String USERNAME_PARAMETER = "email";
    private static final String RESOURCE_IMAGES = "/images/**";
    private static final String RESOURCE_CSS = "/css/**";
    private static final String RESOURCE_JS = "/js/**";

    private final UserDetailsService localUserUserDetailsService;
    private final UserDetailsService oAuthClientUserDetailsService;

    @Autowired
    public SecurityConfiguration(@Qualifier("localUserUserDetailsService") UserDetailsService localUserUserDetailsService,
                                 @Qualifier("OAuthClientUserDetailsService") UserDetailsService oAuthClientUserDetailsService) {
        this.localUserUserDetailsService = localUserUserDetailsService;
        this.oAuthClientUserDetailsService = oAuthClientUserDetailsService;
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

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {

        auth
                .authenticationProvider(oAuthClientAuthenticationProvider(passwordEncoder()))
                .authenticationProvider(localUserAuthenticationProvider(passwordEncoder()));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http

                .authorizeRequests()
                    .antMatchers(PATH_OAUTH_ROOT)
                        .fullyAuthenticated()
                    .antMatchers(PATH_LOGIN, RESOURCE_IMAGES, RESOURCE_CSS, RESOURCE_JS)
                        .permitAll()
                    .and()

                .httpBasic()
                    .and()

                .formLogin()
                    .loginPage(PATH_LOGIN)
                    .failureUrl(PATH_LOGIN_FAILURE)
                    .usernameParameter(USERNAME_PARAMETER)
                    .and()

                .csrf()
                    .ignoringAntMatchers(PATH_OAUTH_ROOT)
                    .and()

                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    private AuthenticationProvider createAuthenticationProvider(PasswordEncoder passwordEncoder,
                                                                UserDetailsService userDetailsService) {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);

        return authenticationProvider;
    }
}
