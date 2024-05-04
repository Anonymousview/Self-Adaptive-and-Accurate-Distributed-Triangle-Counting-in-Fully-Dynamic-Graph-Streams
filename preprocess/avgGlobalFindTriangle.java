import java.io.*;
import java.util.Map;

public class avgGlobalFindTriangle {

    public static void main(String[] args) throws IOException{
        
        if(args.length < 2){
            printError();
            System.exit(-1);
        }
        final String inputPath = args[0];
        System.out.println("inputPath: " + inputPath);
        final int NUM = Integer.valueOf(args[1]);
        System.out.println("NUM: " + NUM);

        double globalTriNum = 0.0;
        double real = 2224385.0; //dblp
        long findTriNum = 0;

        for(int trialNum = 0; trialNum < NUM; trialNum++){
         
            BufferedReader br = new BufferedReader(new FileReader(inputPath + "/global" + trialNum + ".txt"));
            String line1 = br.readLine();//estimate triangles
            String line2 = br.readLine();
            String line3 = br.readLine();//find triangles
            String line4 = br.readLine();

            double gNum = Double.valueOf(line2.toString());
            long fNum = Long.valueOf(line4.toString());
            globalTriNum = globalTriNum + gNum;
            findTriNum = findTriNum + fNum;
           
            System.out.println("[" + trialNum + "]-> globalSum: " + globalTriNum + "; gNum: " + gNum);
            System.out.println("[" + trialNum + "]-> findSum: " + findTriNum + "; fNum: " + fNum);
        }

        double gavg = globalTriNum / NUM; //global avg
        long favg = findTriNum / NUM; //find avg 

        double gError = (Math.abs(real - gavg)) / (real + 1.0);

        System.out.println("avg(estimate): " + gavg + "; avg-real: " + (gavg-real) + "; global-error: " + gError);
        System.out.println("find --> avg: " + favg);

    }

    private static void printError() {
        System.err.println("Usage: inputPath TrialNum");
    }
}