package wtf.ranked.godemiche.scheduler;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Unit sync work executed by {@link ScheduledTaskRegistry} on a fixed schedule.
 *
 * <p>A task provides scheduling metadata via {@link #options()} and the work to execute via {@link #run(RunningTask)}.
 * Tasks are started by calling {@link ScheduledTaskRegistry#start(ScheduledTask)}.
 */
@NullMarked
public interface ScheduledTask {

    /**
     * Returns the task configuration.
     *
     * <p>Contains task id, initial delay, repeat interval and target executor pool.
     * The {@link ScheduledTaskRegistry} uses these values when scheduling the task.
     *
     * @return task options
     */
    ScheduledTaskOptions options();

    /**
     * Executes the scheduled task.
     *
     * <p>Invoked by {@link ScheduledTaskRegistry} using the scheduling configuration defined in {@link #options()}.
     * The provided {@link RunningTask} can be used to cancel further executions via the registry.
     *
     * @param runningTask currently executing task handle
     */
    void run(RunningTask runningTask);

    /**
     * Create a new {@link ScheduledTaskImpl.Builder}.
     *
     * @return new builder instance
     */
    static ScheduledTaskImpl.Builder builder() {
        return ScheduledTaskImpl.builder();
    }

    /**
     * Immutable data holder describing scheduled task configuration.
     *
     * <p>Contains task id, initial delay, repeat interval and scheduler pool.
     *
     * @param identifier               task identifier used by {@link ScheduledTaskRegistry}
     * @param delay                    initial delay before first execution
     * @param repeat                   interval between executions
     * @param scheduledExecutorService scheduled executor used to execute the task
     */
    record ScheduledTaskOptions(
            String identifier,
            Duration delay,
            @Nullable Duration repeat,
            ScheduledExecutorService scheduledExecutorService
    ) {

        private static final String EXECUTOR_IS_NECESSARY = "Providing SchedulerPool is necessary";

        /**
         * Create a new {@link Builder}.
         *
         * @return new builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder for {@link ScheduledTaskOptions}.
         *
         * <p>Allows configuring id, delay, repeat interval and executor pool
         * before building an immutable options instance.
         */
        public static final class Builder {

            private Builder() {

            }

            private @Nullable String identifier;
            private @Nullable Duration repeat;
            private Duration delay = Duration.ofMillis(0);
            private @Nullable ScheduledExecutorService scheduledExecutorService;

            /**
             * Returns builder with the task identifier set.
             *
             * @param identifier task identifier
             * @return Builder with identifier set
             */
            public Builder identifier(final String identifier) {
                this.identifier = identifier;
                return this;
            }

            /**
             * Generates a random task identifier.
             *
             * @return builder instance
             */
            public Builder randomIdentifier() {
                this.identifier = UUID.randomUUID().toString();
                return this;
            }

            /**
             * Returns builder with the initial delay set.
             *
             * @param delay duration to wait before first execution
             * @return Builder with delay set
             */
            public Builder delay(final Duration delay) {
                this.delay = delay;
                return this;
            }

            /**
             * Returns builder with the repeat interval set.
             *
             * @param repeat duration between executions
             * @return Builder with repeat set
             */
            public Builder repeat(final @Nullable Duration repeat) {
                this.repeat = repeat;
                return this;
            }

            /**
             * Returns builder with the scheduler pool set.
             *
             * @param schedulerPool pool uses to execute the task
             * @return Builder with pool set
             */
            public Builder schedulerPool(final ScheduledTaskPool schedulerPool) {
                this.scheduledExecutorService = schedulerPool.getScheduledExecutorService();
                return this;
            }

            /**
             *
             * Returns builder with the executor service set.
             *
             * @param scheduledExecutorService uses to execute the task
             * @return Builder with pool set
             */
            public Builder schedulerPool(final ScheduledExecutorService scheduledExecutorService) {
                this.scheduledExecutorService = scheduledExecutorService;
                return this;
            }

            /**
             * Builds immutable {@link ScheduledTaskOptions}.
             *
             * @return new options instance
             * @throws NullPointerException if scheduler pool is not set
             */
            public ScheduledTaskOptions build() {
                Preconditions.checkNotNull(identifier, "scheduled task can't have no identifier");
                Preconditions.checkNotNull(scheduledExecutorService, EXECUTOR_IS_NECESSARY);

                return new ScheduledTaskOptions(identifier, delay, repeat, scheduledExecutorService);
            }
        }
    }

    /**
     * Immutable data holder describing scheduled task configuration.
     *
     * <p>Contains task id, initial delay, repeat interval, scheduler pool, and the action to run.
     *
     * @param identifier               task identifier used by {@link ScheduledTaskRegistry}
     * @param delay                    initial delay before first execution
     * @param repeat                   interval between executions
     * @param scheduledExecutorService scheduled executor used to execute the task
     * @param runningTaskAction        the action or logic to be executed by the scheduled task
     */
    record ScheduledTaskImpl(
            String identifier,
            Duration delay,
            @Nullable Duration repeat,
            ScheduledExecutorService scheduledExecutorService,
            Consumer<RunningTask> runningTaskAction
    ) implements ScheduledTask {

        /**
         * Creates a new builder for constructing {@link ScheduledTask} instances.
         *
         * @return new builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ScheduledTaskOptions options() {
            return ScheduledTaskOptions.builder()
                    .identifier(identifier)
                    .delay(delay)
                    .repeat(repeat)
                    .schedulerPool(scheduledExecutorService)
                    .build();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run(final RunningTask runningTask) {
            this.runningTaskAction.accept(runningTask);
        }

        /**
         * Builder for {@link ScheduledTaskOptions}.
         *
         * <p>Allows configuring id, delay, repeat interval and executor pool
         * before building an immutable options instance.
         */
        public static final class Builder {

            private String identifier = UUID.randomUUID().toString();
            private @Nullable Duration repeat;
            private Duration delay = Duration.ofMillis(0);
            private @Nullable ScheduledExecutorService scheduledExecutorService;
            private @Nullable Consumer<RunningTask> runningTaskAction;

            private Builder() {

            }

            /**
             * Returns builder with the task identifier set.
             *
             * @param identifier task identifier
             * @return Builder with identifier set
             */
            public Builder identifier(final String identifier) {
                this.identifier = identifier;
                return this;
            }

            /**
             * Returns builder with the initial delay set.
             *
             * @param delay duration to wait before first execution
             * @return Builder with delay set
             */
            public Builder delay(final Duration delay) {
                this.delay = delay;
                return this;
            }

            /**
             * Returns builder with the repeat interval set.
             *
             * @param repeat duration between executions
             * @return Builder with repeat set
             */
            public Builder repeat(final Duration repeat) {
                this.repeat = repeat;
                return this;
            }

            /**
             * Returns builder with the scheduler pool set.
             *
             * @param schedulerPool pool uses to execute the task
             * @return Builder with pool set
             */
            public Builder schedulerPool(final ScheduledTaskPool schedulerPool) {
                this.scheduledExecutorService = schedulerPool.getScheduledExecutorService();
                return this;
            }

            /**
             *
             * Returns builder with the executor service set.
             *
             * @param scheduledExecutorService uses to execute the task
             * @return Builder with pool set
             */
            public Builder schedulerPool(final ScheduledExecutorService scheduledExecutorService) {
                this.scheduledExecutorService = scheduledExecutorService;
                return this;
            }

            /**
             * Returns builder with the run action set.
             *
             * @param runningTaskAction action invoked for each execution
             * @return Builder with run action set
             */
            public Builder run(final Consumer<RunningTask> runningTaskAction) {
                this.runningTaskAction = runningTaskAction;
                return this;
            }

            /**
             * Builds immutable {@link ScheduledTask}.
             *
             * @return new task instance
             * @throws NullPointerException if run action or scheduler pool is not set
             */
            public ScheduledTask build() {
                Preconditions.checkNotNull(identifier, "scheduled task can't have no identifier");
                Preconditions.checkNotNull(runningTaskAction, "run action is not specified for task " + identifier);
                Preconditions.checkNotNull(scheduledExecutorService, ScheduledTaskOptions.EXECUTOR_IS_NECESSARY);

                return new ScheduledTaskImpl(identifier, delay, repeat, scheduledExecutorService, runningTaskAction);
            }
        }
    }
}
