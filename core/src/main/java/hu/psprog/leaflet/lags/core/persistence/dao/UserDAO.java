package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * DAO interface for user entities.
 *
 * @author Peter Smith
 */
public interface UserDAO {

    /**
     * Retrieves a page of existing {@link User} entities.
     *
     * @param pageable page settings
     * @return page of {@link User} entities
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Retrieves a {@link User} record by its ID.
     *
     * @param id ID of the user
     * @return identified {@link User} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<User> findByID(Long id);

    /**
     * Retrieves a registered user by the provided email address.
     *
     * @param email email address of the registered user
     * @return registered user as {@link User} object wrapped in {@link Optional} or empty Optional if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Saves a new user into the database.
     *
     * @param entity user object
     * @return updated user object (with its generated ID)
     */
    User save(User entity);

    /**
     * Updates the last login of the given user to the current timestamp.
     *
     * @param id ID of the user to update last login timestamp of
     */
    void updateLastLogin(Long id);
}
