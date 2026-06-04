package wtf.ranked.godemiche;

import wtf.ranked.godemiche.util.ObjectPool;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

final class ObjectPoolTest {

    @Test
    void get_SameIdentifier_ReturnsSameInstanceAndFactoryCalledOnce() {
        final AtomicInteger factoryInvocationCount = new AtomicInteger();
        final Supplier<String> stringFactory = () -> {
            factoryInvocationCount.incrementAndGet();
            return "test-value";
        };

        final String firstRetrievedObject = ObjectPool.get("shared-identifier", stringFactory);
        final String secondRetrievedObject = ObjectPool.get("shared-identifier", stringFactory);

        Assertions.assertThat(firstRetrievedObject).isSameAs(secondRetrievedObject);
        Assertions.assertThat(factoryInvocationCount.get()).isEqualTo(1);
    }

    @Test
    void get_DifferentIdentifiers_ReturnDifferentInstances() {
        final Object firstObject = ObjectPool.get("first-identifier", Object::new);
        final Object secondObject = ObjectPool.get("second-identifier", Object::new);

        Assertions.assertThat(firstObject).isNotSameAs(secondObject);
    }

    @Test
    void getAllExistingObjects_ReturnedCollectionIsUnmodifiable() {
        ObjectPool.get("single-key", () -> "value");

        final Collection<String> retrievedObjects = ObjectPool.getAllExistingObjects(String.class);
        Assertions.assertThatThrownBy(() -> retrievedObjects.add("new-value")).isInstanceOf(UnsupportedOperationException.class);
    }
}
