package hu.psprog.leaflet.lags.core.domain.entity;

import lombok.Getter;

import java.util.Locale;

/**
 * Locales with language codes.
 *
 * @author Peter Smith
 */
@Getter
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
}
