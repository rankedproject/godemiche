package wtf.ranked.godemiche.scheduler;

import org.jspecify.annotations.NullMarked;

/**
 * Handle for a currently scheduled task execution.
 *
 * <p>Provides access to the owning task id and registry-backed cancellation.
 * Instances are created by {@link ScheduledTaskRegistry} and passed to
 * {@link ScheduledTask#run(RunningTask)}.
 */
@NullMarked
public final class RunningTask {

    private final String identifier;
    private final ScheduledTaskRegistry taskRegistry;

    private RunningTask(final String identifier, final ScheduledTaskRegistry registry) {
        this.identifier = identifier;
        this.taskRegistry = registry;
    }

    /**
     * Creates a new {@link RunningTask} handle.
     *
     * @param identifier           task identifier
     * @param taskRegistry registry managing the task lifecycle
     * @return new running task handle for a scheduled task
     */
    public static RunningTask of(final String identifier, final ScheduledTaskRegistry taskRegistry) {
        return new RunningTask(identifier, taskRegistry);
    }

    /**
     * Cancels the task in the owning registry.
     *
     * <p>Delegates to {@link ScheduledTaskRegistry#cancel(String)} using this task id.
     */
    public void cancel() {
        this.taskRegistry.cancel(identifier);
    }
}
