package data_analysis;
import java.io.*;
import java.util.Map;

public class avg_local_triangle {

    public static void main(String[] args) throws IOException{
        
        if(args.length < 5){
            printError();
            System.exit(-1);
        }
        final String fNodeID = args[0];
        System.out.println("fNodeID: " + fNodeID);
        final String inputPath = args[1];
        System.out.println("inputPath: " + inputPath);
        final int NUM = Integer.valueOf(args[2]);
        System.out.println("NUM: " + NUM);
        final String outputFile = args[3];
        System.out.println("outputFile: " + outputFile);
        final String timeFile = args[4];
        System.out.println("timeFile: " + timeFile);

        int nodeTotalNum = 317080; //dblp
        long startTime = System.currentTimeMillis();

        int[] nodeID = new int[nodeTotalNum];
        double[] avgNum = new double[nodeTotalNum];
        int i = 0;

        BufferedReader brNodeID = new BufferedReader(new FileReader(fNodeID));
        brNodeID.readLine();
        for(i = 0; i < nodeTotalNum; i++){
            String line = brNodeID.readLine();
            nodeID[i] = Integer.valueOf(line.toString());
        }

        for(int trialNum = 0; trialNum < NUM; trialNum++){
            System.out.println(inputPath + " Reading: " + trialNum); 
         
            BufferedReader br = new BufferedReader(new FileReader(inputPath + "/local" + trialNum + ".txt"));
            String line1 = br.readLine();//The 1'st row is not used.
            //System.out.println("local" + trialNum + ": " + trialNum);
            while(true){//calculate the triangle sum of the same node, Num
                String line = br.readLine();
                if(line == null){
                    break;
                }
                String[] tokens = line.split("\t");

                int id = Integer.valueOf(tokens[0]);
                double num = Double.valueOf(tokens[1]);
                for(i = 0; i < nodeTotalNum; i++){
                    if(id == nodeID[i]){
                        avgNum[i] += num;
                        break;
                    }
                }
            }
        }
            
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        bw.write("average local triangles for every node (node_id, number)");
        bw.newLine();
        for(i = 0; i < nodeTotalNum; i++){//write average local triangle number for every node, Num
            avgNum[i] = avgNum[i] / (NUM * 1.0);
            bw.write(String.valueOf(nodeID[i]) +"\t"+ String.valueOf(avgNum[i]));
            bw.newLine();
        }
        bw.close();
           
        BufferedWriter bwt = new BufferedWriter(new FileWriter(timeFile));
        bwt.write("average local triangles for every node (node_id, number)");
        bwt.newLine();
        bwt.write("Consuming: " + String.valueOf((System.currentTimeMillis() - startTime)/1000) + " s.");
        bwt.newLine();
        bwt.close();
        
        System.out.println("Consuming: " + (System.currentTimeMillis() - startTime)/1000 + " s.");
        System.out.println("Done. ");
    }

    private static void printError() {
        System.err.println("Usage: fNodeID inputPath TrialNum outputFile timeFile");
    }
}