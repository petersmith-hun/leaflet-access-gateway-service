package hu.psprog.leaflet.lags.core.service.mailing.impl;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.service.mailing.MailFactory;
import hu.psprog.leaflet.mail.domain.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@link MailFactory} implementation for password reset request mails.
 *
 * @author Peter Smith
 */
@Component
public class PasswordResetRequestMailFactory extends AbstractMailFactory<PasswordResetRequest> {

    private static final String PASSWORD_RESET_REQUEST_MAIL_SUBJECT = "mail.user.pwreset.demand.subject";
    private static final String PASSWORD_RESET_REQUEST_MAIL_TEMPLATE = "pw_reset_request.html";

    private static final String GENERATED_AT = "generatedAt";
    private static final String EXPIRATION = "expiration";
    private static final String USERNAME = "username";
    private static final String TOKEN = "token";
    private static final String RESET_LINK = "resetLink";

    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public PasswordResetRequestMailFactory(MessageSource messageSource, AuthenticationConfig authenticationConfig) {
        super(messageSource);
        this.authenticationConfig = authenticationConfig;
    }

    @Override
    public Mail buildMail(PasswordResetRequest content, String... recipient) {

        Assert.isTrue(Objects.nonNull(recipient) && recipient.length == 1, "Exactly one recipient is required.");

        return Mail.getBuilder()
                .withRecipient(recipient[0])
                .withSubject(translateSubject(PASSWORD_RESET_REQUEST_MAIL_SUBJECT))
                .withTemplate(PASSWORD_RESET_REQUEST_MAIL_TEMPLATE)
                .withContentMap(createContentMap(content))
                .build();
    }

    private Map<String, Object> createContentMap(PasswordResetRequest passwordResetRequest) {

        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put(TOKEN, passwordResetRequest.getToken());
        contentMap.put(USERNAME, passwordResetRequest.getUsername());
        contentMap.put(EXPIRATION, passwordResetRequest.getExpiration());
        contentMap.put(RESET_LINK, authenticationConfig.getPasswordReset().getReturnUrl());
        contentMap.put(GENERATED_AT, DATE_FORMAT.format(new Date()));

        return contentMap;
    }
}
