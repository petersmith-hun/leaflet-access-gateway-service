package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.dao.AccessTokenDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link TokenTrackerImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class TokenTrackerImplTest {

    private static final String JTI = UUID.randomUUID().toString();
    private static final StoreAccessTokenInfoRequest STORE_ACCESS_TOKEN_INFO_REQUEST = StoreAccessTokenInfoRequest.builder()
            .id(JTI)
            .build();
    private static final AccessTokenInfo ACCESS_TOKEN_INFO = new AccessTokenInfo(STORE_ACCESS_TOKEN_INFO_REQUEST);

    @Mock
    private AccessTokenDAO accessTokenDAO;

    @InjectMocks
    private TokenTrackerImpl tokenTracker;

    @Test
    public void shouldStoreTokenInfoCallDAO() {

        // given
        given(accessTokenDAO.retrieveByJTI(JTI)).willReturn(Optional.empty());

        // when
        tokenTracker.storeTokenInfo(STORE_ACCESS_TOKEN_INFO_REQUEST);

        // then
        verify(accessTokenDAO).save(ACCESS_TOKEN_INFO);
    }

    @Test
    public void shouldStoreTokenInfoDoNothingIfEntryAlreadyExists() {

        // given
        given(accessTokenDAO.retrieveByJTI(JTI)).willReturn(Optional.of(ACCESS_TOKEN_INFO));

        // when
        tokenTracker.storeTokenInfo(STORE_ACCESS_TOKEN_INFO_REQUEST);

        // then
        verify(accessTokenDAO).retrieveByJTI(JTI);
        verifyNoMoreInteractions(accessTokenDAO);
    }

    @Test
    public void shouldRetrieveTokenInfoCallDAOForData() {

        // given
        given(accessTokenDAO.retrieveByJTI(JTI)).willReturn(Optional.of(ACCESS_TOKEN_INFO));

        // when
        Optional<AccessTokenInfo> result = tokenTracker.retrieveTokenInfo(JTI);

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(ACCESS_TOKEN_INFO));
    }

    @Test
    public void shouldRevokeTokenDoRevokeActiveToken() {

        // given
        Optional<AccessTokenInfo> accessTokenInfo = prepareAccessTokenInfo(TokenStatus.ACTIVE);
        given(accessTokenDAO.retrieveByJTI(JTI)).willReturn(accessTokenInfo);
        
        // when
        tokenTracker.revokeToken(JTI);
        
        // then
        AccessTokenInfo revokedAccessTokenInfo = accessTokenInfo.get();
        assertThat(revokedAccessTokenInfo.getStatus(), equalTo(TokenStatus.REVOKED));
        assertThat(System.currentTimeMillis() - revokedAccessTokenInfo.getRevokedAt().getTime() < 100, is(true));
        verify(accessTokenDAO).save(accessTokenInfo.get());
    }

    @Test
    public void shouldRevokeTokenDoNothingIfTokenIsNotTracked() {

        // given
        given(accessTokenDAO.retrieveByJTI(JTI)).willReturn(Optional.empty());

        // when
        tokenTracker.revokeToken(JTI);

        // then
        verify(accessTokenDAO).retrieveByJTI(JTI);
        verifyNoMoreInteractions(accessTokenDAO);
    }

    @Test
    public void shouldRevokeTokenThrowExceptionOnRevokingAlreadyRevokedToken() {

        // given
        Optional<AccessTokenInfo> accessTokenInfo = prepareAccessTokenInfo(TokenStatus.REVOKED);
        given(accessTokenDAO.retrieveByJTI(JTI)).willReturn(accessTokenInfo);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> tokenTracker.revokeToken(JTI));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Access token by JTI=%s to be modified must be ACTIVE, but is %s", JTI, TokenStatus.REVOKED)));
    }

    @Test
    public void shouldCleanUpExpiredTokenExecuteCleanUp() {

        // given
        given(accessTokenDAO.getAllAccessTokenInfo()).willReturn(Arrays.asList(
                prepareAccessTokenInfo("to-keep-1", TokenStatus.ACTIVE, prepareExpirationDate(1)),
                prepareAccessTokenInfo("to-delete-1", TokenStatus.ACTIVE, prepareExpirationDate(-1)),
                prepareAccessTokenInfo("to-keep-2", TokenStatus.REVOKED, prepareExpirationDate(1)),
                prepareAccessTokenInfo("to-delete-2", TokenStatus.REVOKED, prepareExpirationDate(-1))
        ));

        // when
        tokenTracker.cleanUpExpiredToken();

        // then
        verify(accessTokenDAO).getAllAccessTokenInfo();
        verify(accessTokenDAO).deleteByJTI("to-delete-1");
        verify(accessTokenDAO).deleteByJTI("to-delete-2");
        verifyNoMoreInteractions(accessTokenDAO);
    }

    private Optional<AccessTokenInfo> prepareAccessTokenInfo(TokenStatus status) {
        return Optional.of(prepareAccessTokenInfo(JTI, status, new Date()));
    }

    private AccessTokenInfo prepareAccessTokenInfo(String jti, TokenStatus status, Date expiresAt) {

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo(StoreAccessTokenInfoRequest.builder()
                .id(jti)
                .expiresAt(expiresAt)
                .build());
        accessTokenInfo.setStatus(status);

        return accessTokenInfo;
    }

    private Date prepareExpirationDate(long hoursOffset) {

        return Date.from(ZonedDateTime.now()
                .plusHours(hoursOffset)
                .toInstant());
    }
}
