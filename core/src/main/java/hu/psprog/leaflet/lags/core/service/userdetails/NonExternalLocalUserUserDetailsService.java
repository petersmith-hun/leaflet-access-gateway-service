package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.registry.RoleToAuthorityMappingRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

/**
 * {@link LocalUserUserDetailsService} implementation handling users only with local account type (excluding any account
 * created via the registered OAuth providers).
 *
 * @author Peter Smith
 */
@Service
public class NonExternalLocalUserUserDetailsService extends LocalUserUserDetailsService {

    @Autowired
    public NonExternalLocalUserUserDetailsService(UserDAO userDAO, RoleToAuthorityMappingRegistry roleToAuthorityMappingRegistry) {
        super(userDAO, roleToAuthorityMappingRegistry);
    }

    @Override
    protected Predicate<User> userFilter() {
        return user -> user.getAccountType() == AccountType.LOCAL;
    }
}
