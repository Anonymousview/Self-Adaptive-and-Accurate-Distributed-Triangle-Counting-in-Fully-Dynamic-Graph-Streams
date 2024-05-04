import java.io.IOException;
import java.util.Random;

public class BatchAcc {

    public static void main(String[] args) throws IOException {

        if(args.length < 4) {
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
       
        long findTriangleSum = 0;
        long start = System.currentTimeMillis();

        for(int trial = 0; trial < numberOfTrials; trial++) {
            final ThinkDAcc module = new ThinkDAcc(memroyBudget, new Random().nextInt());

            Common.runBatch(module, inputPath, "\t");
        }
        System.out.println("Consuming " + (System.currentTimeMillis() - start)/1000 + "s. " + "Average find " + (findTriangleSum /(long)numberOfTrials));
        return;
    }

    private static void printError() {
        System.err.println("Usage: run_acc.sh input_path output_path memory_budget number_of_trials");
        System.err.println("- memory_budget should be an integer greater than or equal to 2.");
    }
}
