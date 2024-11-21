package timingtest;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns = new AList<Integer>();
        AList<Double> times = new AList<Double>();
        AList<Integer> opCounts = new AList<Integer>();

        // How many times has the experiment been conducted
        int nTimes = 8;
        int nOpts = 10000;
        int[] nArray = new int[nTimes];
        for (int i = 0; i < nTimes; i++) {
            nArray[i] = (int) (Math.pow(2, i) * 1000);
        }

        for (int i = 0; i < nTimes; i++) {
            int N = nArray[i];
            SLList<Integer> list = new SLList<Integer>();
            for (int j = 0; j < N; j++) {
                list.addLast(j);
            }

            Stopwatch sw = new Stopwatch();
            for (int j = 0; j < nOpts; j++) {
                list.getLast();
            }
            double timeInSeconds = sw.elapsedTime();

            Ns.addLast(N);
            times.addLast(timeInSeconds);
            opCounts.addLast(nOpts);
        }

        printTimingTable(Ns, times, opCounts);
    }

}
