package wtf.ranked.godemiche.result;

import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Factory for creating {@link Result} (synchronous) and {@link ResultAsync} (asynchronous) instances.
 * <p>
 * Use this class to wrap operations that may succeed or fail, providing a unified API
 * for handling results and their associated failure reasons.
 */
@NullMarked
public final class Results {

    private Results() {

    }

    /**
     * Initiates the creation of a synchronous {@link Result} using a builder.
     *
     * @param <T>   The type of the success value.
     * @param value The object to be contained in the successful result.
     * @return A {@link ResultBuilder.Sync} instance to further configure the result.
     */
    public static <T> ResultBuilder.Sync<T> sync(final @Nullable T value) {
        return ResultBuilder.sync(value);
    }

    /**
     * Initiates the creation of an asynchronous {@link ResultAsync} using a builder.
     *
     * @param <T>   The type of the success value.
     * @param value A {@link CompletableFuture} that will eventually hold the success value.
     * @return A {@link ResultBuilder.Async} instance to further configure the asynchronous result.
     */
    public static <T> ResultBuilder.Async<T> async(final CompletableFuture<@Nullable T> value) {
        return ResultBuilder.async(value);
    }

    /**
     * Wraps a successful value into a synchronous {@link Result}.
     *
     * @param <T>   The type of the success value.
     * @param value The value indicating a successful operation.
     * @return A new {@link Result} instance containing the value.
     */
    public static <T> Result<T> ofSuccess(final @Nullable T value) {
        return new Result<>(value);
    }

    /**
     * Creates a failed synchronous {@link Result} with a specific reason.
     *
     * @param <T>    The type of the expected value (can be inferred).
     * @param reason The {@link ResultReason.Failure} object detailing why the operation failed.
     * @return A new failed {@link Result} instance.
     */
    public static <T> Result<T> ofFailure(final ResultReason.Failure reason) {
        return new Result<>(null, reason);
    }

    /**
     * Wraps a {@link CompletableFuture} into a {@link ResultAsync}.
     *
     * <p>This method converts a raw future into a {@link ResultAsync} that resolves
     * once the input future completes successfully.
     *
     * @param <T>   The type of the success value.
     * @param value A {@link CompletableFuture} containing the success value.
     * @return A new {@link ResultAsync} wrapping the completed result.
     */
    public static <T> ResultAsync<T> ofSuccessAsync(final CompletableFuture<T> value) {
        final CompletableFuture<Result<T>> resultFuture = value.thenApply(Results::ofSuccess);
        return new ResultAsync<>(resultFuture);
    }

    /**
     * Creates an immediately failed asynchronous {@link ResultAsync}.
     * <p>
     * The returned {@link ResultAsync} contains a completed future with a null value
     * and the provided failure reason.
     *
     * @param <T>    The type of the expected value.
     * @param reason The {@link ResultReason.Failure} object detailing the failure.
     * @return A new {@link ResultAsync} instance that is already in a failed state.
     */
    public static <T> ResultAsync<T> ofFailureAsync(final ResultReason.Failure reason) {
        return new ResultAsync<>(CompletableFuture.completedFuture(null), reason);
    }
}
