package wtf.ranked.godemiche;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.NullMarked;
import wtf.ranked.godemiche.scheduler.RunningTask;
import wtf.ranked.godemiche.scheduler.ScheduledTask;
import wtf.ranked.godemiche.scheduler.ScheduledTaskPoolType;

@NullMarked
final class ScheduledTestTask implements ScheduledTask {

    private final AtomicInteger counter;

    ScheduledTestTask(final AtomicInteger counter) {
        this.counter = counter;
    }

    @Override
    public ScheduledTaskOptions options() {
        return ScheduledTaskOptions.builder()
                .identifier("scheduled-task-1")
                .schedulerPool(ScheduledTaskPoolType.NEW_VIRTUAL_THREAD)
                .delay(Duration.ofSeconds(1))
                .repeat(Duration.ofSeconds(1))
                .build();
    }

    @Override
    public void run(final RunningTask runningTask) {
        this.counter.addAndGet(1);
    }
}
