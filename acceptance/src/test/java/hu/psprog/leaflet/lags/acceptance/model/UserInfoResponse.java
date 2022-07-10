package hu.psprog.leaflet.lags.acceptance.model;

import lombok.Data;

import java.util.Map;
import java.util.function.Function;

/**
 * Domain class for holding pieces of user information for the OAuth2 user info endpoint.
 *
 * @author Peter Smith
 */
@Data
public class UserInfoResponse {

    public static final Map<String, Function<UserInfoResponse, String>> FIELD_EXTRACTION_MAPPING = Map.of(
            "sub", UserInfoResponse::getSub,
            "name", UserInfoResponse::getName,
            "email", UserInfoResponse::getEmail
    );

    private String sub;
    private String name;
    private String email;
}
