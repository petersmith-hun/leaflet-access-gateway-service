package hu.psprog.leaflet.lags.core.service.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility implementation for creating {@link Pageable} page definitions.
 *
 * @author Peter Smith
 */
public final class PaginationUtil {

    private static final int PAGE_SIZE = 10;
    private static final Sort DEFAULT_PAGE_SORT = Sort.by("name").ascending();

    private PaginationUtil() { }

    /**
     * Creates a {@link Pageable} page definition from the given, 1-based page number. Adds default sorting by name, and
     * a default page size of 10. If the given page number is less than 1, created an unpaged definition, using the
     * aforementioned default sorting.
     *
     * @param page 1-based page number, pass 0 to turn off pagination
     * @return created {@link Pageable} instance
     */
    public static Pageable createPageRequest(int page) {
        return createPageRequest(page, DEFAULT_PAGE_SORT);
    }

    /**
     * Creates a {@link Pageable} page definition from the given, 1-based page number. Adds default sorting by name, and
     * a default page size of 10. If the given page number is less than 1, created an unpaged definition, using the
     * aforementioned default sorting.
     *
     * @param page 1-based page number, pass 0 to turn off pagination
     * @param sort custom override for sorting directive
     * @return created {@link Pageable} instance
     */
    public static Pageable createPageRequest(int page, Sort sort) {

        return page < 1
                ? Pageable.unpaged(sort)
                : PageRequest.of(page - 1, PAGE_SIZE, sort);
    }
}
