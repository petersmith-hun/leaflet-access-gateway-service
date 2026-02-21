package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository interface for users.
 *
 * @author Peter Smith
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a registered user by the provided email address.
     *
     * @param email email address of the registered user
     * @return registered user as {@link User} object wrapped in {@link Optional} or empty Optional if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Updates the last login of the given user to the current timestamp.
     *
     * @param id ID of the user to update last login timestamp of
     */
    @Modifying
    @Query("update User u set u.lastLogin = current_timestamp where u.id = :id")
    void updateLastLoginDate(@Param("id") Long id);
}
