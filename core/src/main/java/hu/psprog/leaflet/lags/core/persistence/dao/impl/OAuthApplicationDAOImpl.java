package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.persistence.repository.OAuthApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of {@link OAuthApplicationDAO}.
 *
 * @author Peter Smith
 */
@Component
public class OAuthApplicationDAOImpl implements OAuthApplicationDAO {

    private final OAuthApplicationRepository oAuthApplicationRepository;

    @Autowired
    public OAuthApplicationDAOImpl(OAuthApplicationRepository oAuthApplicationRepository) {
        this.oAuthApplicationRepository = oAuthApplicationRepository;
    }

    @Override
    public OAuthApplication save(OAuthApplication entity) {
        return oAuthApplicationRepository.saveAndFlush(entity);
    }

    @Override
    public Page<OAuthApplication> findAll(Pageable pageable) {
        return oAuthApplicationRepository.findAll(pageable);
    }

    @Override
    public Optional<OAuthApplication> findByID(UUID applicationID) {
        return oAuthApplicationRepository.findById(applicationID);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OAuthApplication> findByName(String name) {
        return oAuthApplicationRepository.findByName(name);
    }

    @Override
    public Optional<OAuthApplication> findByClientID(String clientID) {
        return oAuthApplicationRepository.findByClientId(clientID);
    }

    @Override
    public Optional<OAuthApplication> findByAudience(String audience) {
        return oAuthApplicationRepository.findByAudience(audience);
    }

    @Override
    public List<OAuthApplication> findResourceServersForTargetApplication(UUID targetApplicationID) {
        return oAuthApplicationRepository.findAllByAllowedClientsTargetApplicationId(targetApplicationID);
    }

    @Override
    public long count() {
        return oAuthApplicationRepository.count();
    }

    @Override
    public void delete(UUID applicationID) {
        oAuthApplicationRepository.deleteById(applicationID);
    }
}
