package wtf.ranked.godemiche.result;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An asynchronous implementation of {@link ResultChain} wrapping a {@link CompletableFuture}.
 * <p>
 * This class manages the lifecycle of an asynchronous operation's outcome. All transformation
 * and chaining operations are performed asynchronously when the underlying {@link CompletableFuture}
 * completes.
 *
 * @param <T> The type of the success value.
 */
@NullMarked
public final class ResultAsync<T> implements ResultChain<T> {

    private final CompletableFuture<Result<T>> futureResult;

    /**
     * Creates an instance from an existing {@link CompletableFuture} that resolves to a {@link Result}.
     *
     * @param futureResult The future containing the result state (Success or Failure).
     */
    public ResultAsync(final CompletableFuture<Result<T>> futureResult) {
        this.futureResult = futureResult;
    }

    /**
     * Creates an instance from a value future and a specific failure reason.
     *
     * @param futureResult The future containing the success value.
     * @param reason       The reason why the operation failed.
     */
    public ResultAsync(final CompletableFuture<T> futureResult, final ResultReason.Failure reason) {
        this.futureResult = futureResult.thenApply($ -> Results.ofFailure(reason));
    }

    /**
     * Schedules an action to be performed if the result resolves to Success.
     *
     * @param successConsumer {@link Consumer} receiving the success value.
     * @return The current {@code ResultAsync} instance for chaining.
     */
    @Override
    public ResultAsync<T> success(final Consumer<T> successConsumer) {
        final CompletableFuture<Result<T>> newFuture = futureResult.thenApply(result -> result.success(successConsumer));
        return new ResultAsync<>(newFuture);
    }

    /**
     * Schedules an action to be performed if the result resolves to Failure.
     *
     * @param failureConsumer {@link Consumer} receiving the {@link ResultReason.Failure}.
     * @return The current {@code ResultAsync} instance for chaining.
     */
    @Override
    public ResultAsync<T> failure(final Consumer<ResultReason.Failure> failureConsumer) {
        final CompletableFuture<Result<T>> newFuture = futureResult.thenApply(result -> result.failure(failureConsumer));
        return new ResultAsync<>(newFuture);
    }

    /**
     * Schedules an action to be performed if the result matches a specific failure reason.
     *
     * @param reason          The specific {@link ResultReason.Failure} to check.
     * @param failureConsumer {@link Consumer} receiving the {@link ResultReason.Failure}.
     * @return The current {@code ResultAsync} instance for chaining.
     */
    @Override
    public ResultAsync<T> failure(final ResultReason.Failure reason, final Consumer<ResultReason.Failure> failureConsumer) {
        final CompletableFuture<Result<T>> newFuture = futureResult.thenApply(result -> result.failure(reason, failureConsumer));
        return new ResultAsync<>(newFuture);
    }

    /**
     * Transforms the success value asynchronously.
     *
     * @param successFunction {@link Function} mapping the current success value to a new value.
     * @return A new {@code ResultAsync} with the transformed value or original failure.
     */
    @Override
    public ResultAsync<T> mapSuccess(final Function<T, T> successFunction) {
        final var mappedFuture = futureResult.thenApply(result -> result.mapSuccess(successFunction));
        return new ResultAsync<>(mappedFuture);
    }

    /**
     * Recovers from a failure asynchronously by transforming the failure into a success value.
     *
     * @param failureFunction {@link Function} mapping the {@link ResultReason.Failure} to a success value.
     * @return A new {@code ResultAsync} instance containing the recovered success value.
     */
    @Override
    public ResultAsync<T> mapFailure(final Function<ResultReason.Failure, T> failureFunction) {
        final var mappedFuture = futureResult.thenApply(result -> result.mapFailure(failureFunction));
        return new ResultAsync<>(mappedFuture);
    }

    /**
     * Chains a new asynchronous operation that depends on the current success value.
     *
     * @param successFunction A {@link Function} returning a {@link CompletableFuture} of the new type.
     * @param <U>             The type of the new result.
     * @return A new {@code ResultAsync} wrapping the resulting asynchronous chain.
     */
    @SuppressWarnings("unchecked")
    public <U> ResultAsync<U> flatMapSuccess(final Function<T, CompletableFuture<U>> successFunction) {
        final CompletableFuture<Result<U>> resultFuture = futureResult.thenCompose(result -> {
            if (result.getValue() == null) {
                return CompletableFuture.completedFuture((Result<U>) result);
            }

            return successFunction.apply(result.getValue()).thenApply(Results::ofSuccess);
        });

        return new ResultAsync<>(resultFuture);
    }

    /**
     * Recovers from a failure by chaining a new asynchronous operation.
     *
     * @param failureFunction A {@link Function} returning a {@link CompletableFuture} to recover the failure.
     * @param <U>             The type of the new result.
     * @return A new {@code ResultAsync} wrapping the resulting recovery chain.
     */
    @SuppressWarnings("unchecked")
    public <U> ResultAsync<U> flatMapFailure(final Function<ResultReason.Failure, CompletableFuture<U>> failureFunction) {
        final CompletableFuture<Result<U>> resultFuture = futureResult.thenCompose(result -> {
            if (result.getValue() == null) {
                return failureFunction.apply((ResultReason.Failure) result.getReason()).thenApply(Results::ofSuccess);
            }

            return CompletableFuture.completedFuture((Result<U>) result);
        });

        return new ResultAsync<>(resultFuture);
    }

    /**
     * Exposes the internal value as a {@link CompletableFuture}.
     *
     * @return A future that completes with the success value or {@code null} if failed.
     */
    public CompletableFuture<@Nullable T> getAsFuture() {
        return futureResult.thenApply(Result::getValue);
    }

    /**
     * Checks the failure state asynchronously.
     *
     * @return A future that completes with {@code true} if the result is a failure.
     */
    public CompletableFuture<Boolean> isFailure() {
        return futureResult.thenApply(Result::isFailure);
    }

    /**
     * Checks the success state asynchronously.
     *
     * @return A future that completes with {@code true} if the result is a success.
     */
    public CompletableFuture<Boolean> isSuccess() {
        return futureResult.thenApply(Result::isSuccess);
    }
}
