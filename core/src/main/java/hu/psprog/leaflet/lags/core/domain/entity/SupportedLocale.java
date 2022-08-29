package hu.psprog.leaflet.lags.core.domain.entity;

import java.util.Locale;

/**
 * Locales with language codes.
 *
 * @author Peter Smith
 */
public enum SupportedLocale {

    /**
     * Hungarian.
     */
    HU(Locale.forLanguageTag("HU")),

    /**
     * English (United States).
     */
    EN(Locale.US);

    private final Locale locale;

    SupportedLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the JDK {@link Locale} object assigned to a specific supported locale.
     *
     * @return assigned JDK {@link Locale} object
     */
    public Locale getLocale() {
        return locale;
    }
}
