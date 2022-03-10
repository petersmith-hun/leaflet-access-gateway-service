package hu.psprog.leaflet.lags.core.service.mailing.observer;

import io.reactivex.Observable;

/**
 * Provides observer logic to an observable.
 *
 * @author Peter Smith
 */
public interface ObserverHandler<T> {

    /**
     * Attaches an observer to the given observable.
     *
     * @param observable an observable to attach observer to
     */
    void attachObserver(Observable<T> observable);
}
