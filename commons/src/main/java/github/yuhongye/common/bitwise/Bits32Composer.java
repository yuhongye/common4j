package github.yuhongye.common.bitwise;

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
 * 最终结果可以放到byte, short, int 中使用本类，如果超过了32位 使用 {@link Bits64Composer}
 * 这个类为什么会存在？ 在jmh中测试发现，对于composed value is int 的场景下，该类略快于{@link Bits64Composer}
 */
public class Bits32Composer {
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
    private int[] maskAndShift;

    /**
     * 将设有3个字段，分别占用3， 8， 10位，则将3, 8, 10传递进去
     * @param bitCountPerField 按字段顺序传递每个字段占用的位数，总和不超过32位
     */
    public Bits32Composer(int... bitCountPerField) {
        maskAndShift = initMaskAnsShift(bitCountPerField);
    }

    int[] initMaskAnsShift(int... bitCountPerField) {
        return maskAndShift = Arrays.stream(Bits64Composer.initMaskAnsShift(Integer.SIZE, bitCountPerField))
                .mapToInt(v -> (int) v)
                .toArray();
    }

    /**
     * 获取指定字段的值
     * @param composedValue 所有字段组合在一起的值
     * @param index 字段下标，从0开始
     * @return 字段的值
     */
    public int get(int composedValue, int index) {
        int idx = index * 2;
        int clearOther = maskAndShift[idx];
        int shift = maskAndShift[idx+1];
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
    public int set(int composedValue, int index, int newVal) {
        int idx = index * 2;
        int clearOther = maskAndShift[idx];
        int shift = maskAndShift[idx+1];
        int clearSelf = ~clearOther;
        // 1. 将该字段原来的值清楚掉
        composedValue &= clearSelf;
        // 2. 将新值移动到正确的位置, 同时确保新值不会侵占别的字段的位置
        int fieldVal = (newVal << shift) & clearOther;
        // 3. 设置新值
        return composedValue | fieldVal;
    }
}
