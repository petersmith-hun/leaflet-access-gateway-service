package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.persistence.dao.AccessTokenDAO;
import hu.psprog.leaflet.lags.core.persistence.repository.AccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link AccessTokenDAO}.
 * Passes through the requests to the {@link AccessTokenRepository}.
 *
 * @author Peter Smith
 */
@Component
public class AccessTokenDAOImpl implements AccessTokenDAO {

    private final AccessTokenRepository accessTokenRepository;

    @Autowired
    public AccessTokenDAOImpl(@Qualifier("inMemoryAccessTokenRepository") AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public void save(AccessTokenInfo accessTokenInfo) {
        accessTokenRepository.save(accessTokenInfo);
    }

    @Override
    public Optional<AccessTokenInfo> retrieveByJTI(String jti) {
        return accessTokenRepository.retrieveByJTI(jti);
    }

    @Override
    public List<AccessTokenInfo> getAllAccessTokenInfo() {
        return accessTokenRepository.getAllAccessTokenInfo();
    }

    @Override
    public void deleteByJTI(String jti) {
        accessTokenRepository.deleteByJTI(jti);
    }

    @Override
    public void updateStatusByJTI(String jti, TokenStatus newStatus) {
        accessTokenRepository.updateStatusByJTI(jti, newStatus);
    }
}
