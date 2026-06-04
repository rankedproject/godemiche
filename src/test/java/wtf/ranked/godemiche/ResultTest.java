package wtf.ranked.godemiche;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import wtf.ranked.godemiche.result.Result;
import wtf.ranked.godemiche.result.ResultAsync;
import wtf.ranked.godemiche.result.ResultReason;
import wtf.ranked.godemiche.result.Results;

final class ResultTest {

    @Test
    void ofSuccess_Sync_ResultTriggersSuccessConsumer() {
        final AtomicReference<String> consumedValue = new AtomicReference<>();
        Results.ofSuccess("value").success(consumedValue::set);

        Assertions.assertThat(consumedValue.get()).isEqualTo("value");
    }

    @Test
    void ofFailure_Sync_ResultTriggersFailureConsumer() {
        final AtomicReference<ResultReason.Failure> consumedReason = new AtomicReference<>();
        Results.ofFailure(ResultReason.Failure.Default.NOT_PROVIDED_REASON).failure(consumedReason::set);

        Assertions.assertThat(consumedReason.get()).isEqualTo(ResultReason.Failure.Default.NOT_PROVIDED_REASON);
    }

    @Test
    void failure_reasonConsumer_matchingReason_invokesConsumer() {
        final AtomicInteger invocationCount = new AtomicInteger();
        Results.ofFailure(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .failure(ResultReason.Failure.Default.NOT_PROVIDED_REASON, $ -> invocationCount.incrementAndGet());

        Assertions.assertThat(invocationCount.get()).isEqualTo(1);
    }

    @Test
    void failure_reasonConsumer_differentReason_doesNotInvokeConsumer() {
        final AtomicInteger invocationCount = new AtomicInteger();
        Results.ofFailure(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .failure(TestFailureReason.TEST_REASON, $ -> invocationCount.incrementAndGet());

        Assertions.assertThat(invocationCount.get()).isEqualTo(0);
    }

    @Test
    void mapSuccess_Sync_TransformsValue() {
        final String result = Results.ofSuccess("10")
                .mapSuccess($ -> "11")
                .getValue();

        Assertions.assertThat(result).isEqualTo("11");
    }

    @Test
    void mapSuccess_Sync_OnFailure_ReturnsFailureState() {
        final Result<String> result = Results.<String>ofFailure(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .mapSuccess($ -> "11");

        Assertions.assertThat(result.getValue()).isNull();
    }

    @Test
    void mapFailure_Sync_TransformsValue() {
        final String result = Results.<String>ofFailure(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .mapFailure($ -> "11")
                .getValue();

        Assertions.assertThat(result).isEqualTo("11");
    }

    @Test
    void mapFailure_Sync_OnSuccess_ReturnsOriginalValue() {
        final String result = Results.ofSuccess("10")
                .mapFailure($ -> "11")
                .getValue();

        Assertions.assertThat(result).isEqualTo("10");
    }

    @Test
    void async_ofSuccessAsync_ReturnsComputedValue() throws Exception {
        final String result = Results.ofSuccessAsync(CompletableFuture.completedFuture("async-value"))
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isEqualTo("async-value");
    }

    @Test
    void async_ofFailureAsync_ReturnsNullValue() throws Exception {
        final String result = Results.<String>ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isNull();
    }

    @Test
    void async_success_InvokesConsumer() {
        final AtomicReference<String> consumedValue = new AtomicReference<>();
        Results.ofSuccessAsync(CompletableFuture.completedFuture("value")).success(consumedValue::set);

        Awaitility.await()
                .atMost(Duration.ofMillis(100))
                .pollInterval(Duration.ofMillis(10))
                .until(() -> Objects.equals(consumedValue.get(), "value"));

        Assertions.assertThat(consumedValue.get()).isEqualTo("value");
    }

    @Test
    void async_failure_InvokesConsumer() {
        final AtomicReference<ResultReason.Failure> consumedReason = new AtomicReference<>();
        Results.ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .failure(consumedReason::set);

        Awaitility.await()
                .atMost(Duration.ofMillis(100))
                .pollInterval(Duration.ofMillis(10))
                .until(() -> consumedReason.get() != null);

        Assertions.assertThat(consumedReason.get()).isEqualTo(ResultReason.Failure.Default.NOT_PROVIDED_REASON);
    }

    @Test
    void async_failure_reasonConsumer_matchingReason_invokesConsumer() {
        final AtomicInteger invocationCount = new AtomicInteger();
        Results.ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .failure(ResultReason.Failure.Default.NOT_PROVIDED_REASON, $ -> invocationCount.incrementAndGet());

        Awaitility.await()
                .atMost(Duration.ofMillis(100))
                .pollInterval(Duration.ofMillis(10))
                .until(() -> invocationCount.get() == 1);

        Assertions.assertThat(invocationCount.get()).isEqualTo(1);
    }

    @Test
    void async_failure_reasonConsumer_differentReason_doesNotInvokeConsumer() {
        final AtomicInteger invocationCount = new AtomicInteger();
        Results.ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .failure(TestFailureReason.TEST_REASON, $ -> invocationCount.incrementAndGet());

        Awaitility.await()
                .atMost(Duration.ofMillis(100))
                .pollInterval(Duration.ofMillis(10))
                .until(() -> invocationCount.get() == 0);

        Assertions.assertThat(invocationCount.get()).isEqualTo(0);
    }

    @Test
    void async_mapSuccess_TransformsValue() throws Exception {
        final String result = Results.ofSuccessAsync(CompletableFuture.completedFuture("5"))
                .mapSuccess(value -> "value-" + value)
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isEqualTo("value-5");
    }

    @Test
    void async_mapFailure_TransformsValue() throws Exception {
        final String result = Results.<String>ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .mapFailure($ -> "value-5")
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isEqualTo("value-5");
    }

    @Test
    void async_mapFailure_OnSuccess_ReturnsOriginalValue() throws Exception {
        final String result = Results.ofSuccessAsync(CompletableFuture.completedFuture("value"))
                .mapFailure($ -> "other")
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isEqualTo("value");
    }

    @Test
    void builder_sync_successIfCondition_MarksAsSuccess() {
        final Result<String> result = Results.sync("ok")
                .successIf(value -> Objects.equals(value, "ok"))
                .build();

        Assertions.assertThat(result.isFailure()).isFalse();
        Assertions.assertThat(result.getValue()).isEqualTo("ok");
    }

    @Test
    void builder_sync_failIfCondition_MarksAsFailure() {
        final Result<String> result = Results.sync("bad")
                .failIf(value -> Objects.equals(value, "bad"), ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .build();

        Assertions.assertThat(result.isFailure()).isTrue();
    }

    @Test
    void builder_sync_noPredicates_returnsSuccess() {
        final Result<String> result = Results.sync("value").build();

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getValue()).isEqualTo("value");
    }

    @Test
    void builder_async_buildAndExecute_successPath() throws Exception {
        final ResultAsync<String> result = Results.async(CompletableFuture.completedFuture("hello"))
                .successIf(value -> Objects.equals(value, "hello"))
                .build();

        final String value = result.getAsFuture().get();
        Assertions.assertThat(value).isEqualTo("hello");
    }

    @Test
    void builder_async_buildAndExecute_failurePath() throws Exception {
        final ResultAsync<String> result = Results.async(CompletableFuture.completedFuture("bad"))
                .failIf(value -> Objects.equals(value, "bad"), ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .build();

        final boolean isFailed = result.isFailure().get();
        Assertions.assertThat(isFailed).isTrue();
    }

    @Test
    void flatMapSuccess_whenValuePresent_TransformsValue() throws Exception {
        final String result = Results.ofSuccessAsync(CompletableFuture.completedFuture("string-1"))
                .flatMapSuccess(value -> CompletableFuture.completedFuture(value + "string-2"))
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isEqualTo("string-1string-2");
    }

    @Test
    void flatMapSuccess_whenFailure_ReturnsNullValue() throws Exception {
        final String result = Results.<String>ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .flatMapSuccess(value -> CompletableFuture.completedFuture(value + "string-2"))
                .getAsFuture()
                .get();

        Assertions.assertThat(result).isNull();
    }

    @Test
    void flatMapFailure_whenFailure_ReturnsNewValue() throws Exception {
        final String value = Results.<String>ofFailureAsync(ResultReason.Failure.Default.NOT_PROVIDED_REASON)
                .flatMapFailure($ -> CompletableFuture.completedFuture("recovered"))
                .getAsFuture()
                .get();

        Assertions.assertThat(value).isEqualTo("recovered");
    }

    @Test
    void flatMapFailure_whenSuccess_DoesntTransformsValue() throws Exception {
        final String value = Results.ofSuccessAsync(CompletableFuture.completedFuture("value"))
                .flatMapFailure($ -> CompletableFuture.completedFuture("recovered"))
                .getAsFuture()
                .get();

        Assertions.assertThat(value).isEqualTo("value");
    }

    private enum TestFailureReason implements ResultReason.Failure {
        TEST_REASON
    }
}
