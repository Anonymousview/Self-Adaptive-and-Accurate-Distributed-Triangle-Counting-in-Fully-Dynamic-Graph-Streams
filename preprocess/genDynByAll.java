import java.util.Random;
import java.io.*;
import java.util.Arrays;

/* 
    Based on the edge streams for addition only, create a fully dynamic graph stream that includes random edge deletions within the entire stream.
*/

public class genDynByAll {
    public static void main(String[] args) throws IOException {
        int numAdd=297456, numDel=59106;  //dyn = add + del, row number
        int numDyn = numAdd + numDel; 
        String inputFile = "out_example_graph.txt";
        String outputFile = "dyn_example_graph.txt";

        String[][] dynArray = new String[numDyn][3];
        String[][] addArray = new String[numAdd][2];
        String[][] delArray = new String[numDel][2];
        int[] insSequence = new int[numDel];
        //generate deleted number
        int[] delNums = dynamicEdge.randomNum(numDel, numAdd);
        //sort from small to large
        Arrays.sort(delNums);
        
        //TEST: delNums
        String outputDel = "out_del_array.txt";
        BufferedWriter bwDel = new BufferedWriter(new FileWriter(outputDel));
        for(int i = 0; i < numDel; i++){
            bwDel.write(String.valueOf(delNums[i]));
            bwDel.newLine();
        }
        bwDel.close();

        //1.addArray: read add_edge file from disk into memory
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        int iAdd = 0;
        while(true){
            String line = br.readLine();
            if(line == null){
                break;
            }
            String[] token = line.split("\t");
            addArray[iAdd][0] = token[0];
            //addArray[iAdd][0] = Integer.valueOf(token[0]);
            addArray[iAdd][1] = token[1];
            //System.out.println("addArray " + iAdd + "(" + addArray[iAdd][0] + ", " + addArray[iAdd][1] + ")");
            iAdd++;
        }
        br.close();
        
        //2.delArray
        Random r = new Random();
        for(int i=0; i<numDel; i++){ //TEST
            delArray[i][0] = addArray[delNums[i]][0];
            delArray[i][1] = addArray[delNums[i]][1];
            //delArray[i][2] = "-1";

            insSequence[i] = delNums[i] + r.nextInt(numAdd - delNums[i]);//*** determine the location of deleted edges
            //System.out.println("delArray "+ i + ": " + delNums[i] + ", " + insSequence[i] + "(" + delArray[i][0] +", " + delArray[i][1] + ")");
        }

        //3.dynArray
        int iDyn = 0, iDel = 0;
        for(iAdd = 0; iAdd < numAdd; iAdd++){
            dynArray[iDyn][0] = addArray[iAdd][0];  
            dynArray[iDyn][1] = addArray[iAdd][1];  
            dynArray[iDyn][2] = "1";  
            iDyn++;
            
            iDel = 0;
            while(iDel < numDel){//insert deleted edges
                if(insSequence[iDel] == iAdd){//insert deleted edges
                    dynArray[iDyn][0] = delArray[iDel][0];  
                    dynArray[iDyn][1] = delArray[iDel][1];  
                    dynArray[iDyn][2] = "-1";  
                    iDyn++;
                }
                iDel++;
            }
        }

        // Write into file
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        for(int i = 0; i < numDyn; i++){
            bw.write(dynArray[i][0]);
            bw.write("\t");
            bw.write(dynArray[i][1]);
            bw.write("\t");
            bw.write(dynArray[i][2]);
            bw.newLine();
        }
        bw.close();
        System.out.println("Done.");

    }
    public static int[] randomNum(int amount, int max){//generate amount unique numbers (0 ~ max-1)
        if(amount > max){
            throw new ArrayStoreException("Error!");
        }
        int[] array = new int[amount];
        for(int i = 0; i < array.length; i++){//initialize
            array[i] = -1;
        }
        Random rdm = new Random();
        int num;
        amount -= 1;
        while(amount >= 0){
            num = rdm.nextInt(max);
            if(exist(num, array, amount - 1)){
                continue;
            }
            array[amount] = num;
            amount--;
        }
        return array;
    }
    private static boolean exist(int num, int[] array, int seed){
        for(int i = array.length - 1; i > seed; i--){
            if(num == array[i]){
                return true;
            }
        }
        return false;
    }
}