package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.registry.RoleToAuthorityMappingRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

/**
 * {@link LocalUserUserDetailsService} implementation handling all users, regardless their account type.
 *
 * @author Peter Smith
 */
@Service
public class AllLocalUserUserDetailsService extends LocalUserUserDetailsService {

    @Autowired
    public AllLocalUserUserDetailsService(UserDAO userDAO, RoleToAuthorityMappingRegistry roleToAuthorityMappingRegistry) {
        super(userDAO, roleToAuthorityMappingRegistry);
    }

    @Override
    protected Predicate<User> userFilter() {
        return user -> true;
    }
}
