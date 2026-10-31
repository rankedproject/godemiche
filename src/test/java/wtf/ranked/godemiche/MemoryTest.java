
package wtf.ranked.godemiche;

import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import wtf.ranked.godemiche.memory.Memory;
import wtf.ranked.godemiche.memory.MemoryUnit;

@NullMarked
final class MemoryTest {

    private static final long HUNDRED_MEGABYTES_IN_KILOBYTES = 102400;
    private static final double HUNDRED_KILOBYTES_IN_MEGABYTES = 0.09765625;

    @Test
    void convert_HundredMegabytes_Kilobytes() {
        final Memory memory = Memory.of(100, MemoryUnit.MEGABYTES);
        Assertions.assertThat(memory.toKilobytes()).isEqualTo(HUNDRED_MEGABYTES_IN_KILOBYTES);
    }

    @Test
    void convert_HundredKilobytes_Megabytes() {
        final Memory memory = Memory.of(100, MemoryUnit.KILOBYTES);
        Assertions.assertThat(memory.toMegabytes()).isEqualTo(HUNDRED_KILOBYTES_IN_MEGABYTES);
    }

    @Test
    void convert_Gigabyte_Megabytes() {
        final Memory memory = Memory.of(1, MemoryUnit.GIGABYTES);
        Assertions.assertThat(memory.toMegabytes()).isEqualTo(1024.0);
    }

    @Test
    void convert_Terabyte_Gigabytes() {
        final Memory memory = Memory.of(1, MemoryUnit.TERABYTES);
        Assertions.assertThat(memory.toGigabytes()).isEqualTo(1024.0);
    }

    @Test
    void convert_Megabyte_Bytes() {
        final Memory memory = Memory.of(1, MemoryUnit.MEGABYTES);
        Assertions.assertThat(memory.toBytes()).isEqualTo(1_048_576L);
    }

    @Test
    void convert_Terabyte_Bytes() {
        final Memory memory = Memory.of(1, MemoryUnit.TERABYTES);
        Assertions.assertThat(memory.toBytes()).isEqualTo(1_099_511_627_776L);
    }

    @Test
    void convert_OneByte_Kilobytes() {
        final Memory memory = Memory.of(1, MemoryUnit.BYTES);
        Assertions.assertThat(memory.toKilobytes()).isEqualTo(1.0 / 1024);
    }

    @Test
    void convert_OneByte_Megabytes() {
        final Memory memory = Memory.of(1, MemoryUnit.BYTES);
        Assertions.assertThat(memory.toMegabytes()).isEqualTo(1.0 / 1_048_576);
    }

    @Test
    void convert_OneKilobyte_Gigabytes() {
        final Memory memory = Memory.of(1, MemoryUnit.KILOBYTES);
        Assertions.assertThat(memory.toGigabytes()).isEqualTo(1.0 / 1_048_576);
    }

    @Test
    void convert_ZeroBytes_AllUnits() {
        final Memory memory = Memory.zero();

        Assertions.assertThat(memory.toBytes()).isZero();
        Assertions.assertThat(memory.toKilobytes()).isZero();
        Assertions.assertThat(memory.toMegabytes()).isZero();
        Assertions.assertThat(memory.toGigabytes()).isZero();
        Assertions.assertThat(memory.toTerabytes()).isZero();
    }

    @Test
    void convert_OneKilobyte_Bytes() {
        final Memory memory = Memory.of(1, MemoryUnit.KILOBYTES);
        Assertions.assertThat(memory.toBytes()).isEqualTo(1024L);
    }

    @Test
    void convert_OneMegabyte_Kilobytes() {
        final Memory memory = Memory.of(1, MemoryUnit.MEGABYTES);
        Assertions.assertThat(memory.toKilobytes()).isEqualTo(1024.0);
    }

    @Test
    void convert_OneGigabyte_Megabytes() {
        final Memory memory = Memory.of(1, MemoryUnit.GIGABYTES);
        Assertions.assertThat(memory.toMegabytes()).isEqualTo(1024.0);
    }

    @Test
    void convert_OneTerabyte_Megabytes() {
        final Memory memory = Memory.of(1, MemoryUnit.TERABYTES);
        Assertions.assertThat(memory.toMegabytes()).isEqualTo(1_048_576.0);
    }

    @Test
    void create_NegativeBytes_ThrowsException() {
        Assertions
                .assertThatThrownBy(() -> Memory.of(-1, MemoryUnit.BYTES))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_MaximumLongBytes_DoesNotOverflow() {
        final Memory memory = Memory.of(Long.MAX_VALUE, MemoryUnit.BYTES);
        Assertions.assertThat(memory.toBytes()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void create_OverflowingLong_ThrowsException() {
        Assertions
                .assertThatThrownBy(() -> Memory.of(Long.MAX_VALUE, MemoryUnit.KILOBYTES))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void create_Zero() {
        final Memory memory = Memory.zero();
        Assertions.assertThat(memory.toBytes()).isZero();
    }

    @Test
    void createOf_Bytes() {
        final Memory memory = Memory.ofBytes(1024);
        Assertions.assertThat(memory.toBytes()).isEqualTo(1024L);
    }

    @Test
    void createOf_Kilobytes() {
        final Memory memory = Memory.ofKilobytes(100);
        Assertions.assertThat(memory.toBytes()).isEqualTo(102_400L);
    }

    @Test
    void createOf_Megabytes() {
        final Memory memory = Memory.ofMegabytes(100);
        Assertions.assertThat(memory.toBytes()).isEqualTo(104_857_600L);
    }

    @Test
    void createOf_Gigabytes() {
        final Memory memory = Memory.ofGigabytes(5);
        Assertions.assertThat(memory.toBytes()).isEqualTo(5_368_709_120L);
    }

    @Test
    void createOf_Terabytes() {
        final Memory memory = Memory.ofTerabytes(2);
        Assertions.assertThat(memory.toBytes()).isEqualTo(2_199_023_255_552L);
    }

    @Test
    void createOf_ZeroBytes() {
        final Memory memory = Memory.ofBytes(0);
        Assertions.assertThat(memory.toBytes()).isZero();
    }

    @Test
    void createOf_NegativeBytes_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofBytes(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory cannot be less than 0");
    }

    @Test
    void create_NegativeKilobytes_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofKilobytes(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory cannot be less than 0");
    }

    @Test
    void create_NegativeMegabytes_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofMegabytes(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory cannot be less than 0");
    }

    @Test
    void create_NegativeGigabytes_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofGigabytes(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory cannot be less than 0");
    }

    @Test
    void create_NegativeTerabytes_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofTerabytes(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memory cannot be less than 0");
    }

    @Test
    void create_Bytes_MaximumLong_DoesNotOverflow() {
        final Memory memory = Memory.ofBytes(Long.MAX_VALUE);
        Assertions.assertThat(memory.toBytes()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void create_Kilobytes_Overflow_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofKilobytes(Long.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void create_Megabytes_Overflow_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofMegabytes(Long.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void create_Gigabytes_Overflow_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofGigabytes(Long.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void create_Terabytes_Overflow_ThrowsException() {
        Assertions.assertThatThrownBy(() -> Memory.ofTerabytes(Long.MAX_VALUE))
                .isInstanceOf(ArithmeticException.class);
    }
}
