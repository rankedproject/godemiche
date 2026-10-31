package wtf.ranked.godemiche.memory;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum MemoryUnit {
    Bytes(1L),
    Kilobytes(1L << 10),
    Megabytes(1L << 20),
    Gigabytes(1L << 30),
    Terabytes(1L << 40);

    private final long bytes;

    MemoryUnit(final long bytes) {
        this.bytes = bytes;
    }

    public long bytes() {
        return bytes;
    }
}
