package hu.psprog.leaflet.lags.core.service.account;

/**
 * Interface for handling account related operations.
 *
 * @param <T> type of request model to be processed
 * @param <R> type of the response model to be returned after processing
 * @author Peter Smith
 */
public interface AccountRequestHandler<T, R> {

    /**
     * Processes the given account related request.
     *
     * @param accountRequest source request object of type T
     * @return response object of R
     */
    R processAccountRequest(T accountRequest);
}
