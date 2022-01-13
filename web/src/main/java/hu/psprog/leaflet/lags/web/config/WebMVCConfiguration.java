package hu.psprog.leaflet.lags.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

/**
 * Spring Web MVC configuration.
 *
 * @author Peter Smith
 */
@Configuration
public class WebMVCConfiguration implements WebMvcConfigurer {

    private final static Map<String, String> MVC_RESOURCES = Map.of(
            "/css/**", "classpath:/webapp/resources/css/",
            "/images/**", "classpath:/webapp/resources/images/",
            "/js/**", "classpath:/webapp/resources/js/"
    );

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        MVC_RESOURCES.forEach((folder, path) -> registry
                .addResourceHandler(folder)
                .addResourceLocations(path));
    }
}
