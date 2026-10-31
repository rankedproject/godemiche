package wtf.ranked.godemiche.memory;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Memory {

    private final long bytes;

    private Memory(final long bytes) {
        Preconditions.checkArgument(bytes >= 0, "Memory cannot be less than 0");
        this.bytes = bytes;
    }

    public static Memory zero() {
        return of(0, MemoryUnit.Bytes);
    }

    public static Memory of(final long size, final MemoryUnit memoryUnit) {
        return new Memory(Math.multiplyExact(size, memoryUnit.bytes()));
    }

    public static Memory ofBytes(final long size) {
        return of(size, MemoryUnit.Bytes);
    }

    public static Memory ofKilobytes(final long size) {
        return of(size, MemoryUnit.Kilobytes);
    }

    public static Memory ofMegabytes(final long size) {
        return of(size, MemoryUnit.Megabytes);
    }

    public static Memory ofGigabytes(final long size) {
        return of(size, MemoryUnit.Gigabytes);
    }

    public static Memory ofTerabytes(final long size) {
        return of(size, MemoryUnit.Terabytes);
    }

    public long toBytes() {
        return bytes;
    }

    public double toKilobytes() {
        return (double) bytes / MemoryUnit.Kilobytes.bytes();
    }

    public double toMegabytes() {
        return (double) bytes / MemoryUnit.Megabytes.bytes();
    }

    public double toGigabytes() {
        return (double) bytes / MemoryUnit.Gigabytes.bytes();
    }

    public double toTerabytes() {
        return (double) bytes / MemoryUnit.Terabytes.bytes();
    }
}
