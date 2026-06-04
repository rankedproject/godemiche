package wtf.ranked.godemiche.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NullMarked;

/**
 * A thread-safe pool that ensures a single instance per unique identifier.
 */
@NullMarked
public final class ObjectPool {

    private ObjectPool() {

    }

    private static final Map<String, Object> CREATED_OBJECTS = new ConcurrentHashMap<>();
    private static final Multimap<Class<?>, Object> CREATED_OBJECTS_BY_TYPE = ArrayListMultimap.create();

    /**
     * Retrieves the instance associated with the identifier. If absent, the factory
     * creates exactly one instance which is then cached for all future calls.
     * * <pre>{@code
     * final ExecutorService executor = ObjectPool.get("executorService4cores", () -> Executors.newFixedThreadPool(4));
     * executor.submit(() -> doSomething());
     * }</pre>
     *
     * @param identifier    Unique key.
     * @param objectFactory Factory called only if the identifier is not already registered.
     * @param <T>           Object type.
     * @return The unique instance mapped to the identifier
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(final String identifier, final Supplier<T> objectFactory) {
        return (T) CREATED_OBJECTS.computeIfAbsent(identifier, $ -> {
            final T object = objectFactory.get();
            CREATED_OBJECTS_BY_TYPE.put(object.getClass(), object);
            return object;
        });
    }

    /**
     * Returns an unmodifiable collection sync all cached objects sync the specified type.
     *
     * @param objectType The class to filter by.
     * @param <T>        The type.
     * @return All instances in the pool belonging to the provided class.
     */
    @UnmodifiableView
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> getAllExistingObjects(final Class<T> objectType) {
        return (Collection<T>) Collections.unmodifiableCollection(CREATED_OBJECTS_BY_TYPE.get(objectType));
    }
}
