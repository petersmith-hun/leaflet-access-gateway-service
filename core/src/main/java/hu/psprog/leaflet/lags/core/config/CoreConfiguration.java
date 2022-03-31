package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.mail.config.MailComponentConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Service layer configuration.
 *
 * @author Peter Smith
 */
@Configuration
@Import(MailComponentConfig.class)
public class CoreConfiguration {
}
