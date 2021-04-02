package github.yuhongye.common.bitwise;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * 假设在某个场景中有5个整型字段，它们的取值范围分别是：[0, 1], [0, 1], [0, 31], [0, 499], [0, 499]
 * 那么每个字段分别需要存储位数: 1, 1, 5, 9, 9, 总共需要 25 位，因此可以把它们组合到一个 int 中. 通过这种
 * 方式可以极大的节省内存，尤其是在大数据场景下。
 * <p>
 *     使用方式：
 *     // 确定每个字段占用的位数，总位数不超过 32
 *     // 假设我们有4个字段：dayOfMonth，需要 5-bit; dataType: 需要 1-bit; deviceType: 需要 1-bit; pv: 需要 16-bit;
 *     BitComposer composer = new BitComposer(5, 1, 1, 16);
 *     int value = 0;
 *
 *     // 可以设置每个字段的值
 *     // set dayOfMonth = 13
 *     value = composer.set(value, 0, 13);
 *     // set pv = 34;
 *     value = composer.set(value, 3, 34);
 *
 *     // 获取字段的值
 *     int dayOfMonth = composer.get(value, 0);
 *     int pv = composer.get(value, 3);
 * </p>
 *
 */
public class Bits64Composer {
    /**
     * 如何获取和设置某个字段的值?
     * 字段 X 在 int 中的布局如下:
     * +----------+-----+---------+
     * |    A     |  X  |    B    |
     * +----------+-----+---------+
     * 1. 在获取 X 的值时，我们需要把 A 部分清零，然后左移把 B 完全覆盖掉, 因此我们需要一个 clear other mask，和右移的位数 shift
     *    在实现中，我们同时把 A 和 B 都清零了.
     *
     * 2. 在set X 的值时，我们首先需要把 X 的旧值给清0，然后把新值左移 shift 位, 最后通过一次或运算进行 set.
     *    原来整个int的布局: A X0 B
     *    1. 清除 X 的旧值 X0: A 0 B
     *    2. 将新值 X1 放到正确的位置: 0 X1 0
     *    3. (1) | （2） = (A 0 B) | (0 X1 0) ==> A X1 B
     *   在 set 时我们需要 3 个值:
     *      - clear self mask: 用来清除旧值的 mask
     *      - shift: 新值左移的位数，用来将新值放到正确的位置
     *      - clear other mask: 确保新值不会侵占其他字段的位置
     *   等式: clear self mask = ~clear other mask
     */

    /**
     * 每个字段在数组中占两个slot, mask和需要移动的位数
     */
    private long[] maskAndShift;

    /**
     * 将设有3个字段，分别占用3， 8， 10位，则将3, 8, 10传递进去
     * @param bitCountPerField 按字段顺序传递每个字段占用的位数，总和不超过32位
     */
    public Bits64Composer(int... bitCountPerField) {
        maskAndShift = initMaskAnsShift(Long.SIZE, bitCountPerField);
    }

    static long[] initMaskAnsShift(int capacity, int... bitCountPerField) {
        int totalBitNumber = Arrays.stream(bitCountPerField).sum();
        Preconditions.checkArgument(totalBitNumber <= capacity, "总数量: " + capacity + ", 要求的容量: " + totalBitNumber);
        long[] maskAndShift = new long[bitCountPerField.length * 2];
        int start = 0;
        int index = 0;
        for (int bits : bitCountPerField) {
            int shift = start + bits;
            // [0, start) 都是1
            long prev = (1L << start) - 1;
            // [0, shift) 都是1
            long current = (1L << shift) - 1;
            /**
             * shift == 32，(1 << shift) == (1 << 0)
             * shift == 64, (1 << shift) == (1 << 0)
             */
            if (shift == capacity) {
                current = -1L;
            }
            long mask = prev ^ current;
            maskAndShift[index++] = mask;
            maskAndShift[index++] = start;

            start = shift;
        }

        return maskAndShift;
    }

    /**
     * 获取指定字段的值
     * @param composedValue 所有字段组合在一起的值
     * @param index 字段下标，从0开始
     * @return 字段的值
     */
    public long get(long composedValue, int index) {
        int idx = index * 2;
        long clearOther = maskAndShift[idx];
        int shift = (int) maskAndShift[idx+1];
        // 将其他部分清理后移位返回
        return (composedValue & clearOther) >>> shift;
    }

    /**
     *
     * @param composedValue 所有字段组合在一起的值
     * @param index 字段下标，从0开始
     * @param newVal 指定字段的新值
     * @return 更新指定字段后总的value
     */
    public long set(long composedValue, int index, long newVal) {
        int idx = index * 2;
        long clearOther = maskAndShift[idx];
        int shift = (int) maskAndShift[idx+1];
        long clearSelf = ~clearOther;
        // 1. 将该字段原来的值清楚掉
        composedValue &= clearSelf;
        // 2. 将新值移动到正确的位置, 同时确保新值不会侵占别的字段的位置
        long fieldVal = (newVal << shift) & clearOther;
        // 3. 设置新值
        return composedValue | fieldVal;
    }
}
