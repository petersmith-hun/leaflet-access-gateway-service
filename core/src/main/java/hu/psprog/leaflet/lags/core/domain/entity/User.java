package hu.psprog.leaflet.lags.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Domain class representing a local user.
 *
 * @author Peter Smith
 */
@Data
@Entity
@Table(
        name = DatabaseConstants.TABLE_USERS,
        uniqueConstraints = @UniqueConstraint(
                columnNames = DatabaseConstants.COLUMN_EMAIL,
                name = DatabaseConstants.UK_USER_EMAIL))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = DatabaseConstants.COLUMN_USERNAME)
    @NotNull
    @Size(max = 255)
    private String username;

    @Column(name = DatabaseConstants.COLUMN_EMAIL)
    @NotNull
    @Size(max = 255)
    private String email;

    @Column(name = DatabaseConstants.COLUMN_ROLE)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = DatabaseConstants.COLUMN_PASSWORD)
    @Size(max = 255)
    private String password;

    @Column(name = DatabaseConstants.COLUMN_DEFAULT_LOCALE)
    @Enumerated(EnumType.STRING)
    private SupportedLocale defaultLocale;

    @Column(name = DatabaseConstants.COLUMN_ACCOUNT_TYPE)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = DatabaseConstants.COLUMN_DATE_LAST_LOGIN)
    private Date lastLogin;

    @Column(name = DatabaseConstants.COLUMN_DATE_CREATED)
    private Date created;

    @Column(name = DatabaseConstants.COLUMN_DATE_LAST_MODIFIED)
    private Date lastModified;

    @Column(name = DatabaseConstants.COLUMN_IS_ENABLED)
    private boolean enabled;
}
