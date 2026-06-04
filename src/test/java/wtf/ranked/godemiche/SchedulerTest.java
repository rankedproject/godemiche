package wtf.ranked.godemiche;

import wtf.ranked.godemiche.scheduler.ScheduledTask;
import wtf.ranked.godemiche.scheduler.ScheduledTaskPoolType;
import wtf.ranked.godemiche.scheduler.ScheduledTaskRegistry;
import wtf.ranked.godemiche.scheduler.SchedulerDuplicateIdentifierException;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

final class SchedulerTest {

    private final ScheduledTaskRegistry taskRegistry = ScheduledTaskRegistry.create();

    @Test
    void cancel_RepeatingTaskDuringRunning_CanceledSuccessfully() {
        final AtomicInteger counter = new AtomicInteger();
        final ScheduledTask repeatingTask = ScheduledTask.builder()
                .identifier("repeating-task")
                .schedulerPool(ScheduledTaskPoolType.NEW_VIRTUAL_THREAD)
                .repeat(Duration.ofMillis(100))
                .run(task -> {
                    if (counter.addAndGet(1) == 5) {
                        task.cancel();
                    }
                })
                .build();

        taskRegistry.start(repeatingTask);

        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .until(() -> counter.get() == 5);

        Assertions.assertThat(counter.get()).isEqualTo(5);
        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task")).isFalse();
    }

    @Test
    void start_ScheduledTestTask_SuccessfullyStartedWithDelay() {
        Assertions.assertThat(taskRegistry.isTaskRunning("scheduled-task-1")).isFalse();
        final long startTimeMS = System.currentTimeMillis();

        final AtomicInteger counter = new AtomicInteger(0);
        final ScheduledTask scheduledTask = new ScheduledTestTask(counter);
        taskRegistry.start(scheduledTask);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .until(() -> counter.get() == 1);

        final long timeElapsedBeforeRunning = System.currentTimeMillis() - startTimeMS;
        Assertions.assertThat(timeElapsedBeforeRunning).isBetween(999L, 1100L);
        Assertions.assertThat(taskRegistry.isTaskRunning("scheduled-task-1")).isTrue();
    }

    @Test
    void start_AlreadyRunningScheduledTestTask_ThrowsDuplicateException() {
        final ScheduledTask task = ScheduledTask.builder()
                .identifier("task")
                .schedulerPool(ScheduledTaskPoolType.NEW_VIRTUAL_THREAD)
                .repeat(Duration.ofMillis(100))
                .run($ -> {
                    // NO-OP
                })
                .build();

        final ScheduledTaskRegistry commonTaskRegistry = ScheduledTaskRegistry.common();
        Assertions.assertThatCode(() -> commonTaskRegistry.start(task)).doesNotThrowAnyException();
        Assertions.assertThatThrownBy(() -> commonTaskRegistry.start(task)).isInstanceOf(SchedulerDuplicateIdentifierException.class);
    }

    @Test
    void start_TwoTasksInSingleThreadPool_RunInSameThread() {
        final AtomicLong firstThreadId = new AtomicLong();
        final AtomicLong secondThreadId = new AtomicLong();

        final ScheduledTask repeatingTaskOne = ScheduledTask.builder()
                .identifier("repeating-task-1")
                .schedulerPool(ScheduledTaskPoolType.SINGLE_THREAD_FOR_ALL_TASKS)
                .repeat(Duration.ofMillis(100))
                .run($ -> firstThreadId.set(Thread.currentThread().getId()))
                .build();

        final ScheduledTask repeatingTaskTwo = ScheduledTask.builder()
                .identifier("repeating-task-2")
                .schedulerPool(ScheduledTaskPoolType.SINGLE_THREAD_FOR_ALL_TASKS)
                .repeat(Duration.ofMillis(100))
                .run($ -> secondThreadId.set(Thread.currentThread().getId()))
                .build();

        taskRegistry.start(repeatingTaskOne);
        taskRegistry.start(repeatingTaskTwo);

        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .until(() -> firstThreadId.get() != 0 && secondThreadId.get() != 0);

        Assertions.assertThat(firstThreadId.get()).isEqualTo(secondThreadId.get());
        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task-1")).isTrue();
        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task-2")).isTrue();
    }

    @Test
    void cancelAll_RepeatingTasksDuringRunning_CanceledSuccessful() {
        final ScheduledTask firstRepeatingTask = ScheduledTask.builder()
                .identifier("repeating-task-1")
                .schedulerPool(ScheduledTaskPoolType.NEW_VIRTUAL_THREAD)
                .repeat(Duration.ofMillis(100))
                .run($ -> {
                    // NO-OP
                })
                .build();

        final ScheduledTask secondRepeatingTask = ScheduledTask.builder()
                .identifier("repeating-task-2")
                .schedulerPool(ScheduledTaskPoolType.NEW_VIRTUAL_THREAD)
                .repeat(Duration.ofMillis(100))
                .run($ -> {
                    // NO-OP
                })
                .build();

        taskRegistry.start(firstRepeatingTask);
        taskRegistry.start(secondRepeatingTask);

        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task-1")).isTrue();
        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task-2")).isTrue();

        taskRegistry.cancelAll();

        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task-1")).isFalse();
        Assertions.assertThat(taskRegistry.isTaskRunning("repeating-task-2")).isFalse();
    }
}
