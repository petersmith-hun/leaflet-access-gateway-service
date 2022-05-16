package hu.psprog.leaflet.lags.core.persistence.repository.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of {@link OngoingAuthorizationRepository}.
 *
 * @author Peter Smith
 */
@Component
public class InMemoryOngoingAuthorizationRepository implements OngoingAuthorizationRepository {

    private final Map<String, OngoingAuthorization> ongoingAuthorizationStorage = new HashMap<>();

    @Override
    public Optional<OngoingAuthorization> getOngoingAuthorizationByCode(String authorizationCode) {
        return Optional.ofNullable(ongoingAuthorizationStorage.get(authorizationCode));
    }

    @Override
    public void saveOngoingAuthorization(OngoingAuthorization ongoingAuthorization) {
        ongoingAuthorizationStorage.put(ongoingAuthorization.getAuthorizationCode(), ongoingAuthorization);
    }

    @Override
    public void deleteOngoingAuthorization(String authorizationCode) {
        ongoingAuthorizationStorage.remove(authorizationCode);
    }
}
