package hu.psprog.leaflet.lags.web.rest.utility;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.web.exception.MissingReturnDirectiveException;
import hu.psprog.leaflet.lags.web.utility.ReturnDirectiveUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link ReturnDirectiveUtility}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ReturnDirectiveUtilityTest {

    @Mock
    private AuthenticationConfig authenticationConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ReturnDirectiveUtility returnDirectiveUtility;

    @Test
    public void shouldEnsureReturnDirectiveSuccessfullyWriteDirectiveIntoSession() {

        // given
        var returnDirective = "test123";

        given(request.getSession()).willReturn(session);
        given(request.getParameter("return")).willReturn(returnDirective);
        given(session.getAttribute("returnDirective"))
                .willReturn(null)
                .willReturn(returnDirective);

        // when
        returnDirectiveUtility.ensureReturnDirective(request);

        // then
        verify(session).setAttribute("returnDirective", returnDirective);
    }

    @Test
    public void shouldEnsureReturnDirectiveDoNothingIfDirectiveIsAlreadyInSession() {

        // given
        var returnDirective = "test123";

        given(request.getParameter("return")).willReturn(null);
        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn(returnDirective);

        // when
        returnDirectiveUtility.ensureReturnDirective(request);

        // then
        verifyNoMoreInteractions(request, session);
    }

    @Test
    public void shouldEnsureReturnDirectiveOverwriteExistingDirectiveIfPresentInQueryParameter() {

        // given
        var returnDirective = "test123";
        var newReturnDirective = "new-directive";

        given(request.getParameter("return")).willReturn(newReturnDirective);
        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn(returnDirective);

        // when
        returnDirectiveUtility.ensureReturnDirective(request);

        // then
        verify(session).setAttribute("returnDirective", newReturnDirective);
    }

    @Test
    public void shouldEnsureReturnDirectiveThrowExceptionOnMissingReturnDirective() {

        // given
        given(request.getSession()).willReturn(session);
        given(request.getParameter("return")).willReturn(null);
        given(session.getAttribute("returnDirective"))
                .willReturn(null)
                .willReturn(null);

        // when
        assertThrows(MissingReturnDirectiveException.class,
                () -> returnDirectiveUtility.ensureReturnDirective(request));

        // then
        // exception expected

        verify(session).setAttribute("returnDirective", null);
        verifyNoMoreInteractions(request, session);
    }

    @Test
    public void shouldGetRequiredReturnDirectiveReturnFromSession() {

        // given
        var returnDirective = "test123";

        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn(returnDirective);

        // when
        var result = returnDirectiveUtility.getRequiredReturnDirective(request);

        // then
        assertThat(result, equalTo(result));
    }

    @Test
    public void shouldGetRequiredReturnDirectiveThrowExceptionOnMissingReturnDirective() {

        // given
        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn(null);

        // when
        assertThrows(MissingReturnDirectiveException.class,
                () -> returnDirectiveUtility.getRequiredReturnDirective(request));

        // then
        // exception expected
    }

    @Test
    public void shouldGetReturnDefinitionWithSuccess() {

        // given
        var returnDirective = "test123";
        var definition1 = new AuthenticationConfig.ReturnDefinition();
        definition1.setReturnUrl("/returnUrl1");
        definition1.setDisplayName("displayName1");
        var definition2 = new AuthenticationConfig.ReturnDefinition();

        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn(returnDirective);
        given(authenticationConfig.getReturnDefinitions()).willReturn(Map.of(
                returnDirective, definition1,
                "something-else", definition2
        ));

        // when
        var result =  returnDirectiveUtility.getReturnDefinition(request);

        // then
        assertThat(result, equalTo(definition1));
    }

    @Test
    public void shouldGetReturnDefinitionThrowExceptionOnMissingReturnDirective() {

        // given
        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn(null);

        // when
        assertThrows(MissingReturnDirectiveException.class,
                () -> returnDirectiveUtility.getReturnDefinition(request));

        // then
        // exception expected
    }

    @Test
    public void shouldGetReturnDefinitionThrowExceptionOnMissingReturnDefinition() {

        // given
        given(request.getSession()).willReturn(session);
        given(session.getAttribute("returnDirective")).willReturn("no-def-for-this");
        given(authenticationConfig.getReturnDefinitions()).willReturn(Map.of(
                "def1", new AuthenticationConfig.ReturnDefinition(),
                "def2", new AuthenticationConfig.ReturnDefinition()
        ));

        // when
        assertThrows(MissingReturnDirectiveException.class,
                () -> returnDirectiveUtility.getReturnDefinition(request));

        // then
        // exception expected
    }
}
