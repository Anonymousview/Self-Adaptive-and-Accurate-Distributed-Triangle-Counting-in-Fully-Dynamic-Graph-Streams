import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length < 4) {
            printError();
            System.exit(-1);
        }

        final String inputPath = args[0];
        System.out.println("input_path: " + inputPath);
        final String outputPath = args[1];
        System.out.println("output_path: " + outputPath);
        final int memroyBudget = Integer.valueOf(args[2]);
        System.out.println("memory_budget: " + memroyBudget);
        final int numberOfTrials = Integer.valueOf(args[3]);
        System.out.println("number_of_trials: " + numberOfTrials);

        long startTime, duringTime;
        for (int trial = 0; trial < numberOfTrials; trial++) {
            startTime = System.currentTimeMillis();
            final TriestFd module = new TriestFd(memroyBudget, new Random().nextInt());

            System.out.println("start running Triest (Accurate Ver.)...(" + trial + "/" + numberOfTrials + ")");
            Common.runBatch(module, inputPath, "\t");

            duringTime = System.currentTimeMillis() - startTime;
            Common.writeOutputs(module, outputPath, trial, duringTime);
        }
        return;
    }

    private static void printError() {
        System.err.println("Usage: run_acc.sh input_path output_path memory_budget number_of_trials");
        System.err.println("- memory_budget should be an integer greater than or equal to 2.");
    }
}
