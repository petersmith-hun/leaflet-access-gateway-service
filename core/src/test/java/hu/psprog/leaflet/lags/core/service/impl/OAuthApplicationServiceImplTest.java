package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationRegistrationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationSummaryResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingOAuthApplicationRegistrationException;
import hu.psprog.leaflet.lags.core.exception.OAuthApplicationNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.OAuthApplicationMapper;
import hu.psprog.leaflet.lags.core.mapper.OAuthApplicationRegistrationRequestMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.service.util.SecretGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link OAuthApplicationServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthApplicationServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecretGenerator secretGenerator;

    @Mock
    private OAuthApplicationDAO oAuthApplicationDAO;

    @Mock
    private OAuthApplicationMapper oAuthApplicationMapper;

    @Mock
    private OAuthApplicationRegistrationRequestMapper oAuthApplicationRegistrationRequestMapper;

    @InjectMocks
    private OAuthApplicationServiceImpl oAuthApplicationService;

    @Test
    public void shouldCreateApplication() {

        // given
        var request = OAuthApplicationRegistrationRequest.builder()
                .name("app1")
                .clientID("client-id-app1")
                .build();
        var rawSecret = "secret-1";
        var encryptedSecret = "encrypted-secret-1";
        var applicationID = UUID.randomUUID();
        var mappedEntity = OAuthApplication.builder().build();
        var savedEntity = OAuthApplication.builder()
                .id(applicationID)
                .build();

        var expectedResponse = OAuthApplicationRegistrationResponse.builder()
                .id(applicationID)
                .clientSecret(rawSecret)
                .build();

        given(oAuthApplicationRegistrationRequestMapper.mapApplication(request)).willReturn(mappedEntity);
        given(secretGenerator.generateSecret()).willReturn(rawSecret);
        given(passwordEncoder.encode(rawSecret)).willReturn(encryptedSecret);
        given(oAuthApplicationDAO.save(mappedEntity)).willReturn(savedEntity);

        // when
        var result = oAuthApplicationService.createApplication(request);

        // then
        assertThat(result, equalTo(expectedResponse));
        assertThat(mappedEntity.getClientSecret(), equalTo(encryptedSecret));
    }

    @Test
    public void shouldCreateApplicationThrowExceptionOnDataIntegrityViolation() {

        // given
        var request = OAuthApplicationRegistrationRequest.builder()
                .name("app1")
                .clientID("client-id-app1")
                .build();
        var rawSecret = "secret-1";
        var encryptedSecret = "encrypted-secret-1";
        var mappedEntity = OAuthApplication.builder().build();

        given(oAuthApplicationRegistrationRequestMapper.mapApplication(request)).willReturn(mappedEntity);
        given(secretGenerator.generateSecret()).willReturn(rawSecret);
        given(passwordEncoder.encode(rawSecret)).willReturn(encryptedSecret);
        given(oAuthApplicationDAO.save(mappedEntity)).willThrow(DataIntegrityViolationException.class);

        // when
        assertThrows(ConflictingOAuthApplicationRegistrationException.class,
                () -> oAuthApplicationService.createApplication(request));

        // then
        // exception expected
    }

    @Test
    public void shouldEditApplication() {

        // given
        var applicationID = UUID.randomUUID();
        var encryptedSecret = "encrypted-secret-1";
        var request = OAuthApplicationRegistrationRequest.builder()
                .name("app1")
                .clientID("client-id-app1")
                .build();
        var currentEntity = OAuthApplication.builder()
                .id(applicationID)
                .clientSecret(encryptedSecret)
                .enabled(true)
                .build();
        var mappedEntity = OAuthApplication.builder().build();

        var expectedResponse = OAuthApplicationRegistrationResponse.builder()
                .id(applicationID)
                .build();

        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.of(currentEntity));
        given(oAuthApplicationRegistrationRequestMapper.mapApplication(request)).willReturn(mappedEntity);

        // when
        var result = oAuthApplicationService.editApplication(applicationID, request);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldEditApplicationThrowExceptionOnMissingApplication() {

        // given
        var applicationID = UUID.randomUUID();
        var request = OAuthApplicationRegistrationRequest.builder()
                .name("app1")
                .clientID("client-id-app1")
                .build();

        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.empty());

        // when
        assertThrows(OAuthApplicationNotFoundException.class,
                () -> oAuthApplicationService.editApplication(applicationID, request));

        // then
        // exception expected
    }

    @Test
    public void shouldGetApplication() {

        // given
        var applicationID = UUID.randomUUID();
        var resourceServers = List.of(OAuthApplication.builder().id(UUID.randomUUID()).build());
        var storedEntity = OAuthApplication.builder()
                .id(applicationID)
                .build();

        var expectedResponse = OAuthApplicationResponse.builder()
                .id(applicationID)
                .build();

        given(oAuthApplicationDAO.findResourceServersForTargetApplication(applicationID)).willReturn(resourceServers);
        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.of(storedEntity));
        given(oAuthApplicationMapper.mapApplication(storedEntity, resourceServers)).willReturn(expectedResponse);

        // when
        var result = oAuthApplicationService.getApplication(applicationID);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldGetApplicationThrowExceptionOnMissingApplication() {

        // given
        var applicationID = UUID.randomUUID();

        given(oAuthApplicationDAO.findResourceServersForTargetApplication(applicationID)).willReturn(List.of());
        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.empty());

        // when
        assertThrows(OAuthApplicationNotFoundException.class,
                () -> oAuthApplicationService.getApplication(applicationID));

        // then
        // exception expected
    }

    @Test
    public void shouldGetApplications() {

        // given
        var page = 1;
        var applicationID = UUID.randomUUID();
        var applications = List.of(OAuthApplication.builder().id(applicationID).build());

        var expectedPageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        var expectedApplication = OAuthApplicationSummaryResponse.builder()
                .id(applicationID)
                .build();

        given(oAuthApplicationDAO.findAll(expectedPageRequest)).willReturn(new PageImpl<>(applications));
        given(oAuthApplicationMapper.mapApplicationSummary(applications.getFirst())).willReturn(expectedApplication);

        // when
        var result = oAuthApplicationService.getApplications(page);

        // then
        assertThat(result.getContent(), equalTo(List.of(expectedApplication)));
    }

    @Test
    public void shouldGetApplicationsUnpaged() {

        // given
        var page = 0;
        var applicationID = UUID.randomUUID();
        var applications = List.of(OAuthApplication.builder().id(applicationID).build());

        var expectedPageRequest = Pageable.unpaged(Sort.by("name").ascending());
        var expectedApplication = OAuthApplicationSummaryResponse.builder()
                .id(applicationID)
                .build();

        given(oAuthApplicationDAO.findAll(expectedPageRequest)).willReturn(new PageImpl<>(applications));
        given(oAuthApplicationMapper.mapApplicationSummary(applications.getFirst())).willReturn(expectedApplication);

        // when
        var result = oAuthApplicationService.getApplications(page);

        // then
        assertThat(result.getContent(), equalTo(List.of(expectedApplication)));
    }

    @Test
    public void shouldUpdateApplicationStatusToEnabled() {

        // given
        var applicationID = UUID.randomUUID();
        var currentEntity = OAuthApplication.builder()
                .name("app1")
                .clientId("client-id-app1")
                .enabled(false)
                .build();

        var expectedResponse = OAuthApplicationResponse.builder()
                .id(applicationID)
                .build();

        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.of(currentEntity));
        given(oAuthApplicationDAO.save(currentEntity)).willReturn(currentEntity);
        given(oAuthApplicationDAO.findResourceServersForTargetApplication(applicationID)).willReturn(List.of());
        given(oAuthApplicationMapper.mapApplication(currentEntity, List.of())).willReturn(expectedResponse);

        // when
        var result = oAuthApplicationService.updateApplicationStatus(applicationID, true);

        // then
        assertThat(result, equalTo(expectedResponse));
        assertThat(currentEntity.isEnabled(), is(true));
    }

    @Test
    public void shouldUpdateApplicationStatusToDisabled() {

        // given
        var applicationID = UUID.randomUUID();
        var currentEntity = OAuthApplication.builder()
                .name("app1")
                .clientId("client-id-app1")
                .enabled(true)
                .build();

        var expectedResponse = OAuthApplicationResponse.builder()
                .id(applicationID)
                .build();

        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.of(currentEntity));
        given(oAuthApplicationDAO.save(currentEntity)).willReturn(currentEntity);
        given(oAuthApplicationDAO.findResourceServersForTargetApplication(applicationID)).willReturn(List.of());
        given(oAuthApplicationMapper.mapApplication(currentEntity, List.of())).willReturn(expectedResponse);

        // when
        var result = oAuthApplicationService.updateApplicationStatus(applicationID, false);

        // then
        assertThat(result, equalTo(expectedResponse));
        assertThat(currentEntity.isEnabled(), is(false));
    }

    @Test
    public void shouldRegenerateApplicationSecret() {

        // given
        var applicationID = UUID.randomUUID();
        var rawSecret = "secret-1";
        var encryptedSecret = "encrypted-secret-1";
        var currentEntity = OAuthApplication.builder()
                .name("app1")
                .clientId("client-id-app1")
                .enabled(true)
                .build();

        var expectedResponse = OAuthApplicationRegistrationResponse.builder()
                .id(applicationID)
                .clientSecret(rawSecret)
                .build();


        given(oAuthApplicationDAO.findByID(applicationID)).willReturn(Optional.of(currentEntity));
        given(secretGenerator.generateSecret()).willReturn(rawSecret);
        given(passwordEncoder.encode(rawSecret)).willReturn(encryptedSecret);
        given(oAuthApplicationDAO.save(currentEntity)).willReturn(currentEntity);

        // when
        var result = oAuthApplicationService.regenerateApplicationSecret(applicationID);

        // then
        assertThat(result, equalTo(expectedResponse));
        assertThat(currentEntity.getClientSecret(), equalTo(encryptedSecret));
    }

    @Test
    public void shouldDeleteApplication() {

        // given
        var applicationID = UUID.randomUUID();

        // when
        oAuthApplicationService.deleteApplication(applicationID);

        // then
        verify(oAuthApplicationDAO).delete(applicationID);
    }

    @Test
    public void shouldDeleteApplicationThrowExceptionOnDataIntegrityViolation() {

        // given
        var applicationID = UUID.randomUUID();

        doThrow(DataIntegrityViolationException.class).when(oAuthApplicationDAO).delete(applicationID);

        // when
        assertThrows(ConflictingOAuthApplicationRegistrationException.class,
                () -> oAuthApplicationService.deleteApplication(applicationID));

        // then
        // exception expected
    }
}
