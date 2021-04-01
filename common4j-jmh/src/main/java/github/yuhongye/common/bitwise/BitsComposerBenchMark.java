package github.yuhongye.common.bitwise;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BitsComposerBenchMark {

    @Benchmark
    public void testCompose() {
        Bits32Composer c1 = new Bits32Composer(new int[]{3, 4, 5, 6});
        Bits32Composer c2 = new Bits32Composer(new int[]{1, 8, 5, 1, 9, 2, 6});
        Bits32Composer c3 = new Bits32Composer(new int[]{8, 8, 8, 8});
        int sum = 0;
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int i = (int) (System.currentTimeMillis() & 1024);
        for (; i < 100 * 10000; i++) {
            for (int k = 0; k < 4; k++) {
                v1 = c1.set(v1, k, i);
                v3 = c3.set(v3, k, i);
            }
            for (int k = 0; k < 7; k++) {
                v2 = c2.set(v2, k, i);
            }

            for (int k = 0; k < 4; k++) {
                sum += c1.get(v1, k);
                sum += c3.get(v3, k);
            }
            for (int k = 0; k < 7; k++) {
                sum += c2.get(v2, k);
            }
        }
        System.out.println("sum: " + sum);
    }

    @Benchmark
    public void testCompose2() {
        Bits64Composer c1 = new Bits64Composer(new int[]{3, 4, 5, 6});
        Bits64Composer c2 = new Bits64Composer(new int[]{1, 8, 5, 1, 9, 2, 6});
        Bits64Composer c3 = new Bits64Composer(new int[]{8, 8, 8, 8});
        int sum = 0;
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int i = (int) (System.currentTimeMillis() & 1024);
        for (; i < 100 * 10000; i++) {
            for (int k = 0; k < 4; k++) {
                v1 = (int) c1.set(v1, k, i);
                v3 = (int) c3.set(v3, k, i);
            }
            for (int k = 0; k < 7; k++) {
                v2 = (int) c2.set(v2, k, i);
            }

            for (int k = 0; k < 4; k++) {
                sum += c1.get(v1, k);
                sum += c3.get(v3, k);
            }
            for (int k = 0; k < 7; k++) {
                sum += c2.get(v2, k);
            }
        }
        System.out.println("sum: " + sum);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BitsComposerBenchMark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
