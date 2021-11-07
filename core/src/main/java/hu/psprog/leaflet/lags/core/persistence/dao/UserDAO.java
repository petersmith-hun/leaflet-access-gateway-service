package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.persistence.repository.UserRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * DAO interface for user entities.
 *
 * @author Peter Smith
 */
@NoRepositoryBean
public interface UserDAO extends UserRepository {

}
