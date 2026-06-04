package wtf.ranked.godemiche.result;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Defines a fluent API for chaining operations based on the state of a {@link Result}.
 * <p>
 * This interface allows for conditional execution of side effects and functional
 * transformation of values, effectively handling the success or failure path of an operation.
 *
 * @param <T> The type of the success value held by the result.
 */
interface ResultChain<T> {

    /**
     * Executes the provided consumer only if the result represents a successful operation.
     *
     * @param successConsumer {@link Consumer} receiving the successful value of type {@code T}.
     * @return The current {@link ResultChain} instance to allow further chaining.
     */
    ResultChain<T> success(Consumer<T> successConsumer);

    /**
     * Executes the provided consumer only if the result represents a failure.
     *
     * @param failureConsumer {@link Consumer} receiving the {@link ResultReason.Failure} object.
     * @return The current {@link ResultChain} instance to allow further chaining.
     */
    ResultChain<T> failure(Consumer<ResultReason.Failure> failureConsumer);

    /**
     * Executes the provided consumer only if the result is a failure that matches
     * the specified {@code reason}.
     *
     * @param reason          The specific {@link ResultReason.Failure} type to match against.
     * @param failureConsumer {@link Consumer} receiving the matching {@link ResultReason.Failure} object.
     * @return The current {@link ResultChain} instance to allow further chaining.
     */
    ResultChain<T> failure(ResultReason.Failure reason, Consumer<ResultReason.Failure> failureConsumer);

    /**
     * Transforms the success value if the operation was successful.
     * <p>
     * If the result is currently a failure, this operation is skipped, and the failure is preserved.
     *
     * @param successFunction {@link Function} that takes the success value {@code T} and returns a new value {@code T}.
     * @return A new {@link ResultChain} instance containing the transformed success value, or the original failure.
     */
    ResultChain<T> mapSuccess(Function<T, T> successFunction);

    /**
     * Transforms a failure reason into a successful value, effectively recovering from a failure.
     * <p>
     * If the result is already a success, this operation is ignored, and the original value is preserved.
     *
     * @param failureFunction {@link Function} that takes the {@link ResultReason.Failure} and returns a valid value of type {@code T}.
     * @return A new {@link ResultChain} instance containing the newly recovered success value.
     */
    ResultChain<T> mapFailure(Function<ResultReason.Failure, T> failureFunction);
}
