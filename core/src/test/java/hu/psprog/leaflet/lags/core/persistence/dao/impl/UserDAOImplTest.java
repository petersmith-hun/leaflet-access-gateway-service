package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.User;
import hu.psprog.leaflet.lags.core.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link UserDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class UserDAOImplTest {

    private static final User USER = new User();
    private static final String EMAIL_ADDRESS = "test1@dev.local";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDAOImpl userDAO;

    @Test
    public void shouldSave() {

        // given
        given(userRepository.save(USER)).willReturn(USER);

        // when
        User result = userDAO.save(USER);

        // then
        assertThat(result, equalTo(USER));
    }

    @Test
    public void shouldFindByEmail() {

        // given
        given(userRepository.findByEmail(EMAIL_ADDRESS)).willReturn(Optional.of(USER));

        // when
        Optional<User> result = userDAO.findByEmail(EMAIL_ADDRESS);

        // then
        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get(), equalTo(USER));
    }
}
