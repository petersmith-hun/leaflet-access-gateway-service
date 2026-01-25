package hu.psprog.leaflet.lags.core.mapper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * Common mapping implementations.
 *
 * @author Peter Smith
 */
abstract class AbstractCommonMapper {

    /**
     * Maps the given source timestamp of {@link Date} to {@link ZonedDateTime}.
     *
     * @param sourceDate source {@link Date} object to be converted
     * @return converted {@link ZonedDateTime} instance
     */
    protected ZonedDateTime convertDate(Date sourceDate) {

        return Optional.ofNullable(sourceDate)
                .map(date -> date.toInstant().atZone(ZoneId.systemDefault()))
                .orElse(null);
    }
}
