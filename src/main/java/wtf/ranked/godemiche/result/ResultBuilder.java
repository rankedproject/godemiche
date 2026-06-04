package wtf.ranked.godemiche.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A builder for constructing {@link Result} or {@link ResultAsync} instances with conditional logic.
 * <p>
 * This builder enables the validation of values against {@code successIf} or {@code failIf}
 * predicates to determine the final state (Success or Failure) of the resulting object.
 *
 * @param <O> The type of the value being validated.
 * @param <T> The container type of the value (e.g., raw {@code O} or {@code CompletableFuture<O>}).
 * @param <R> The type of the resulting {@link ResultChain} implementation.
 */
public abstract sealed class ResultBuilder<O, T, R extends ResultChain<O>> permits ResultBuilder.Async, ResultBuilder.Sync {

    private final T value;
    private final List<SuccessPredicate<O>> successPredicates;
    private final List<FailurePredicate<O>> failurePredicates;

    /**
     * Initializes a new result builder.
     *
     * @param value             the initial value
     * @param successPredicates list of success conditions
     * @param failurePredicates list of failure conditions
     */
    ResultBuilder(
            final T value,
            final List<SuccessPredicate<O>> successPredicates,
            final List<FailurePredicate<O>> failurePredicates
    ) {
        this.value = value;
        this.successPredicates = successPredicates;
        this.failurePredicates = failurePredicates;
    }

    /**
     * Factory for an asynchronous builder.
     *
     * @param value The {@link CompletableFuture} to monitor.
     * @param <O>   The inner value type.
     * @return A new {@link Async} builder.
     */
    protected static <O> Async<O> async(final CompletableFuture<@Nullable O> value) {
        return new Async<>(value);
    }

    /**
     * Factory for a synchronous builder.
     *
     * @param value The value to validate.
     * @param <O>   The value type.
     * @return A new {@link Sync} builder.
     */
    protected static <O> Sync<O> sync(final O value) {
        return new Sync<>(value);
    }

    /**
     * Defines a condition that, if met, marks the result as a Success.
     *
     * @param predicate A {@link Predicate} to evaluate the value.
     * @return This builder instance for chaining.
     */
    public ResultBuilder<O, T, R> successIf(final Predicate<@Nullable O> predicate) {
        successPredicates.add(SuccessPredicate.of(predicate));
        return this;
    }

    /**
     * Defines a condition that, if met, marks the result as a Failure with the specified reason.
     *
     * @param predicate A {@link Predicate} to evaluate the value.
     * @param reason    The {@link ResultReason.Failure} to assign if the predicate returns {@code true}.
     * @return This builder instance for chaining.
     */
    public ResultBuilder<O, T, R> failIf(final Predicate<@Nullable O> predicate, final ResultReason.Failure reason) {
        failurePredicates.add(FailurePredicate.of(predicate, reason));
        return this;
    }

    /**
     * Validates the value against defined predicates and constructs the final {@link ResultChain}.
     *
     * @return A constructed {@link Result} or {@link ResultAsync} instance.
     */
    public abstract R build();

    /**
     * Retrieves the value currently held by this builder.
     *
     * @return The value container {@code T}.
     */
    protected T getValue() {
        return value;
    }

    /**
     * Retrieves the list of success predicates.
     *
     * @return An unmodifiable view of the success predicates.
     */
    @UnmodifiableView
    List<SuccessPredicate<O>> getSuccessPredicates() {
        return Collections.unmodifiableList(successPredicates);
    }

    /**
     * Retrieves the list of failure predicates.
     *
     * @return An unmodifiable view of the failure predicates.
     */
    @UnmodifiableView
    List<FailurePredicate<O>> getFailurePredicates() {
        return Collections.unmodifiableList(failurePredicates);
    }

    /**
     * {@link ResultBuilder} implementation for asynchronous operations.
     *
     * @param <O> The value type.
     */
    @NullMarked
    public static non-sealed class Async<O> extends ResultBuilder<O, CompletableFuture<@Nullable O>, ResultAsync<O>> {

        /**
         * Creates an async builder.
         *
         * @param value the future value.
         *
         */
        protected Async(final CompletableFuture<@Nullable O> value) {
            super(value, new ArrayList<>(), new ArrayList<>());
        }

        /**
         * Asynchronously evaluates the value once the future completes.
         *
         * @return A {@link ResultAsync} representing the eventual outcome.
         */
        @Override
        public ResultAsync<O> build() {
            final CompletableFuture<Result<O>> resultFuture = getValue().thenApply(this::newResult);
            return new ResultAsync<>(resultFuture);
        }

        private Result<O> newResult(final @Nullable O value) {
            return new Sync<>(value, getSuccessPredicates(), getFailurePredicates()).build();
        }
    }

    /**
     * {@link ResultBuilder} implementation for synchronous operations.
     *
     * @param <O> The value type.
     */
    @NullMarked
    public static non-sealed class Sync<O> extends ResultBuilder<O, O, Result<O>> {

        /**
         * Creates a sync builder.
         *
         * @param value the value.
         *
         */
        protected Sync(final O value) {
            super(value, new ArrayList<>(), new ArrayList<>());
        }

        /**
         * Constructs a synchronous builder with pre-defined predicates.
         *
         * @param value             the initial value
         * @param successPredicates the list of success conditions
         * @param failurePredicates the list of failure conditions
         */
        Sync(
                final @Nullable O value,
                final List<SuccessPredicate<O>> successPredicates,
                final List<FailurePredicate<O>> failurePredicates
        ) {
            super(value, successPredicates, failurePredicates);
        }

        /**
         * Validates the value synchronously. Checks {@code successIf} first, then {@code failIf}.
         *
         * @return A {@link Result} instance indicating Success or Failure.
         */
        @Override
        public Result<O> build() {
            final boolean isSuccess = getSuccessPredicates().stream()
                    .map(SuccessPredicate::predicate)
                    .anyMatch(predicate -> predicate.test(getValue()));

            if (isSuccess) {
                return new Result<>(getValue());
            }

            final FailurePredicate<O> failurePredicate = getFailurePredicates().stream()
                    .filter(predicate -> predicate.predicate().test(getValue()))
                    .findFirst()
                    .orElse(null);

            if (failurePredicate != null) {
                return new Result<>(getValue(), failurePredicate.reason());
            }

            return new Result<>(getValue());
        }
    }

    /**
     * A predicate that triggers a specific failure.
     *
     * @param <T>       the value type
     * @param predicate the condition
     * @param reason    the failure reason
     */
    @NullMarked
    record FailurePredicate<T>(
            Predicate<@Nullable T> predicate,
            ResultReason.Failure reason
    ) {

        static <T> FailurePredicate<T> of(final Predicate<@Nullable T> predicate, final ResultReason.Failure reason) {
            return new FailurePredicate<>(predicate, reason);
        }
    }

    /**
     * A predicate that defines success.
     *
     * @param <T>       the value type
     * @param predicate the condition
     */
    @NullMarked
    record SuccessPredicate<T>(
            Predicate<@Nullable T> predicate
    ) {

        static <T> SuccessPredicate<T> of(final Predicate<@Nullable T> predicate) {
            return new SuccessPredicate<>(predicate);
        }
    }
}

