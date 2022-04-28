package hu.psprog.leaflet.lags.acceptance.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Test utility beans for Cucumber based acceptance tests.
 *
 * @author Peter Smith
 */
@TestConfiguration
public class UtilityConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
