import java.util.Random;
import java.io.*;
import java.util.Arrays;

/* 
    Based on the edge streams for addition only, create a fully dynamic graph stream that includes random edge deletions within each batch of the entire stream.
*/

public class genDynByBatch {
    public static void main(String[] args) throws IOException {
        if(args.length < 5){
            printError();
            System.exit(-1);
        }
        final String inputFile = args[0];
        System.out.println("inputFile: " + inputFile);
        final String outputFile = args[1];
        System.out.println("outputFile: " + outputFile);
        int numBatchSize = Integer.valueOf(args[2]); 
        System.out.println("numBatchSize: " + numBatchSize);
        final int numAdd = Integer.valueOf(args[3]); 
        System.out.println("numAdd: " + numAdd);
        final double rateDel = Double.valueOf(args[4]); 
        System.out.println("rateDel: " + rateDel);

        double numBatchTmp = Math.ceil((numAdd * 1.0) / numBatchSize);
        int numBatch = (int)numBatchTmp;
        assert numBatch > 0;
        
        double numBatchDelTmp = Math.ceil(numBatchSize * rateDel);
        int numBatchDel = (int)numBatchDelTmp;
        int numDyn = numBatchDel + numBatchSize; 
        
        int[][] dynArray = new int[numDyn][4];//(src, dst, add/delete, flag), flag: to generate dynamic edge streams.
        int[][] addArray = new int[numBatchSize][2];
        
        int startLine = 0, endLine = 0; // read inputFile by batch
        String line;
        Random ran = new Random();

        for(int k = 0; k < numBatch; k++){

            if(k < numBatch - 1){ // first (n-1) batch
                startLine = k * numBatchSize;
                endLine = (k + 1) * numBatchSize;
            }else{//the last batch
                startLine = k * numBatchSize;
                endLine = numAdd;
                numBatchSize = numAdd - startLine;

                numBatchDelTmp = Math.ceil(numBatchSize * rateDel);
                numBatchDel = (int)numBatchDelTmp;
                numDyn = numBatchDel + numBatchSize; 
            }

            // Generate deleted number 
            int[] delNums = genDynByBatch.randomNum(numBatchDel, numBatchSize);
            //sort from small to large
            Arrays.sort(delNums); // Ascending order sorting
        
            //1.addArray: read add_edge file from disk into memory
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            int indexA = 0;
            int size = 0;
            while(indexA < startLine) {
                br.readLine();
                indexA++;
            }
            //System.out.println("indexA: " + indexA);
            while((indexA < endLine) && (line = br.readLine()) != null){
                String[] token = line.split("\t");
                addArray[size][0] = Integer.valueOf(token[0]);
                addArray[size][1] = Integer.valueOf(token[1]);
                size++;
                indexA++;
            }
            br.close();

            //3.dynArray
            int iDyn = 0, iDel = 0, iAdd = 0;
            int cavity_residue = numDyn;
            int nxt, index, nxt_index;

            for(int i = 0; i < numDyn; i++){
                dynArray[i][3] = 0; //0 represents an empty slot, while 1 represents a filled slot
            }

            for(iAdd = 0; iAdd < numBatchSize; iAdd++){ //kernel code ***
                
                cavity_residue--; //Decreasing number of empty slots
                if(iAdd != delNums[iDel]){ //edge addition
                    //System.out.println("iAdd: " + iAdd + ", iDyn: " + iDyn + ", dynArray: " + dynArray[iDyn][3]);
                    while(dynArray[iDyn][3] != 0){
                        iDyn++;
                    }
                    dynArray[iDyn][0] = addArray[iAdd][0];  
                    dynArray[iDyn][1] = addArray[iAdd][1];  
                    dynArray[iDyn][2] = 1;  
                    dynArray[iDyn][3] = 1;  
                    iDyn++;
                }else{ //edge deletion
                    while(dynArray[iDyn][3] != 0){
                        iDyn++;
                    }
                    dynArray[iDyn][0] = addArray[iAdd][0]; // first add edge 
                    dynArray[iDyn][1] = addArray[iAdd][1];  
                    dynArray[iDyn][2] = 1;  
                    dynArray[iDyn][3] = 1;  
                    iDyn++;
                    
                    nxt = ran.nextInt(cavity_residue); //0 ~ cavity_residue-1
                    //System.out.println("cavity_residue: " + cavity_residue + "\t nxt: " + nxt);
                    index = iDyn;
                    nxt_index = 0;
                    while(nxt_index < nxt){ // Locate the position of the edge to be deleted
                        if(dynArray[index][3] != 0){
                            index++;
                        }else{
                            index++;
                            nxt_index++;
                        }
                    }
                        
                    while(dynArray[index][3] != 0){ // Confirm that the current position nxt_index is an empty slot
                        index++;
                    }
                    dynArray[index][0] = addArray[iAdd][0];  
                    dynArray[index][1] = addArray[iAdd][1];  
                    dynArray[index][2] = -1;
                    dynArray[index][3] = 1;
                    cavity_residue--; // Decreasing number of empty slots, edge deletion occupies 2 empty slots
                    
                    iDel++;
                    if(iDel == numBatchDel)
                        iDel = 0;
                }
            }
            // Write into file
            FileWriter bw = new FileWriter(outputFile, true);

            for(int i = 0; i < numDyn; i++){
                bw.write(Integer.toString(dynArray[i][0]));
                bw.write("\t");
                bw.write(Integer.toString(dynArray[i][1]));
                bw.write("\t");
                bw.write(Integer.toString(dynArray[i][2]));
                bw.write("\n");
                //System.out.println("dynArray " + i + "(" + dynArray[i][0] + ", " + dynArray[i][1] + ")");
            }
            bw.close();
        }

        System.out.println("Done.");
    }
    
    // Generate the index number for edge deletion
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
    private static void printError(){
        System.out.println("Usage: inputFile outputFile numBatchSize numAdd rateDel(0.2) ");
    }
}