package hu.psprog.leaflet.lags.acceptance.model;

import lombok.Data;

import java.util.Map;
import java.util.function.Function;

/**
 * Application info endpoint response.
 *
 * @author Peter Smith
 */
@Data
public class ApplicationInfoResponse {

    public static final Map<String, Function<ApplicationInfoResponse, String>> FIELD_MAPPING = Map.of(
            "name", applicationInfoResponse -> applicationInfoResponse.getApp().getName(),
            "abbreviation", applicationInfoResponse -> applicationInfoResponse.getApp().getAbbreviation()
    );

    private Application app;

    @Data
    public static class Application {

        private String name;
        private String abbreviation;
    }
}
