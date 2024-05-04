import java.io.*;
import java.util.Map;
import java.util.HashMap;

/*
 *  Calculate count_pearson_coefficient for n trials
 */

public class count_pearson_coefficient {
    public static void main(String[] args) throws IOException {

        if (args.length < 7) {
            printError();
            System.exit(-1);
        }

        final int numNodes = Integer.valueOf(args[0]);
        System.out.println("numNodes: " + numNodes);
        final int trials = Integer.valueOf(args[1]);
        System.out.println("trials: " + trials);
        final String inputNodeSet = args[2];
        System.out.println("inputNodeSet: " + inputNodeSet);
        final String inputEstimatedCountsPath = args[3];
        System.out.println("inputEstimatedCountsPath: " + inputEstimatedCountsPath);
        final String inputRealCounts = args[4];
        System.out.println("inputRealCounts: " + inputRealCounts);
        final String outputPearson = args[5];
        System.out.println("outputPearson: " + outputPearson);
        final String outputTime = args[6];
        System.out.println("outputTime: " + outputTime);

        long startTime = System.currentTimeMillis();

        //1. Read real local counts and calculate the y(mean)
        Map<Integer, Double> realLocalCountSet = new HashMap<>();

        BufferedReader brRealCounts = new BufferedReader(new FileReader(inputRealCounts));
        brRealCounts.readLine();//The 1'st row is not used.
        double meanRealCounts = 0;
        while (true) {//calculate the triangle sum of the same node, Num
            String line = brRealCounts.readLine();
            if (line == null) {
                break;
            }
            String[] tokens = line.split("\t");
            int id = Integer.valueOf(tokens[0]);
            double num = Double.valueOf(tokens[1]);
            realLocalCountSet.put(id, num);
            meanRealCounts += num;
        }
        meanRealCounts /= numNodes;
        brRealCounts.close();

        //TEST
        // System.out.println("meanRealCounts: " + meanRealCounts);

        //2. Calculate x(mean)
        double[] meanEstiCouts = new double[trials];
        for (int trialNum = 0; trialNum < trials; trialNum++) {
            BufferedReader brEstiCounts = new BufferedReader(
                    new FileReader(inputEstimatedCountsPath + "/local" + trialNum + ".txt"));
            meanEstiCouts[trialNum] = 0;
            while (true) {//calculate the triangle sum of the same node, Num
                String line = brEstiCounts.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split("\t");
                //int id = Integer.valueOf(tokens[0]);
                double num = Double.valueOf(tokens[1]);
                meanEstiCouts[trialNum] += num;
            }
            meanEstiCouts[trialNum] /= numNodes;
            brEstiCounts.close();
            //TEST
            // System.out.println("meanEstiCouts[" + trialNum + "]: " + meanEstiCouts[trialNum]);
        }

        //3. Calculate the various components of the Pearson coefficient
        double xMean = 0; // (xi-x)
        double yMean = 0; // (yi-y)
        double upSum = 0; //(xi-x)(yi-y)
        double leftSum = 0; //(xi-x)^2
        double rightSum = 0; //(yi-y)^2
        Map<Integer, Double> estiLocalCountSet = new HashMap<>();

        for (int trialNum = 0; trialNum < trials; trialNum++) {
            // System.out.println(inputEstimatedCountsPath + " Reading: " + trialNum);
            estiLocalCountSet.clear();
            //estimated local count set
            BufferedReader brEstiCounts = new BufferedReader(
                    new FileReader(inputEstimatedCountsPath + "/local" + trialNum + ".txt"));
            while (true) {//calculate the triangle sum of the same node, Num
                String line = brEstiCounts.readLine();
                if (line == null) {
                    break;
                }
                String[] tokens = line.split("\t");
                int id = Integer.valueOf(tokens[0]);
                double num = Double.valueOf(tokens[1]);
                estiLocalCountSet.put(id, num);
            }
            brEstiCounts.close();

            BufferedReader brNodeSet = new BufferedReader(new FileReader(inputNodeSet));

            while (true) {//calculate the triangle sum of the same node, Num
                String line = brNodeSet.readLine();
                if (line == null) {
                    break;
                }
                int id = Integer.valueOf(line);
                if (realLocalCountSet.containsKey(id)) {
                    // xMean = Math.abs(realLocalCountSet.get(id) - meanRealCounts);
                    xMean = realLocalCountSet.get(id) - meanRealCounts;
                    if (estiLocalCountSet.containsKey(id)) {
                        // yMean = Math.abs(estiLocalCountSet.get(id) - meanEstiCouts[trialNum]);
                        yMean = estiLocalCountSet.get(id) - meanEstiCouts[trialNum];
                    } else {
                        yMean = 0 - meanEstiCouts[trialNum];
                    }
                } else {
                    xMean = 0 - meanRealCounts;
                    if (estiLocalCountSet.containsKey(id)) {
                        // yMean = Math.abs(estiLocalCountSet.get(id) - meanEstiCouts[trialNum]);
                        yMean = estiLocalCountSet.get(id) - meanEstiCouts[trialNum];
                    } else {
                        yMean = 0 - meanEstiCouts[trialNum];
                    }
                }
                upSum += xMean * yMean;
                leftSum += Math.pow(xMean, 2);
                rightSum += Math.pow(yMean, 2);
            }
            //TEST
            // System.out.println("upSum: " + upSum);
            // System.out.println("leftSum: " + leftSum);
            // System.out.println("rightSum: " + rightSum);
            brNodeSet.close();
        }

        //4. Output count_pearson_coefficient
        double pearsonCoef = upSum / (Math.sqrt(leftSum) * Math.sqrt(rightSum));

        BufferedWriter bwVariance = new BufferedWriter(new FileWriter(outputPearson));
        bwVariance.write(String.format("%.6f", pearsonCoef));
        bwVariance.newLine();
        bwVariance.close();
        System.out.println(String.format("%.6f", pearsonCoef));

        //5. Measure the elapsed time
        BufferedWriter bwTime = new BufferedWriter(new FileWriter(outputTime));
        bwTime.write("Pearson Coefficient: ");
        bwTime.newLine();
        bwTime.write("Consuming: " + String.valueOf((System.currentTimeMillis() - startTime) / 1000) + " s.");
        bwTime.newLine();
        bwTime.close();
        System.out.println("Consuming: " + (System.currentTimeMillis() - startTime) / 1000 + " s.");
        System.out.println("Done. ");

    }

    private static void printError() {
        System.err.println(
                "Usage: 0.numNodes | 1.trials | 2.inputNodeSet | 3.inputEstimatedCountsPath | 4.inputRealCounts | 5.outputPearson | 6.outputTime");
    }
}