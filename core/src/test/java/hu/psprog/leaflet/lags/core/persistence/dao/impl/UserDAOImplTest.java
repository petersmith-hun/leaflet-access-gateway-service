package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link UserDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class UserDAOImplTest {

    private static final Long ID = 1L;
    private static final User USER = new User();
    private static final String EMAIL_ADDRESS = "test1@dev.local";

    @Mock
    private Pageable pageable;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDAOImpl userDAO;

    @Test
    public void shouldFindAll() {

        // when
        userDAO.findAll(pageable);

        // then
        verify(userRepository).findAll(pageable);
    }

    @Test
    public void shouldFindByID() {

        // when
        userDAO.findByID(ID);

        // then
        verify(userRepository).findById(ID);
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

    @Test
    public void shouldSave() {

        // given
        given(userRepository.saveAndFlush(USER)).willReturn(USER);

        // when
        User result = userDAO.save(USER);

        // then
        assertThat(result, equalTo(USER));
    }

    @Test
    public void shouldUpdateLastLogin() {

        // when
        userDAO.updateLastLogin(ID);

        // then
        verify(userRepository).updateLastLoginDate(ID);
    }
}
