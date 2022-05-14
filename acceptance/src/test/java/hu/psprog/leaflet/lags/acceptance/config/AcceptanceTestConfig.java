package hu.psprog.leaflet.lags.acceptance.config;

import hu.psprog.leaflet.lags.LeafletAccessGatewayApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

/**
 * Acceptance test suite base configuration.
 *
 * @author Peter Smith
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = {UtilityConfiguration.class, LeafletAccessGatewayApplication.class})
@CucumberContextConfiguration
@ActiveProfiles("acceptance")
@Rollback(false)
public class AcceptanceTestConfig {

}
