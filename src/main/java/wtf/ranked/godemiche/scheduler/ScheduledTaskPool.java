package wtf.ranked.godemiche.scheduler;

import java.util.concurrent.ScheduledExecutorService;
import org.jspecify.annotations.NullMarked;

/**
 * Task execution pool.
 *
 * <p>Provides the {@link ScheduledExecutorService} used by {@link ScheduledTaskRegistry}
 * to schedule and execute tasks.
 */
@NullMarked
public interface ScheduledTaskPool {

    /**
     * Executor service used for scheduling tasks.
     *
     * @return scheduled executor service
     */
    ScheduledExecutorService getScheduledExecutorService();
}
