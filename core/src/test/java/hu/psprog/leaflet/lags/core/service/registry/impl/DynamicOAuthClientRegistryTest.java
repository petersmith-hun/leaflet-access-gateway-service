package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.conversion.OAuthClientConverter;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link DynamicOAuthClientRegistry}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class DynamicOAuthClientRegistryTest {

    @Mock
    private OAuthApplicationDAO oAuthApplicationDAO;

    @Mock
    private OAuthClientConverter oAuthClientConverter;

    @InjectMocks
    private DynamicOAuthClientRegistry dynamicOAuthClientRegistry;

    @Test
    public void shouldGetClientByClientID() {

        // given
        var clientID = "client-1";
        var application = OAuthApplication.builder()
                .clientId(clientID)
                .build();
        var expectedResult = OAuthClient.builder()
                .clientId(clientID)
                .build();

        given(oAuthApplicationDAO.findByClientID(clientID)).willReturn(Optional.of(application));
        given(oAuthClientConverter.convert(application)).willReturn(expectedResult);

        // when
        var result = dynamicOAuthClientRegistry.getClientByClientID(clientID);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(expectedResult));
    }

    @Test
    public void shouldGetClientByAudience() {

        // given
        var audience = "audience-1";
        var application = OAuthApplication.builder()
                .audience(audience)
                .build();
        var expectedResult = OAuthClient.builder()
                .audience(audience)
                .build();

        given(oAuthApplicationDAO.findByAudience(audience)).willReturn(Optional.of(application));
        given(oAuthClientConverter.convert(application)).willReturn(expectedResult);

        // when
        var result = dynamicOAuthClientRegistry.getClientByAudience(audience);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(expectedResult));
    }
}
