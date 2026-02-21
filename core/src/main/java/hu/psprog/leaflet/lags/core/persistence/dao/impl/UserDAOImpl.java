package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link UserDAO} implementation.
 *
 * @author Peter Smith
 */
@Component
public class UserDAOImpl implements UserDAO {

    private final UserRepository userRepository;

    @Autowired
    public UserDAOImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findByID(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User entity) {
        return userRepository.saveAndFlush(entity);
    }

    @Override
    public void updateLastLogin(Long id) {
        userRepository.updateLastLoginDate(id);
    }
}
