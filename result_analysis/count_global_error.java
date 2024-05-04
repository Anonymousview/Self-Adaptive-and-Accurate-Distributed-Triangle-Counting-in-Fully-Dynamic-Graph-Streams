import java.io.*;
import java.util.Map;

public class count_global_error {

    public static void main(String[] args) throws IOException {

        if (args.length < 4) {
            printError();
            System.exit(-1);
        }
        final int trials = Integer.valueOf(args[0]);
        System.out.println("trials: " + trials);
        final String inputEstiGlobalCountsPath = args[1];
        System.out.println("inputEstiGlobalCountsPath: " + inputEstiGlobalCountsPath);
        final String inputRealGlobalCounts = args[2];
        System.out.println("inputRealGlobalCounts: " + inputRealGlobalCounts);
        final String outputGlobalError = args[3];
        System.out.println("outputGlobalError: " + outputGlobalError);

        double globalTriNum = 0;

        //1. Calculate average global counts
        for (int trialNum = 0; trialNum < trials; trialNum++) {
            BufferedReader br = new BufferedReader(
                    new FileReader(inputEstiGlobalCountsPath + "/global" + trialNum + ".txt"));
            String line = br.readLine();//estimate triangles

            double gNum = Double.valueOf(line.toString());
            globalTriNum = globalTriNum + gNum;

            //System.out.println("[" + trialNum + "]-> globalSum: " + globalTriNum + "; gNum: " + gNum);
            br.close();
        }

        //2. Read real global counts
        BufferedReader brRealCounts = new BufferedReader(new FileReader(inputRealGlobalCounts));
        brRealCounts.readLine();
        String gLine = brRealCounts.readLine();//real global triangles
        double real = Double.valueOf(gLine);

        //3. Calculate global error
        double gavg = globalTriNum / trials;//global avg
        System.out.println("real - gavg: " + (real - gavg));
        double gError = (Math.abs(real - gavg)) / (real + 1.0);
        System.out.println("gError: " + gError);

        //4. Write global error into file
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputGlobalError));
        bw.write(String.format("%.6f", gError));
        System.out.println(String.format("%.6f", gError));
        bw.newLine();
        bw.close();

    }

    private static void printError() {
        System.err.println(
                "Usage: 0.trials | 1.inputEstiGlobalCountsPath | 2.inputRealGlobalCounts | 3.outputGlobalError");
    }
}
