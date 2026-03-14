package hu.psprog.leaflet.lags.web.factory;

import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.web.model.ProfileScreen;
import hu.psprog.leaflet.lags.web.utility.ReturnDirectiveUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link ProfileViewFactory}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ProfileViewFactoryTest {

    @Mock
    private ReturnDirectiveUtility returnDirectiveUtility;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProfileViewFactory profileViewFactory;

    @Test
    public void shouldCreateLogoutReference() {

        // given
        given(returnDirectiveUtility.getRequiredReturnDirective(request)).willReturn("test");

        // when
        var result = profileViewFactory.createLogoutReference(request);

        // then
        assertThat(new String(Base64.getDecoder().decode(result)), equalTo("/profile?return=test"));
    }

    @ParameterizedTest
    @MethodSource("redirectionDataSource")
    public void shouldCreateRedirection(ProfileScreen profileScreen, ProfileOperationResult operationResult,
                                        String expectedViewName) {

        // when
        var result = profileViewFactory.createRedirection(profileScreen, operationResult);

        // then
        assertThat(result.getViewName(), equalTo(expectedViewName));
    }

    @ParameterizedTest
    @MethodSource("viewDataSource")
    public void shouldCreateViewWithoutParameters(ProfileScreen profileScreen, String expectedViewName) {

        // when
        var result = profileViewFactory.createView(profileScreen);

        // then
        assertThat(result.getViewName(), equalTo(expectedViewName));
        assertThat(result.getModel().isEmpty(), is(true));
    }

    @Test
    public void shouldCreateViewWithParameters() {

        // given
        var parameters = Map.<String, Object>of(
                "param1", "value1",
                "param2", "value2"
        );

        // when
        var result = profileViewFactory.createView(ProfileScreen.EDIT_ACCOUNT, parameters);

        // then
        assertThat(result.getViewName(), equalTo("views/profile_edit_account"));
        assertThat(result.getModel(), equalTo(parameters));
    }

    private static Stream<Arguments> redirectionDataSource() {

        return Stream.of(
                Arguments.of(ProfileScreen.EDIT_ACCOUNT, ProfileOperationResult.SUCCESS,
                        "redirect:/profile?operation=edit-account"),
                Arguments.of(ProfileScreen.EDIT_ACCOUNT, ProfileOperationResult.NEW_EMAIL_IN_USE,
                        "redirect:/profile/edit-account?status=email-in-use"),
                Arguments.of(ProfileScreen.EDIT_ACCOUNT, ProfileOperationResult.UNKNOWN_ERROR,
                        "redirect:/profile/edit-account?status=unknown-error"),
                Arguments.of(ProfileScreen.CHANGE_PASSWORD, ProfileOperationResult.SUCCESS,
                        "redirect:/profile?operation=change-password"),
                Arguments.of(ProfileScreen.CHANGE_PASSWORD, ProfileOperationResult.CURRENT_PASSWORD_MISMATCH,
                        "redirect:/profile/change-password?status=current-password-mismatch"),
                Arguments.of(ProfileScreen.CHANGE_PASSWORD, ProfileOperationResult.NEW_PASSWORD_MISMATCH,
                        "redirect:/profile/change-password?status=new-password-mismatch"),
                Arguments.of(ProfileScreen.DELETE_ACCOUNT, ProfileOperationResult.CURRENT_PASSWORD_MISMATCH,
                        "redirect:/profile/delete-account?status=current-password-mismatch")

        );
    }

    private static Stream<Arguments> viewDataSource() {

        return Stream.of(
                Arguments.of(ProfileScreen.MAIN, "views/profile_main"),
                Arguments.of(ProfileScreen.EDIT_ACCOUNT, "views/profile_edit_account"),
                Arguments.of(ProfileScreen.CHANGE_PASSWORD, "views/profile_change_password"),
                Arguments.of(ProfileScreen.DELETE_ACCOUNT, "views/profile_delete_account")
        );
    }
}
