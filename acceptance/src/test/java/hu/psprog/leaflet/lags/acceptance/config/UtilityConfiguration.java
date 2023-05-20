package hu.psprog.leaflet.lags.acceptance.config;

import hu.psprog.leaflet.bridge.client.BridgeClient;
import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.recaptcha.api.client.ReCaptchaClient;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaRequest;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaResponse;
import jakarta.annotation.PostConstruct;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.withSettings;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Test utility beans for Cucumber based acceptance tests.
 *
 * @author Peter Smith
 */
@Configuration
public class UtilityConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @PostConstruct
    public void replacePasswordEncoder() {

        //noinspection deprecation
        ((GenericWebApplicationContext) applicationContext)
                .registerBean("passwordEncoder", PasswordEncoder.class, NoOpPasswordEncoder::getInstance);
    }

    @Bean
    public String baseServerPath(@Value("${server.port}") int serverPort, @Value("${server.servlet.context-path}") String contextPath) {
        return String.format("http://localhost:%d%s", serverPort, contextPath);
    }

    @Bean
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {

        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Bean
    @Primary
    public ReCaptchaClient reCaptchaClient() throws CommunicationFailureException {

        ReCaptchaClient reCaptchaClient = Mockito.mock(ReCaptchaClient.class, withSettings().strictness(Strictness.LENIENT));
        given(reCaptchaClient.validate(createReCaptchaRequest(TestConstants.Attribute.RECAPTCHA_TOKEN.getValue()))).willReturn(getReCaptchaResponse(true));
        given(reCaptchaClient.validate(createReCaptchaRequest("invalidToken"))).willReturn(getReCaptchaResponse(false));

        return reCaptchaClient;
    }

    @Bean
    @Primary
    public BridgeClient grc() {
        return Mockito.mock(BridgeClient.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static ReCaptchaRequest createReCaptchaRequest(String value) {

        return ReCaptchaRequest.getBuilder()
                .withRemoteIp("127.0.0.1")
                .withSecret("placeholder")
                .withResponse(value)
                .build();
    }

    private ReCaptchaResponse getReCaptchaResponse(boolean successful) {

        return ReCaptchaResponse
                .getBuilder()
                .withSuccessful(successful)
                .build();
    }
}
