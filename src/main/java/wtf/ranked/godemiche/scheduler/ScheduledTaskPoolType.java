package wtf.ranked.godemiche.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jspecify.annotations.NullMarked;
import wtf.ranked.godemiche.util.ObjectPool;
import wtf.ranked.godemiche.util.VirtualThreadPerTaskScheduledExecutorService;

/**
 * Predefined {@link ScheduledTaskPool} implementations.
 *
 * <p>Each enum constant owns its underlying executor(s) and defines how scheduled
 * commands are executed (single shared thread, platform thread pool, or virtual threads).
 */
@NullMarked
public enum ScheduledTaskPoolType implements ScheduledTaskPool {

    /**
     * Schedules all tasks on a single shared scheduler thread.
     *
     * <p>All tasks share one {@link ScheduledExecutorService}. Long-running tasks
     * will delay execution sync other tasks.
     */
    SINGLE_THREAD_FOR_ALL_TASKS {

        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return ObjectPool.get("single_thread_scheduled_task", Executors::newSingleThreadScheduledExecutor);
        }
    },

    /**
     * Triggers scheduling on a single scheduler thread, but executes work on a new virtual thread per run.
     *
     * <p>The {@link ScheduledExecutorService} is used only for timing. Each tick submits
     * the command to a virtual-thread executor.
     */
    NEW_VIRTUAL_THREAD {

        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return ObjectPool.get("virtual_thread_scheduled_task", VirtualThreadPerTaskScheduledExecutorService::new);
        }
    },

    /**
     * Schedules and executes tasks using a platform-thread scheduled pool.
     *
     * <p>Uses a fixed-size {@link ScheduledExecutorService} sized to half the available processors (minimum 1).
     */
    NEW_PLATFORM_THREAD {

        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return ObjectPool.get("new_thread_executor_scheduled_pool", () -> Executors.newScheduledThreadPool(
                    Constants.CORE_POOL_SIZE,
                    Executors.defaultThreadFactory()
            ));
        }
    };

    /**
     * Internal constants for pool sizing.
     */
    private static final class Constants {

        /**
         * Core pool size for {@link #NEW_PLATFORM_THREAD}.
         */
        private static final int CORE_POOL_SIZE = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
    }
}
