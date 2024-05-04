import java.io.*;
import java.util.Map;

class Common {

    public static int[] parseEdge(String line, String delim) {

        String[] tokens = line.split(delim);
        int src = Integer.valueOf(tokens[0]);
        int dst = Integer.valueOf(tokens[1]);
        int sign = Integer.valueOf(tokens[2]);

        return new int[]{src, dst, sign};
    }

    public static void runBatch(ThinkD module, String inputPath, String delim) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(inputPath));

        while(true) {

            final String line = br.readLine();
            if(line == null) {
                break;
            }

            int[] edge = parseEdge(line, delim);
            if(edge[2] > 0) {
                module.processAddition(edge[0], edge[1]);
            }
            else {
                module.processDeletion(edge[0], edge[1]);
            }
        }

        br.close();
    }

    public static void writeOutputs(final ThinkD module, final String outputPath, final int trialNum) throws IOException {

        System.out.println("writing outputs...");

        File dir = new File(outputPath);
        try{
            dir.mkdir();
        }
        catch(Exception e){

        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath + "/global" + trialNum + ".txt"));
        bw.write("Estimated Global Triangle Count");
        bw.newLine();
        bw.write(String.valueOf(module.getGlobalTriangle()));
        bw.newLine();
        
        bw.write("Found Global Triangle Count");
        bw.newLine();
        bw.write(String.valueOf(module.getFindTriangleNum()));
        bw.newLine();
        bw.close();

        bw = new BufferedWriter(new FileWriter(outputPath + "/local" + trialNum + ".txt"));
        Map<Integer, Double> localCounts = module.getLocalTriangle();
        bw.write("Node Id"+"\t"+"Estimated Local Triangle Count");
        bw.newLine();
        for(int node : localCounts.keySet()) {
            bw.write(node+"\t"+localCounts.get(node));
            bw.newLine();
        }
        bw.close();

        System.out.println("done.");

    }
}