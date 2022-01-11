package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository interface for users.
 *
 * @author Peter Smith
 */
@org.springframework.stereotype.Repository
public interface UserRepository extends Repository<User, Long> {

    /**
     * Saves a new user into the database.
     *
     * @param entity user object
     * @return updated user object (with its generated ID)
     */
    User save(User entity);

    /**
     * Retrieves a registered user by the provided email address.
     *
     * @param email email address of the registered user
     * @return registered user as {@link User} object wrapped in {@link Optional} or empty Optional if not found
     */
    Optional<User> findByEmail(String email);
}
