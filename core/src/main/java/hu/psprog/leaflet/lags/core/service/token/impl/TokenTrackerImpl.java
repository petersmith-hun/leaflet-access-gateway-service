package hu.psprog.leaflet.lags.core.service.token.impl;

import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.dao.AccessTokenDAO;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * Implementation of {@link TokenTracker}.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class TokenTrackerImpl implements TokenTracker {

    private final AccessTokenDAO accessTokenDAO;

    @Autowired
    public TokenTrackerImpl(AccessTokenDAO accessTokenDAO) {
        this.accessTokenDAO = accessTokenDAO;
    }

    @Override
    public void storeTokenInfo(StoreAccessTokenInfoRequest storeAccessTokenInfoRequest) {

        if (accessTokenDAO.retrieveByJTI(storeAccessTokenInfoRequest.getId()).isPresent()) {
            log.warn("Access token info is already stored for token identified by JTI={}", storeAccessTokenInfoRequest.getId());
        } else {
            accessTokenDAO.save(new AccessTokenInfo(storeAccessTokenInfoRequest));
        }
    }

    @Override
    public Optional<AccessTokenInfo> retrieveTokenInfo(String jti) {
        return accessTokenDAO.retrieveByJTI(jti);
    }

    @Override
    public void revokeToken(String jti) {

        Optional<AccessTokenInfo> accessTokenInfoOptional = accessTokenDAO.retrieveByJTI(jti);

        if (accessTokenInfoOptional.isEmpty()) {
            log.warn("Access token info entry does not exist by JTI={}", jti);
        } else {
            AccessTokenInfo accessTokenInfo = accessTokenInfoOptional.get();
            verifyActiveToken(accessTokenInfo);
            doRevokeToken(accessTokenInfo);
        }
    }

    @Override
    @Scheduled(fixedRateString = "PT1H")
    public void cleanUpExpiredToken() {

        log.info("Started cleaning up access token repository...");

        Date currentDate = new Date();

        accessTokenDAO.getAllAccessTokenInfo().stream()
                .filter(accessTokenInfo -> accessTokenInfo.getExpiresAt().before(currentDate))
                .map(AccessTokenInfo::getId)
                .forEach(accessTokenDAO::deleteByJTI);

        log.info("Access token repository clean-up finished.");
    }

    private void verifyActiveToken(AccessTokenInfo accessTokenInfo) {

        if (TokenStatus.ACTIVE != accessTokenInfo.getStatus()) {
            String message = String.format("Access token by JTI=%s to be modified must be ACTIVE, but is %s", accessTokenInfo.getId(), accessTokenInfo.getStatus());
            log.error(message);
            throw new OAuthAuthorizationException(message);
        }
    }

    private void doRevokeToken(AccessTokenInfo accessTokenInfo) {

        accessTokenInfo.setStatus(TokenStatus.REVOKED);
        accessTokenInfo.setRevokedAt(new Date());
        accessTokenDAO.save(accessTokenInfo);
    }
}
