package hu.psprog.leaflet.lags.core.service.mailing.impl;

import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.mail.domain.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * {@link AbstractMailFactory} implementation to generate sign-up confirmation mails.
 *
 * @author Peter Smith
 */
@Component
public class SignUpConfirmationMailFactory extends AbstractMailFactory<SignUpConfirmation> {

    private static final String SIGN_UP_CONFIRMATION_MAIL_SUBJECT = "mail.user.signup.confirm.subject";
    private static final String SIGN_UP_CONFIRMATION_MAIL_TEMPLATE = "signup_confirm.html";

    private static final String GENERATED_AT = "generatedAt";
    private static final String USERNAME = "username";

    @Autowired
    public SignUpConfirmationMailFactory(MessageSource messageSource) {
        super(messageSource);
    }

    @Override
    public Mail buildMail(SignUpConfirmation content, String... recipient) {

        Assert.isTrue(Objects.nonNull(recipient) && recipient.length == 1, "Exactly one recipient is required.");

        return Mail.getBuilder()
                .withRecipient(recipient[0])
                .withSubject(translateSubject(SIGN_UP_CONFIRMATION_MAIL_SUBJECT))
                .withTemplate(SIGN_UP_CONFIRMATION_MAIL_TEMPLATE)
                .withContentMap(createContentMap(content.getUsername()))
                .build();
    }

    private Map<String, Object> createContentMap(String username) {

        return Map.of(
                USERNAME, username,
                GENERATED_AT, DATE_FORMAT.format(new Date()));
    }
}
