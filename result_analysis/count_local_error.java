import java.io.*;
import java.util.Map;
import java.util.HashMap;

/*
 *  Calculate count_local_variance for n trials
 */

public class count_local_error {

    public static void main(String[] args) throws IOException {

        if (args.length < 7) {
            printError();
            System.exit(-1);
        }

        final int numNode = Integer.valueOf(args[0]);
        System.out.println("numNode: " + numNode);
        final int trials = Integer.valueOf(args[1]);
        System.out.println("trials: " + trials);
        final String inputEstimatedCountsPath = args[2];
        System.out.println("inputEstimatedCountsPath: " + inputEstimatedCountsPath);
        final String inputRealCounts = args[3];
        System.out.println("inputRealCounts: " + inputRealCounts);
        final String outputAvgCounts = args[4];
        System.out.println("outputAvgCounts: " + outputAvgCounts);
        final String outputLocalError = args[5];
        System.out.println("outputLocalError: " + outputLocalError);
        final String outputTime = args[6];
        System.out.println("outputTime: " + outputTime);

        long startTime = System.currentTimeMillis();

        Map<Integer, Double> localCounts = new HashMap<>();

        //1. Calculate average local counts
        double temp = 0;
        for (int trialNum = 0; trialNum < trials; trialNum++) {
            //System.out.println(inputEstimatedCountsPath + " Reading: " + trialNum);

            BufferedReader brEstiCounts = new BufferedReader(
                    new FileReader(inputEstimatedCountsPath + "/local" + trialNum + ".txt"));
            brEstiCounts.readLine();//The 1'st row is not used.

            while (true) {//calculate the triangle sum of the same node, Num
                String line = brEstiCounts.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split("\t");
                int id = Integer.valueOf(tokens[0]);
                double num = Double.valueOf(tokens[1]);
                if (localCounts.containsKey(id)) {
                    temp = localCounts.get(id) + num;
                    localCounts.put(id, temp);
                } else {
                    localCounts.put(id, num);
                }
            }
            brEstiCounts.close();
        }

        //2. Calculate exact local error
        double sumLocalError = 0;
        BufferedReader brRealCounts = new BufferedReader(new FileReader(inputRealCounts));
        brRealCounts.readLine();// the 1'st row is not used.
        while (true) {//read real local tris to real2estimated[0][]
            String line = brRealCounts.readLine();
            if (line == null) {
                break;
            }
            String[] tokens = line.split("\t");
            int id = Integer.valueOf(tokens[0]);
            double num = Double.valueOf(tokens[1]);

            if (localCounts.containsKey(id)) {
                sumLocalError += Math.abs((num - localCounts.get(id) / trials) / (num + 1.0));
            } else {
                sumLocalError += Math.abs((num - 0) / (num + 1.0));
            }
        }
        brRealCounts.close();

        //3. Output local error
        BufferedWriter bwError = new BufferedWriter(new FileWriter(outputLocalError));
        bwError.write(String.format("%.6f", sumLocalError / numNode));
        bwError.newLine();
        bwError.close();
        System.out.println(String.format("%.6f", sumLocalError / numNode));

        //4. Measure the elapsed time
        BufferedWriter bwTime = new BufferedWriter(new FileWriter(outputTime));
        bwTime.write("average local triangles for every node (node_id, number)");
        bwTime.newLine();
        bwTime.write("Consuming: " + String.valueOf((System.currentTimeMillis() - startTime) / 1000) + " s.");
        bwTime.newLine();
        bwTime.close();
        System.out.println("Consuming: " + (System.currentTimeMillis() - startTime) / 1000 + " s.");
        System.out.println("Done. ");
    }

    private static void printError() {
        System.err.println(
                "Usage: 0.numNode | 1.trials | 2.inputEstimatedCountsPath | 3.inputRealCounts | 4.outputAvgCounts | 5.outputLocalError | 6.outputTime");
    }
}
