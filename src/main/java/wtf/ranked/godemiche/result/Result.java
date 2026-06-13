package wtf.ranked.godemiche.result;

import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A synchronous container representing the outcome of an operation: either a success value or a failure reason.
 * Implements a functional monad pattern to handle results without branching (if-else).
 *
 * <p>All operations are <b>synchronous</b> and execute on the <b>caller's thread</b>.
 *
 * @param <T> The type of the success value.
 */
@NullMarked
public final class Result<T> implements ResultChain<T> {

    private final @Nullable T value;
    private final ResultReason reason;

    /**
     * Constructs a successful {@link Result} with the provided value.
     *
     * @param value The success value. Must not be null.
     */
    public Result(final @Nullable T value) {
        this.reason = ResultReason.Default.SUCCEED;
        this.value = value;
    }

    /**
     * Constructs a failed {@link Result} with an optional value and the specified failure reason.
     *
     * @param value  The associated value (often null in failure cases).
     * @param reason The {@link ResultReason.Failure} describing why the operation failed.
     */
    public Result(final @Nullable T value, final ResultReason.Failure reason) {
        this.reason = reason;
        this.value = value;
    }

    /**
     * Executes the {@code successConsumer} only if the result is a success (value is non-null).
     * Runs synchronously on the calling thread.
     */
    @Override
    public Result<T> success(final Consumer<T> successConsumer) {
        if (value == null || reason != ResultReason.Default.SUCCEED) {
            return this;
        }

        successConsumer.accept(value);
        return this;
    }

    /**
     * Executes the {@code failureConsumer} only if the result is a failure.
     * Runs synchronously on the calling thread.
     */
    @Override
    public Result<T> failure(final Consumer<ResultReason.Failure> failureConsumer) {
        if (value != null || reason == ResultReason.Default.SUCCEED) {
            return this;
        }

        failureConsumer.accept((ResultReason.Failure) reason);
        return this;
    }

    /**
     * Executes the provided action if the result is a failure matching the specified reason.
     *
     * @param reason          the failure reason to match
     * @param failureConsumer the action to execute
     * @return this instance for method chaining
     */
    @Override
    public Result<T> failure(final ResultReason.Failure reason, final Consumer<ResultReason.Failure> failureConsumer) {
        if (this.reason == reason) {
            failureConsumer.accept(reason);
        }

        return this;
    }

    /**
     * Transforms the success value into a new value if the current result is successful.
     *
     * @param successFunction the transformation function
     * @return a new Result containing the transformed value, or the original error
     */
    @Override
    public Result<T> mapSuccess(final Function<T, T> successFunction) {
        if (reason instanceof ResultReason.Failure failure) {
            return new Result<>(null, failure);
        }

        if (value == null) {
            return new Result<>(null, ResultReason.Failure.Default.NOT_PROVIDED_REASON);
        }

        final T value = successFunction.apply(this.value);
        return new Result<>(value);
    }

    /**
     * Recovers from a failure by transforming the reason into a success value.
     *
     * @param failureFunction the recovery function mapping reason to value
     * @return a new successful Result, or this instance if already successful
     */
    @Override
    public Result<T> mapFailure(final Function<ResultReason.Failure, T> failureFunction) {
        if (reason == ResultReason.Default.SUCCEED) {
            return this;
        }

        final T newValue = failureFunction.apply((ResultReason.Failure) reason);
        return new Result<>(newValue);
    }

    /**
     * Returns the contained value if present.
     *
     * @return the value, or null
     */
    public @Nullable T getValue() {
        return value;
    }

    /**
     * Returns the reason associated with this result.
     *
     * @return the result reason
     */
    public ResultReason getReason() {
        return reason;
    }

    /**
     * Checks if this result represents a failure.
     *
     * @return true if failure, false otherwise
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Checks if this result represents a success.
     *
     * @return true if success, false otherwise
     */
    public boolean isSuccess() {
        return getReason() == ResultReason.Default.SUCCEED;
    }
}
