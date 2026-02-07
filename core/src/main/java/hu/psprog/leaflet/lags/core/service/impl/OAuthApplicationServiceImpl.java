package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationRegistrationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationSummaryResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.OAuthApplicationMapper;
import hu.psprog.leaflet.lags.core.mapper.OAuthApplicationRegistrationRequestMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.service.OAuthApplicationService;
import hu.psprog.leaflet.lags.core.service.util.PaginationUtil;
import hu.psprog.leaflet.lags.core.service.util.SecretGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link OAuthApplicationService}.
 *
 * @author Peter Smith
 */
@Slf4j
@Service
public class OAuthApplicationServiceImpl implements OAuthApplicationService {

    private final PasswordEncoder passwordEncoder;
    private final SecretGenerator secretGenerator;
    private final OAuthApplicationDAO oAuthApplicationDAO;
    private final OAuthApplicationMapper oAuthApplicationMapper;
    private final OAuthApplicationRegistrationRequestMapper oAuthApplicationRegistrationRequestMapper;

    @Autowired
    public OAuthApplicationServiceImpl(PasswordEncoder passwordEncoder, SecretGenerator secretGenerator,
                                       OAuthApplicationDAO oAuthApplicationDAO, OAuthApplicationMapper oAuthApplicationMapper,
                                       OAuthApplicationRegistrationRequestMapper oAuthApplicationRegistrationRequestMapper) {

        this.passwordEncoder = passwordEncoder;
        this.secretGenerator = secretGenerator;
        this.oAuthApplicationDAO = oAuthApplicationDAO;
        this.oAuthApplicationMapper = oAuthApplicationMapper;
        this.oAuthApplicationRegistrationRequestMapper = oAuthApplicationRegistrationRequestMapper;
    }

    @Transactional
    public OAuthApplicationRegistrationResponse createApplication(OAuthApplicationRegistrationRequest request) {

        OAuthApplication application = oAuthApplicationRegistrationRequestMapper.mapApplication(request);
        String unencryptedSecret = secretGenerator.generateSecret();
        application.setClientSecret(passwordEncoder.encode(unencryptedSecret));

        UUID savedApplicationID = exceptionAwareCall(() -> oAuthApplicationDAO.save(application).getId());

        log.info("OAuth application {} ({}) registered successfully", request.name(), request.clientID());

        return OAuthApplicationRegistrationResponse.builder()
                .clientSecret(unencryptedSecret)
                .id(savedApplicationID)
                .build();
    }

    @Override
    @Transactional
    public OAuthApplicationRegistrationResponse editApplication(UUID applicationID, OAuthApplicationRegistrationRequest request) {

        OAuthApplication currentApplicationData = findRequiredApplication(applicationID, Function.identity());
        OAuthApplication newApplicationData = oAuthApplicationRegistrationRequestMapper.mapApplication(request);
        newApplicationData.setId(applicationID);
        newApplicationData.setClientSecret(currentApplicationData.getClientSecret());
        newApplicationData.setEnabled(currentApplicationData.isEnabled());

        exceptionAwareCall(() -> oAuthApplicationDAO.save(newApplicationData));

        log.info("OAuth application {} ({}) updated successfully", request.name(), request.clientID());

        return OAuthApplicationRegistrationResponse.builder()
                .id(applicationID)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OAuthApplicationResponse getApplication(UUID applicationID) {

        List<OAuthApplication> resourceServersOfClient = oAuthApplicationDAO.findResourceServersForTargetApplication(applicationID);

        return findRequiredApplication(applicationID, application -> oAuthApplicationMapper.mapApplication(application, resourceServersOfClient));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OAuthApplicationSummaryResponse> getApplications(int page) {

        return oAuthApplicationDAO.findAll(PaginationUtil.createPageRequest(page))
                .map(oAuthApplicationMapper::mapApplicationSummary);
    }

    @Override
    @Transactional
    public OAuthApplicationResponse updateApplicationStatus(UUID applicationID, boolean enabled) {

        OAuthApplication currentApplicationData = findRequiredApplication(applicationID, Function.identity());
        currentApplicationData.setEnabled(enabled);

        exceptionAwareCall(() -> oAuthApplicationDAO.save(currentApplicationData));

        log.info("Status of OAuth application {} ({}) updated successfully to enabled={}", currentApplicationData.getName(), currentApplicationData.getClientId(), enabled);

        return getApplication(applicationID);
    }

    @Override
    @Transactional
    public OAuthApplicationRegistrationResponse regenerateApplicationSecret(UUID applicationID) {

        OAuthApplication application = findRequiredApplication(applicationID, Function.identity());
        String unencryptedSecret = secretGenerator.generateSecret();
        application.setClientSecret(passwordEncoder.encode(unencryptedSecret));

        exceptionAwareCall(() -> oAuthApplicationDAO.save(application));

        log.info("Secret of OAuth application {} ({}) regenerated successfully", application.getName(), application.getClientId());

        return OAuthApplicationRegistrationResponse.builder()
                .clientSecret(unencryptedSecret)
                .id(applicationID)
                .build();
    }

    @Override
    public void deleteApplication(UUID applicationID) {

        exceptionAwareCall(() -> oAuthApplicationDAO.delete(applicationID));

        log.info("OAuth application {} deleted successfully", applicationID);
    }

    private <T> T findRequiredApplication(UUID applicationID, Function<OAuthApplication, T> mapperFunction) {

        return oAuthApplicationDAO.findByID(applicationID)
                .map(mapperFunction)
                .orElseThrow(() -> {
                    log.error("OAuth application by ID={} not found", applicationID);
                    return ResourceNotFoundException.application(applicationID);
                });
    }

    private <T> T exceptionAwareCall(Supplier<T> call) {

        try {
            return call.get();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting definition: {}", exception.getMessage(), exception);
            throw ConflictingResourceException.onCreate(ManagedResourceType.APPLICATION);
        }
    }

    private void exceptionAwareCall(Runnable call) {

        try {
            call.run();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting definition: {}", exception.getMessage(), exception);
            throw ConflictingResourceException.onDelete(ManagedResourceType.APPLICATION);
        }
    }
}
