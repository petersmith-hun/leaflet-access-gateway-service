package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import hu.psprog.leaflet.lags.core.service.OAuthApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ApplicationRegistrationController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ApplicationRegistrationControllerTest {

    @Mock
    private OAuthApplicationService oAuthApplicationService;

    @InjectMocks
    private ApplicationRegistrationController applicationRegistrationController;

    @Test
    public void shouldGetOAuthApplication() {

        // given
        var id = UUID.randomUUID();

        // when
        applicationRegistrationController.getOAuthApplication(id);

        // then
        verify(oAuthApplicationService).getApplication(id);
    }

    @Test
    public void shouldGetOAuthApplications() {

        // given
        var page = 1;

        // when
        applicationRegistrationController.getOAuthApplications(page);

        // then
        verify(oAuthApplicationService).getApplications(page);
    }

    @Test
    public void shouldCreateApplication() {

        // given
        var application = OAuthApplicationRegistrationRequest.builder()
                .name("Test Application")
                .build();

        // when
        applicationRegistrationController.createApplication(application);

        // then
        verify(oAuthApplicationService).createApplication(application);
    }

    @Test
    public void shouldEditApplication() {

        // given
        var id = UUID.randomUUID();
        var application = OAuthApplicationRegistrationRequest.builder()
                .name("Test Application")
                .build();

        // when
        applicationRegistrationController.editApplication(id, application);

        // then
        verify(oAuthApplicationService).editApplication(id, application);
    }

    @Test
    public void shouldRegenerateApplicationSecret() {

        // given
        var id = UUID.randomUUID();

        // when
        applicationRegistrationController.regenerateApplicationSecret(id);

        // then
        verify(oAuthApplicationService).regenerateApplicationSecret(id);
    }

    @Test
    public void shouldEnableApplication() {

        // given
        var id = UUID.randomUUID();

        // when
        applicationRegistrationController.enableApplication(id);

        // then
        verify(oAuthApplicationService).updateApplicationStatus(id, true);
    }

    @Test
    public void shouldDisableApplication() {

        // given
        var id = UUID.randomUUID();

        // when
        applicationRegistrationController.disableApplication(id);

        // then
        verify(oAuthApplicationService).updateApplicationStatus(id, false);
    }

    @Test
    public void shouldDeleteApplication() {

        // given
        var id = UUID.randomUUID();

        // when
        applicationRegistrationController.deleteApplication(id);

        // then
        verify(oAuthApplicationService).deleteApplication(id);
    }
}