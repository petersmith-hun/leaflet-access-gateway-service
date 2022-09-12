package hu.psprog.leaflet.lags.core.domain.internal;

import lombok.Data;

/**
 * Domain class for GitHub user emails API call responses.
 *
 * @author Peter Smith
 */
@Data
public class GitHubEmailItem {

    private boolean primary;
    private String email;
}
