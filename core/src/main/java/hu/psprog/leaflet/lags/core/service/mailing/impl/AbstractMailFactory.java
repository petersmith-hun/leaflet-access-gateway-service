package hu.psprog.leaflet.lags.core.service.mailing.impl;

import hu.psprog.leaflet.lags.core.service.mailing.MailFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Abstract implementation of {@link MailFactory} interface providing common tools for generating Mail objects.
 *
 * @param <T> T type of mail content wrapper model
 * @author Peter Smith
 */
@ConfigurationProperties(prefix = "tms")
abstract class AbstractMailFactory<T> implements MailFactory<T> {

    private final MessageSource messageSource;
    private Locale forcedLocale;

    AbstractMailFactory(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Translates the given message key to the currently set forced locale.
     *
     * @param subjectMessageKey message key for the mail's subject
     * @return translated subject value
     */
    String translateSubject(String subjectMessageKey) {
        return messageSource.getMessage(subjectMessageKey, null, subjectMessageKey, forcedLocale);
    }

    public void setForcedLocale(Locale forcedLocale) {
        this.forcedLocale = forcedLocale;
    }
}
