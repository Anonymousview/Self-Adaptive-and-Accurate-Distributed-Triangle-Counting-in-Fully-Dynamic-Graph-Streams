import java.io.*;
import java.util.Map;
import java.util.HashMap;

/*
 *  Calculate count_global_variance for n trials
 */

public class count_global_variance {
    public static void main(String[] args) throws IOException {

        if (args.length < 4) {
            printError();
            System.exit(-1);
        }

        final int trials = Integer.valueOf(args[0]);
        System.out.println("trials: " + trials);
        final String inputEstimatedCountsPath = args[1];
        System.out.println("inputEstimatedCountsPath: " + inputEstimatedCountsPath);
        final String inputRealCounts = args[2];
        System.out.println("inputRealCounts: " + inputRealCounts);
        final String outputGlobalVariance = args[3];
        System.out.println("outputGlobalVariance: " + outputGlobalVariance);

        long startTime = System.currentTimeMillis();

        //1. Read real local counts
        double realGlobalCounts = 0;

        BufferedReader brRealCounts = new BufferedReader(new FileReader(inputRealCounts));
        brRealCounts.readLine();//The 1'st row is not used.
        String line = brRealCounts.readLine();
        realGlobalCounts = Double.valueOf(line);

        brRealCounts.close();

        //2. Calculate global variance
        double temp = 0;
        // double square = 0;
        double gVariance = 0;
        // Map<Integer, Double> gVariance = new HashMap<>();

        for (int trialNum = 0; trialNum < trials; trialNum++) {
            //System.out.println(inputEstimatedCountsPath + " Reading: " + trialNum);

            BufferedReader brEstiCounts = new BufferedReader(
                    new FileReader(inputEstimatedCountsPath + "/global" + trialNum + ".txt"));
            //brEstiCounts.readLine();//The 1'st row is not used.

            line = brEstiCounts.readLine();
            double estiGlobalCounts = Double.valueOf(line);
            temp = realGlobalCounts - estiGlobalCounts;
            gVariance += Math.pow(temp, 2);
            brEstiCounts.close();
        }
        //3. Outputglobal variance
        BufferedWriter bwVariance = new BufferedWriter(new FileWriter(outputGlobalVariance));
        bwVariance.write(String.format("%.6f", gVariance / trials));
        System.out.println(String.format("%.6f", gVariance / trials));
        bwVariance.newLine();
        bwVariance.close();

        System.out.println("Consuming: " + (System.currentTimeMillis() - startTime) / 1000 + " s.");
        System.out.println("Done. ");
    }

    private static void printError() {
        System.err.println(
                "Usage: 0.trials | 1.inputEstimatedCountsPath | 2.inputRealCounts | 3.outputGlobalVariance ");
    }
}
