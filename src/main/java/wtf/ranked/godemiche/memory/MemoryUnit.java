package wtf.ranked.godemiche.memory;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum MemoryUnit {
    BYTES(1L),
    KILOBYTES(1L << 10),
    MEGABYTES(1L << 20),
    GIGABYTES(1L << 30),
    TERABYTES(1L << 40);

    private final long bytes;

    MemoryUnit(final long bytes) {
        this.bytes = bytes;
    }

    /**
     * Returns the number of bytes represented by this unit.
     *
     * @return the number of bytes
     */
    public long bytes() {
        return bytes;
    }
}
