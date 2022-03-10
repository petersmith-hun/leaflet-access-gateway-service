package hu.psprog.leaflet.lags.core.service.mailing;

import hu.psprog.leaflet.mail.domain.Mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * {@link MailFactory} implementations help to build up specific emails easily.
 *
 * @param <T> T type of mail content wrapper model
 * @author Peter Smith
 */
public interface MailFactory<T> {

    DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    /**
     * Builds a {@link Mail} object with given content and given recipient.
     *
     * @param content content of the mail
     * @param recipient recipient of the mail
     * @return Mail object
     */
    Mail buildMail(T content, String ... recipient);
}
