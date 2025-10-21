package hu.psprog.leaflet.lags.core.persistence.repository.impl;

import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.persistence.repository.AccessTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link AccessTokenRepository}.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class InMemoryAccessTokenRepository implements AccessTokenRepository {

    private final Map<String, AccessTokenInfo> accessTokenInfoStorage = new ConcurrentHashMap<>();

    @Override
    public void save(AccessTokenInfo accessTokenInfo) {
        accessTokenInfoStorage.put(accessTokenInfo.getId(), accessTokenInfo);
    }

    @Override
    public Optional<AccessTokenInfo> retrieveByJTI(String jti) {
        return Optional.ofNullable(accessTokenInfoStorage.get(jti));
    }

    @Override
    public List<AccessTokenInfo> getAllAccessTokenInfo() {
        return new ArrayList<>(accessTokenInfoStorage.values());
    }

    @Override
    public void deleteByJTI(String jti) {
        accessTokenInfoStorage.remove(jti);
    }

    @Override
    public void updateStatusByJTI(String jti, TokenStatus newStatus) {

        AccessTokenInfo accessTokenInfo = accessTokenInfoStorage.get(jti);

        if (Objects.nonNull(accessTokenInfo)) {
            accessTokenInfo.setStatus(newStatus);
            accessTokenInfo.setRevokedAt(new Date());

            save(accessTokenInfo);
        }
    }
}
