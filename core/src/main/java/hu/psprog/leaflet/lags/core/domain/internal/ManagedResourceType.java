package hu.psprog.leaflet.lags.core.domain.internal;

import org.apache.commons.lang3.StringUtils;

/**
 * Known resource types.
 *
 * @author Peter Smith
 */
public enum ManagedResourceType {

    APPLICATION,
    PERMISSION,
    ROLE;

    /**
     * Returns the enum with starting with the first character uppercase, and the rest lowercase.
     *
     * @return formatted display name
     */
    public String getDisplayName() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
