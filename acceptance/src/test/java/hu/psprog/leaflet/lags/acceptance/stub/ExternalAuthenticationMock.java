package hu.psprog.leaflet.lags.acceptance.stub;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.utility.HTTPUtility;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import hu.psprog.leaflet.lags.core.domain.internal.GitHubEmailItem;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.jsonResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * WireMock based mock server component for simulating external OAuth provider based login flows.
 *
 * @author Peter Smith
 */
@Component
public class ExternalAuthenticationMock {

    private static final int WIREMOCK_PORT = 9290;
    private static final int EXPIRES_IN = 3600;
    private static final int EXTERNAL_USER_ID = 558890;
    private static final String EXTERNAL_USER_NAME = "Test User";

    private WireMockServer wireMockServer;

    @PostConstruct
    public void setup() {

        wireMockServer = new WireMockServer(options().port(WIREMOCK_PORT));
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
            configureFor(WIREMOCK_PORT);
        }
    }

    @PreDestroy
    public void tearDown() {
        wireMockServer.stop();
    }

    /**
     * Initializes the mock endpoints for the given provider.
     *
     * @param forProvider provider identifier as {@link Provider} enum constant
     */
    public void registerProviderMock(Provider forProvider) {

        registerTokenEndpoint(forProvider.getTokenEndpoint());
        registerUserInfoEndpoint(forProvider.getUserinfoEndpoint());
        forProvider.getFurtherRegistrations()
                .forEach(Runnable::run);
    }

    /**
     * Resets all registered mock endpoints.
     */
    public void resetProviderMock() {
        WireMock.reset();
        wireMockServer.resetAll();
    }

    private static void registerTokenEndpoint(String tokenEndpoint) {

        URI location = ThreadLocalDataRegistry.getResponseEntity().getHeaders().getLocation();
        String scope = HTTPUtility.getQueryParameter(location, TestConstants.Attribute.SCOPE);

        givenThat(post(tokenEndpoint)
                .willReturn(jsonResponse(OAuthTokenResponse.builder()
                        .accessToken(UUID.randomUUID().toString())
                        .expiresIn(EXPIRES_IN)
                        .scope(scope)
                        .build(), 200)));
    }

    private static void registerUserInfoEndpoint(String userInfoEndpoint) {

        givenThat(get(userInfoEndpoint)
                .willReturn(jsonResponse(Map.of(
                        "name", EXTERNAL_USER_NAME,
                        "sub", String.valueOf(EXTERNAL_USER_ID),
                        "id", EXTERNAL_USER_ID,
                        "email", ThreadLocalDataRegistry.get(TestConstants.Attribute.EMAIL)
                ), 200)));
    }

    private static void registerGitHubEmailsEndpoint() {

        givenThat(get("/githubmock/user/emails")
                .willReturn(jsonResponse(List.of(
                        prepareGitHubEmailItem("ghuser1-non-primary@dev.local", false),
                        prepareGitHubEmailItem(ThreadLocalDataRegistry.get(TestConstants.Attribute.EMAIL), true)
                ), 200)));
    }

    private static GitHubEmailItem prepareGitHubEmailItem(String email, boolean primary) {

        GitHubEmailItem emailItem = new GitHubEmailItem();
        emailItem.setEmail(email);
        emailItem.setPrimary(primary);

        return emailItem;
    }

    public enum Provider {

        GITHUB("/githubmock/token", "/githubmock/userinfo", List.of(ExternalAuthenticationMock::registerGitHubEmailsEndpoint)),
        GOOGLE("/googlemock/token", "/googlemock/userinfo", Collections.emptyList());

        private final String tokenEndpoint;
        private final String userinfoEndpoint;
        private final List<Runnable> furtherRegistrations;

        Provider(String tokenEndpoint, String userinfoEndpoint, List<Runnable> furtherRegistrations) {
            this.tokenEndpoint = tokenEndpoint;
            this.userinfoEndpoint = userinfoEndpoint;
            this.furtherRegistrations = furtherRegistrations;
        }

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public String getUserinfoEndpoint() {
            return userinfoEndpoint;
        }

        public List<Runnable> getFurtherRegistrations() {
            return furtherRegistrations;
        }
    }
}
