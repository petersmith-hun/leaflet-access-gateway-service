package hu.psprog.leaflet.lags.acceptance.stub;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.domain.PasswordResetSuccess;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.util.NotificationAdapter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Stub for {@link NotificationAdapter} interface utilizing the {@link ThreadLocalDataRegistry} to store the sent out
 * emails for later verification.
 *
 * @author Peter Smith
 */
@Component
@Primary
public class NotificationAdapterStub implements NotificationAdapter {

    @Override
    public void signUpConfirmation(SignUpConfirmation signUpConfirmation) {
        ThreadLocalDataRegistry.put(TestConstants.Attribute.SIGN_UP_CONFIRMATION_MAIL, signUpConfirmation);
    }

    @Override
    public void passwordResetRequested(PasswordResetRequest passwordResetRequest) {
        ThreadLocalDataRegistry.put(TestConstants.Attribute.PASSWORD_RESET_REQUEST_MAIL, passwordResetRequest);
    }

    @Override
    public void successfulPasswordReset(PasswordResetSuccess passwordResetSuccess) {
        ThreadLocalDataRegistry.put(TestConstants.Attribute.PASSWORD_RESET_CONFIRMATION_MAIL, passwordResetSuccess);
    }
}
