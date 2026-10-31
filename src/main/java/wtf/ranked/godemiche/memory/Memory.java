
package wtf.ranked.godemiche.memory;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NullMarked;

/**
 * An immutable representation of a memory size, internally stored as bytes.
 *
 * <p>Provides a unified API for creating memory sizes using different units
 * and converting between bytes, kilobytes, megabytes, gigabytes, and terabytes.
 *
 * <p>All memory sizes are stored internally as {@code long} values to preserve
 * integer precision and avoid floating-point inaccuracies during storage.
 * Conversion methods return {@code double} values to preserve fractional
 * results when converting to larger units.
 *
 * <p>All supported units use binary conversion factors (base 1024):
 * <ul>
 *     <li>1 Kilobyte = 1,024 bytes</li>
 *     <li>1 Megabyte = 1,024 kilobytes</li>
 *     <li>1 Gigabyte = 1,024 megabytes</li>
 *     <li>1 Terabyte = 1,024 gigabytes</li>
 * </ul>
 *
 * <p>Negative memory sizes are not supported. Arithmetic overflow during
 * memory size construction is detected and reported explicitly.
 *
 * <p>Instances are immutable and therefore safe to share between threads.
 */
@NullMarked
public final class Memory {

    private final long bytes;

    /**
     * Constructs a {@link Memory} instance representing the specified
     * number of bytes.
     *
     * @param bytes the memory size in bytes; must not be negative
     * @throws IllegalArgumentException if the specified size is negative
     */
    private Memory(final long bytes) {
        Preconditions.checkArgument(bytes >= 0, "Memory cannot be less than 0");
        this.bytes = bytes;
    }

    /**
     * Returns a {@link Memory} instance representing zero bytes.
     *
     * @return a zero-sized memory instance
     */
    public static Memory zero() {
        return of(0, MemoryUnit.BYTES);
    }

    /**
     * Creates a {@link Memory} instance using the specified size and unit.
     *
     * <p>The specified size is converted to bytes before being stored.
     * Conversion uses {@link Math#multiplyExact(long, long)} to detect
     * arithmetic overflow.
     *
     * @param size the memory size; must not be negative
     * @param memoryUnit the unit used to interpret the specified size
     * @return a new memory instance representing the specified size
     * @throws IllegalArgumentException if the resulting memory size is negative
     * @throws ArithmeticException if converting the specified size to bytes
     *         causes a {@code long} overflow
     */
    public static Memory of(final long size, final MemoryUnit memoryUnit) {
        return new Memory(Math.multiplyExact(size, memoryUnit.bytes()));
    }

    /**
     * Creates a {@link Memory} instance representing the specified
     * number of bytes.
     *
     * @param size the memory size in bytes; must not be negative
     * @return a new memory instance representing the specified number of bytes
     * @throws IllegalArgumentException if the specified size is negative
     */
    public static Memory ofBytes(final long size) {
        return of(size, MemoryUnit.BYTES);
    }

    /**
     * Creates a {@link Memory} instance representing the specified
     * number of kilobytes.
     *
     * <p>One kilobyte is equivalent to 1,024 bytes.
     *
     * @param size the memory size in kilobytes; must not be negative
     * @return a new memory instance representing the specified size
     * @throws IllegalArgumentException if the specified size is negative
     * @throws ArithmeticException if converting the specified size to bytes
     *         causes a {@code long} overflow
     */
    public static Memory ofKilobytes(final long size) {
        return of(size, MemoryUnit.KILOBYTES);
    }

    /**
     * Creates a {@link Memory} instance representing the specified
     * number of megabytes.
     *
     * <p>One megabyte is equivalent to 1,024 kilobytes.
     *
     * @param size the memory size in megabytes; must not be negative
     * @return a new memory instance representing the specified size
     * @throws IllegalArgumentException if the specified size is negative
     * @throws ArithmeticException if converting the specified size to bytes
     *         causes a {@code long} overflow
     */
    public static Memory ofMegabytes(final long size) {
        return of(size, MemoryUnit.MEGABYTES);
    }

    /**
     * Creates a {@link Memory} instance representing the specified
     * number of gigabytes.
     *
     * <p>One gigabyte is equivalent to 1,024 megabytes.
     *
     * @param size the memory size in gigabytes; must not be negative
     * @return a new memory instance representing the specified size
     * @throws IllegalArgumentException if the specified size is negative
     * @throws ArithmeticException if converting the specified size to bytes
     *         causes a {@code long} overflow
     */
    public static Memory ofGigabytes(final long size) {
        return of(size, MemoryUnit.GIGABYTES);
    }

    /**
     * Creates a {@link Memory} instance representing the specified
     * number of terabytes.
     *
     * <p>One terabyte is equivalent to 1,024 gigabytes.
     *
     * @param size the memory size in terabytes; must not be negative
     * @return a new memory instance representing the specified size
     * @throws IllegalArgumentException if the specified size is negative
     * @throws ArithmeticException if converting the specified size to bytes
     *         causes a {@code long} overflow
     */
    public static Memory ofTerabytes(final long size) {
        return of(size, MemoryUnit.TERABYTES);
    }

    /**
     * Returns the memory size in bytes.
     *
     * <p>Bytes are the internal storage unit, so this conversion does not
     * introduce rounding or precision loss.
     *
     * @return the exact memory size in bytes
     */
    public long toBytes() {
        return bytes;
    }

    /**
     * Returns the memory size in kilobytes.
     *
     * <p>Fractional results are preserved instead of being truncated.
     *
     * @return the memory size in kilobytes
     */
    public double toKilobytes() {
        return (double) bytes / MemoryUnit.KILOBYTES.bytes();
    }

    /**
     * Returns the memory size in megabytes.
     *
     * <p>Fractional results are preserved instead of being truncated.
     *
     * @return the memory size in megabytes
     */
    public double toMegabytes() {
        return (double) bytes / MemoryUnit.MEGABYTES.bytes();
    }

    /**
     * Returns the memory size in gigabytes.
     *
     * <p>Fractional results are preserved instead of being truncated.
     *
     * @return the memory size in gigabytes
     */
    public double toGigabytes() {
        return (double) bytes / MemoryUnit.GIGABYTES.bytes();
    }

    /**
     * Returns the memory size in terabytes.
     *
     * <p>Fractional results are preserved instead of being truncated.
     *
     * @return the memory size in terabytes
     */
    public double toTerabytes() {
        return (double) bytes / MemoryUnit.TERABYTES.bytes();
    }
}
