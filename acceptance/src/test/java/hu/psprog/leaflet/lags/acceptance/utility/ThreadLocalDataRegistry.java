package hu.psprog.leaflet.lags.acceptance.utility;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Thread-local data map utility for transferring data between test steps.
 *
 * @author Peter Smith
 */
public class ThreadLocalDataRegistry {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_DATA_REGISTRY = new ThreadLocal<>();

    private ThreadLocalDataRegistry() {
    }

    /**
     * Stores an attribute value in the thread-local data map.
     *
     * @param attribute attribute key as {@link TestConstants.Attribute}
     * @param value value of the attribute
     */
    public static void put(TestConstants.Attribute attribute, Object value) {
        putValue(attribute, value);
    }

    /**
     * Stores a {@link ResponseEntity} object under the {@link TestConstants.Attribute#RESPONSE_ENTITY} attribute key.
     *
     * @param responseEntity {@link ResponseEntity} object to be stored
     */
    public static void putResponseEntity(ResponseEntity<?> responseEntity) {
        putValue(TestConstants.Attribute.RESPONSE_ENTITY, responseEntity);
    }

    /**
     * Sets the given boolean flag of {@link TestConstants.Flag} key to {@code true}.
     *
     * @param key boolean flag key as {@link TestConstants.Flag}
     */
    public static void putFlag(TestConstants.Flag key) {
        putValue(key, true);
    }

    /**
     * Returns the stored value of the given attribute key.
     *
     * @param key attribute key as {@link TestConstants.Attribute}
     * @param <T> type of the returned object
     * @return the stored object, or {@code null} if not present
     */
    public static <T> T get(TestConstants.Attribute key) {
        return (T) THREAD_LOCAL_DATA_REGISTRY.get().get(key.getValue());
    }

    /**
     * Returns the stored {@link ResponseEntity} object.
     *
     * @param <T> type of the object within the stored {@link ResponseEntity}
     * @return the stored {@link ResponseEntity} object, or {@code null} if not present
     */
    public static <T> ResponseEntity<T> getResponseEntity() {
        return (ResponseEntity<T>) THREAD_LOCAL_DATA_REGISTRY.get().get(TestConstants.Attribute.RESPONSE_ENTITY.getValue());
    }

    /**
     * Returns the stored boolean flag value.
     *
     * @param key key of the boolean flag as {@link TestConstants.Flag}
     * @return the stored flag value, or {@code false} if the flag is not present
     */
    public static boolean getFlag(TestConstants.Flag key) {
        return (boolean) THREAD_LOCAL_DATA_REGISTRY.get().getOrDefault(key.getValue(), false);
    }

    /**
     * Clears the thread-local data map.
     */
    public static void reset() {
        THREAD_LOCAL_DATA_REGISTRY.get().clear();
    }

    private static void putValue(TestConstants key, Object value) {

        if (Objects.isNull(THREAD_LOCAL_DATA_REGISTRY.get())) {
            THREAD_LOCAL_DATA_REGISTRY.set(new HashMap<>());
        }

        THREAD_LOCAL_DATA_REGISTRY.get().put(key.getValue(), value);
    }
}
