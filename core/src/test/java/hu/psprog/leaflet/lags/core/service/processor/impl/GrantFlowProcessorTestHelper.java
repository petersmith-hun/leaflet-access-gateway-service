package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test utility class for {@link GrantFlowProcessor} implementation tests.
 *
 * @author Peter Smith
 */
class GrantFlowProcessorTestHelper {

    static final OAuthClient SOURCE_O_AUTH_CLIENT = prepareSourceOAuthClient();
    static final OAuthClient TARGET_O_AUTH_CLIENT = prepareTargetOAuthClient(true);
    static final OAuthClient INVALID_TARGET_O_AUTH_CLIENT = prepareTargetOAuthClient(false);

    private static OAuthClient prepareSourceOAuthClient() {

        return new OAuthClient(
                "source-service-1",
                ApplicationType.SERVICE,
                "dummy-source-service-1",
                "secret1",
                "source-service-audience",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
    }

    private static OAuthClient prepareTargetOAuthClient(boolean validRelation) {

        OAuthClientAllowRelation relation = new OAuthClientAllowRelation(
                validRelation ? "source-service-1" : "source-service-2",
                Arrays.asList("read:items", "write:item:self", "default1", "default2", "default3"));

        return new OAuthClient(
                "target-service-1",
                ApplicationType.SERVICE,
                "dummy-target-service-1",
                "secret2",
                "target-service-audience",
                Arrays.asList("read:items", "write:item:all", "write:item:self", "admin:item", "default1", "default2", "default3"),
                Collections.singletonList(relation),
                Collections.emptyList());
    }
}
