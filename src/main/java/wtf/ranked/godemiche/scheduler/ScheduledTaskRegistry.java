package wtf.ranked.godemiche.scheduler;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;

/**
 * Registry managing scheduled task lifecycles.
 *
 * <p>Tracks active scheduled futures by task identifier, and provides start/cancel operations.
 */
@NullMarked
public final class ScheduledTaskRegistry {

    private static final ScheduledTaskRegistry COMMON_TASK_REGISTRY = new ScheduledTaskRegistry();

    private final Map<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();

    private ScheduledTaskRegistry() {

    }

    /**
     * Creates a new independent registry instance.
     *
     * @return new registry
     */
    public static ScheduledTaskRegistry create() {
        return new ScheduledTaskRegistry();
    }

    /**
     * Returns the shared registry instance.
     *
     * @return common registry
     */
    public static ScheduledTaskRegistry common() {
        return COMMON_TASK_REGISTRY;
    }

    /**
     * Starts scheduling the given task.
     *
     * <p>Schedules the task at a fixed rate using {@link ScheduledTask#options()} delay and repeat values,
     * and stores the resulting {@link ScheduledFuture} under the task identifier.
     *
     * @param task task to schedule
     */
    public void start(final ScheduledTask task) {
        final ScheduledTask.ScheduledTaskOptions options = task.options();
        final String identifier = options.identifier();

        if (this.activeTasks.containsKey(identifier)) {
            throw new SchedulerDuplicateIdentifierException("You can't schedule a task with the same identifier");
        }

        final RunningTask runningTask = RunningTask.of(identifier, this);
        final Duration delay = options.delay();
        final Duration repeat = options.repeat();

        final ScheduledExecutorService scheduledExecutor = options.scheduledExecutorService();
        final ScheduledFuture<?> scheduledFuture;

        final Runnable taskRunnable = () -> task.run(runningTask);
        if (repeat == null) {
            scheduledFuture = scheduledExecutor.schedule(taskRunnable, delay.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            scheduledFuture = scheduledExecutor.scheduleAtFixedRate(
                    taskRunnable,
                    delay.toMillis(),
                    repeat.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }

        this.activeTasks.put(identifier, scheduledFuture);
    }

    /**
     * Cancels a running task by identifier.
     *
     * @param taskIdentifier identifier sync the task to cancel
     * @throws NullPointerException if no task is running for the given identifier
     */
    public void cancel(final String taskIdentifier) {
        final ScheduledFuture<?> scheduledFuture = this.activeTasks.remove(taskIdentifier);
        scheduledFuture.cancel(true);
    }

    /**
     * Cancels a running task.
     *
     * @param task task to cancel
     */
    public void cancel(final ScheduledTask task) {
        cancel(task.options().identifier());
    }

    /**
     * Cancels all running tasks in this registry.
     */
    public void cancelAll() {
        final Iterator<ScheduledFuture<?>> iterator = this.activeTasks.values().iterator();
        while (iterator.hasNext()) {
            final ScheduledFuture<?> scheduledFuture = iterator.next();
            scheduledFuture.cancel(true);
            iterator.remove();
        }
    }

    /**
     * Returns whether a task with the given id is currently running.
     *
     * @param taskId task identifier
     * @return {@code true} if the task is scheduled and tracked by this registry
     */
    public boolean isTaskRunning(final String taskId) {
        return this.activeTasks.containsKey(taskId);
    }
}
