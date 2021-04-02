package github.yuhongye.common.bitwise;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Bits64ComposerTest {
    @Test
    public void testInitMaskAnsShift() {
        Bits64Composer composer = new Bits64Composer();

        long[] maskAnsShift = composer.initMaskAnsShift(Long.SIZE, 2, 3, 1, 2, 5, 8);
        assertEquals(12, maskAnsShift.length);
        long[] expected = {0x03, 0, 0x1C, 2, 0x20, 5, 0xC0, 6, 0x1F00, 8, 0x1FE000, 13};
        assertArrayEquals(expected, maskAnsShift);

        maskAnsShift = composer.initMaskAnsShift(Long.SIZE, 3);
        assertEquals(2, maskAnsShift.length);
        expected = new long[]{0x07, 0};
        assertArrayEquals(expected, maskAnsShift);

        maskAnsShift = composer.initMaskAnsShift(Long.SIZE, 3, 10, 19);
        assertEquals(6, maskAnsShift.length);
        expected = new long[]{0x07, 0, 0x1FF8, 3, 0xFFFFE000L, 13};
        assertArrayEquals(expected, maskAnsShift);

        maskAnsShift = composer.initMaskAnsShift(Long.SIZE, 3, 10, 18, 1);
        assertEquals(8, maskAnsShift.length);
        expected = new long[]{0x07, 0, 0x1FF8, 3, 0x7FFFE000, 13, 0x80000000L, 31};
        assertArrayEquals(expected, maskAnsShift);

        maskAnsShift = composer.initMaskAnsShift(Long.SIZE, 32, 32);
        assertEquals(4, maskAnsShift.length);
        expected = new long[]{0xFFFFFFFFL, 0, 0xFFFFFFFF00000000L, 32};
        assertArrayEquals(expected, maskAnsShift);
    }

    @Test
    public void testGetterSetter() {
        Bits64Composer composer = new Bits64Composer(1, 1, 2, 3, 4);
        long[] expected = {1, 0, 2, 7, 12};
        test(composer, expected);

        composer = new Bits64Composer(8, 10, 12);
        expected = new long[]{243, 34, 1047};
        test(composer, expected);
    }

    @Test
    public void testOverflow() {
        Bits64Composer composer = new Bits64Composer(2, 3, 4);
        long value = 0;
        value = composer.set(value, 0, 5);
        assertEquals(1, composer.get(value, 0));
        value = composer.set(value, 1, 14);
        assertEquals(6, composer.get(value, 1));
    }

    private void test(Bits64Composer composer, long[] expected) {
        long value = 0;
        for (int i = 0; i < expected.length; i++) {
            value = composer.set(value, i, expected[i]);
        }
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], composer.get(value, i));
        }
    }
}
