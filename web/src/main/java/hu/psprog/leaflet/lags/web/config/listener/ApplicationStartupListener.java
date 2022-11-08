package hu.psprog.leaflet.lags.web.config.listener;

import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link ApplicationListener} implementation that requests submitting a notification mail about the application's
 * successful startup.
 *
 * @author Peter Smith
 */
@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    private final Optional<BuildProperties> buildPropertiesOptional;
    private final NotificationAdapter notificationAdapter;

    public ApplicationStartupListener(@Autowired(required = false) BuildProperties buildProperties,
                                      NotificationAdapter notificationAdapter) {
        this.buildPropertiesOptional = Optional.ofNullable(buildProperties);
        this.notificationAdapter = notificationAdapter;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        buildPropertiesOptional
                .ifPresent(buildProperties -> notificationAdapter.startupFinished(buildProperties.getVersion()));
    }
}
