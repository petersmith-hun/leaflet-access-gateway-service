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
import org.springframework.security.web.context.NullSecurityContextRepository;

/**
 * OAuth2 security configuration.
 *
 * @author Peter Smith
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuthConfigurationProperties.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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
                .securityContext()
                    .securityContextRepository(new NullSecurityContextRepository())
                .and()

                .authorizeRequests()
                    .antMatchers("/oauth/token").fullyAuthenticated()
                .and()

                .httpBasic()
                .and()

                .csrf()
                    .disable()

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
