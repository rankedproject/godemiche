# 🍆 G.O.D.E.M.I.C.H.E - Godemiche
> More than a library - it's senior-level code satisfaction

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
[![Maven Central](https://img.shields.io/maven-central/v/wtf.ranked/godemiche)](https://central.sonatype.com/artifact/wtf.ranked/godemiche)
![GitHub stars](https://img.shields.io/github/stars/rankedproject/godemiche?style=social)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

Documentation: https://docs.ranked.wtf/

Maven Central: https://central.sonatype.com/artifact/wtf.ranked/godemiche

---

## Getting started

Godemiche is a Java utility library that makes failure, shared object reuse, and scheduled task lifecycle explicit.

> G.O.D.E.M.I.C.H.E stands for Generic Object Data Execution Management Infrastructure Core Helper Engine.

It targets a simple set of real-world Java problems:
- unclear failure handling (`null`, exceptions, error flags)
- duplicated or hidden shared object creation (executors, clients, services)
- uncontrolled background tasks that are hard to track or stop

Godemiche enforces one rule: **important behavior must be visible in the code**. Instead of implicit patterns, it provides four focused primitives:
- **Result** → explicit success/failure as a return type
- **ObjectPool** → deterministic shared object reuse by key
- **ScheduledTask** → scheduled execution with identity + lifecycle control
- **Memory** → immutable memory size representation with explicit unit conversion

---

## Install

### Maven Central

Use the **latest version from Maven Central** (shown in the badge above):

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("wtf.ranked:godemiche:<latest-version>")
}
```

[![Maven Central](https://img.shields.io/maven-central/v/wtf.ranked/godemiche)](https://central.sonatype.com/artifact/wtf.ranked/godemiche)

Always replace `<latest-version>` with the latest version available on Maven Central, not a hardcoded version. The badge above reflects the current published version.

---

## Result

`Result` controls execution flow without relying on exceptions, nulls, or magic return values. It unifies success and failure paths into a single type-safe contract, making outcomes explicit and composable.

It allows mapping, routing by failure reason, and functional-style chaining without mutating state.

```java
enum UserFailureReason implements ResultReason.Failure {
    USER_NOT_FOUND,
    INVALID_EMAIL,
    USER_BANNED,
}

Result<User> userAccountResult = userService.findByEmail(userEmail)
        .success(user -> IO.println(user.getName()))
        .failure(UserFailureReason.INVALID_EMAIL, () -> recreateUserEmail(userEmail))
        .failure(UserFailureReason.USER_BANNED, () -> banUserIpAddress(userEmail))
        .failure(reason -> IO.println(reason));
```

In this example, `userService.findByEmail` returns a `Result<User>` as part of its public API contract. This makes both success and failure paths explicit and composable.

Failure routing can be handled per reason without conditional branching:

```java
Results.<User>ofFailure(UserFailureReason.USER_NOT_FOUND)
    .failure(UserFailureReason.USER_NOT_FOUND, reason -> IO.println("User record missing"))
    .failure(UserFailureReason.INVALID_EMAIL, reason -> IO.println("Email format rejected"));
```

Async execution uses the same model on top of `CompletableFuture`:

```java
CompletableFuture<String> futureUserName = database.findUsernameById("1");

ResultAsync<String> asyncResult = Results.ofAsync(futureUserName)
        .mapSuccess(String::toUpperCase)
        .flatMapFailure(reason -> findDefaultUsernameAsync())
        .success(userName -> IO.println(userName)); // executes asynchronously without blocking
```

Success and failure signals propagate consistently through the chain, regardless of synchronous or asynchronous execution.

---

## ObjectPool

`ObjectPool` provides a simple, key-based mechanism for shared instance reuse. It replaces scattered singleton patterns and duplicated initialization logic with a centralized, deterministic lookup model.

If an object for a given key already exists, it is returned immediately. Otherwise, it is created using the provided supplier and stored for future reuse.

```java
ExecutorService executor1 = ObjectPool.get(
        "executor",
        () -> Executors.newFixedThreadPool(4)
);

ExecutorService executor2 = ObjectPool.get(
        "executor",
        () -> Executors.newFixedThreadPool(4)
);

IO.println(executor1 == executor2); // true
```

### Practical Use Case

This is especially useful for shared infrastructure components such as thread pools, HTTP clients, or database connectors:

```java
HttpClient client = ObjectPool.get(
        "http-client",
        HttpClient::newHttpClient
);
```

Instead of managing singletons manually or passing instances through multiple layers, the pool guarantees consistent reuse based on identity.

---

## ScheduledTask

`ScheduledTask` provides structured, lifecycle-managed scheduling for repeated or delayed execution. It replaces raw executor usage and untracked `ScheduledFuture` instances with identifiable, registry-managed tasks.

Each task has a unique identifier and is controlled through a central registry.

---

### Builder-Based Definition

```java
ScheduledTask task = ScheduledTask.builder()
        .identifier("cleanup")
        .delay(Duration.ofSeconds(5))
        .repeat(Duration.ofMinutes(10))
        .schedulerPool(ScheduledTaskPoolType.NEW_PLATFORM_THREAD)
        .run(t -> performCleanup())
        .build();

ScheduledTaskRegistry.common().start(task);
```

---

### Class-Based Definition

```java
public final class CleanupTask implements ScheduledTask {

    private final CacheService cacheService;

    public CleanupTask(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public ScheduledTaskOptions options() {
        return ScheduledTaskOptions.builder()
            .identifier("cleanup")
            .delay(Duration.ofSeconds(5))
            .repeat(Duration.ofMinutes(10))
            .schedulerPool(ScheduledTaskPoolType.NEW_PLATFORM_THREAD)
            .build();
    }

    @Override
    public void run(RunningTask task) {
        cacheService.cleanupExpiredEntries();
    }
}
```

Both approaches produce identical runtime behavior. The registry is responsible for execution, ensuring identifier uniqueness, and providing lifecycle operations such as cancellation and lookup.

---

### Self-Terminating Tasks

Tasks can stop themselves based on runtime conditions without external control logic:

```java
ScheduledTask task = ScheduledTask.builder()
        .identifier("retry-sync")
        .repeat(Duration.ofSeconds(30))
        .schedulerPool(ScheduledTaskPoolType.NEW_PLATFORM_THREAD)
        .run(t -> {
            if (shouldStop()) {
                t.cancel();
                return;
            }

            performSync();
        })
        .build();

ScheduledTaskRegistry.common().start(task);
```

This allows long-running workflows to remain self-contained while still being fully controlled by the registry.

---

## Memory

`Memory` provides a simple, type-safe representation of memory sizes. It replaces manual unit conversions and scattered byte calculations with a single, immutable value.

Each instance stores its size internally as `long` bytes. Conversion methods return `double` values, preserving fractional results instead of truncating them to integers.

It supports bytes, kilobytes, megabytes, gigabytes, and terabytes.

```java
Memory memory = Memory.of(5, MemoryUnit.Gigabytes);

double kilobytes = memory.toKilobytes();
double megabytes = memory.toMegabytes();
double gigabytes = memory.toGigabytes();
double terabytes = memory.toTerabytes();
```

### Factory Methods

`Memory` provides dedicated factory methods for each supported unit, allowing sizes to be created without explicitly specifying a `MemoryUnit`:

```java
Memory bytes = Memory.ofBytes(1024);
Memory kilobytes = Memory.ofKilobytes(100);
Memory megabytes = Memory.ofMegabytes(512);
Memory gigabytes = Memory.ofGigabytes(5);
Memory terabytes = Memory.ofTerabytes(2);

Memory zero = Memory.zero();
```

All values use the same byte-based representation, regardless of the unit used during construction.

### Conversion

Conversions preserve fractional values instead of truncating them to whole units:

```java
Memory memory = Memory.ofKilobytes(100);

IO.println(memory.toBytes());      // 102400
IO.println(memory.toMegabytes());  // 0.09765625
IO.println(memory.toGigabytes());  // 0.000095367431640625
```

Conversion methods operate on the internally stored byte value, ensuring consistent results across supported units.

### Practical Use Case

This is especially useful for JVM memory limits, cache capacities, payload sizes, file sizes, and resource allocation:

```java
Memory maxHeap = Memory.ofGigabytes(4);
Memory cacheCapacity = Memory.ofMegabytes(512);

if (maxHeap.toBytes() > cacheCapacity.toBytes()) {
    IO.println("Heap capacity exceeds cache capacity");
}
```

Instead of manually calculating byte values or repeating unit multipliers, memory sizes can be represented and converted through a single type-safe API.

Negative values are rejected during construction, and arithmetic overflow is detected before the value is stored. Once created, a `Memory` instance cannot be modified.
