package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.TokenStatus;
import hu.psprog.leaflet.lags.core.persistence.repository.AccessTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AccessTokenDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AccessTokenDAOImplTest {

    private static final String JTI = "jti-1";
    private static final AccessTokenInfo ACCESS_TOKEN_INFO = new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().id(JTI).build());

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @InjectMocks
    private AccessTokenDAOImpl accessTokenDAO;

    @Test
    public void shouldSavePassCallToRepository() {

        // when
        accessTokenDAO.save(ACCESS_TOKEN_INFO);

        // then
        verify(accessTokenRepository).save(ACCESS_TOKEN_INFO);
    }

    @Test
    public void shouldRetrieveByJTIPassCallToRepository() {

        // given
        given(accessTokenRepository.retrieveByJTI(JTI)).willReturn(Optional.of(ACCESS_TOKEN_INFO));

        // when
        Optional<AccessTokenInfo> result = accessTokenDAO.retrieveByJTI(JTI);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(ACCESS_TOKEN_INFO));
    }

    @Test
    public void shouldGetAllAccessTokenInfoPassCallToRepository() {

        // given
        given(accessTokenRepository.getAllAccessTokenInfo()).willReturn(Collections.singletonList(ACCESS_TOKEN_INFO));

        // when
        List<AccessTokenInfo> result = accessTokenDAO.getAllAccessTokenInfo();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(ACCESS_TOKEN_INFO));
    }

    @Test
    public void shouldDeleteByJTIPassCallToRepository() {

        // when
        accessTokenDAO.deleteByJTI(JTI);

        // then
        verify(accessTokenRepository).deleteByJTI(JTI);
    }

    @Test
    public void shouldUpdateStatusByJTIPassCallToRepository() {

        // when
        accessTokenDAO.updateStatusByJTI(JTI, TokenStatus.REVOKED);

        // then
        verify(accessTokenRepository).updateStatusByJTI(JTI, TokenStatus.REVOKED);
    }
}
