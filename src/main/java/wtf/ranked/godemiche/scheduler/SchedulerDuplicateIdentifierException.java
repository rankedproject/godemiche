package wtf.ranked.godemiche.scheduler;

import org.jspecify.annotations.NullMarked;

/**
 * Exception thrown when attempting to register a scheduled task
 * with an identifier that is already in use.
 */
@NullMarked
public final class SchedulerDuplicateIdentifierException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message exception detail message
     */
    public SchedulerDuplicateIdentifierException(final String message) {
        super(message);
    }
}
