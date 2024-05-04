import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.Random;

public class MascotBase extends Mascot {

    private Int2ObjectOpenHashMap<IntOpenHashSet> srcToDsts = new Int2ObjectOpenHashMap(); // graph composed of the sampled edges
    private Int2DoubleOpenHashMap nodeToTriangles = new Int2DoubleOpenHashMap(); // local triangle counts
    private double globalTriangle = 0; // global triangle count

    private long s = 0; // number of current samples
    private int nb = 0; // number of uncompensated deletions
    private int ng = 0; // number of uncompensate deletions
    private long findTriangleNum = 0;

    private final int k; // maximum number of samples
    private final double p; //edge sampling probability
    private final int[][] samples; // sampled edges
    private final Long2IntOpenHashMap edgeToIndex = new Long2IntOpenHashMap(); // edge to the index of cell that the edge is stored in

    private final Random random;
    private final boolean lowerBound;

    /**
     *
     * @param memoryBudget maximum number of samples
     * @param seed random seed
     */
    public MascotBase(final int memoryBudget, final double sampleProbability, final int seed) {
        this(memoryBudget, sampleProbability, seed, true);
    }

    public MascotBase(final int memoryBudget, final double sampleProbability, final int seed, final boolean lowerBound) {
        random = new Random(seed);
        this.k = memoryBudget;
        this.p = sampleProbability;
        samples = new int[2][this.k];
        nodeToTriangles.defaultReturnValue(0);
        this.lowerBound = lowerBound;
    }

    @Override
    public void processAddition(final int src, final int dst) {
        processEdge(src, dst);
    }

    @Override
    public void processDeletion(final int src, final int dst) {
        processEdge(src, dst);
    }

    public void processEdge(int src, int dst) {
        if(src == dst) { //ignore self loop
            return;
        }

		if (src > dst) {
			int temp = src;
			src = dst;
			dst = temp;
		}

        count(src, dst);

        if(random.nextDouble() < p) {
            addEdge(src, dst);
            s++; // the sum of sampled edges
        }

        return;
    }

    /**
     * Store a sampled edge
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     */
    private void addEdge(int src, int dst) {

        int sampleNum = edgeToIndex.size();
        samples[0][sampleNum] = src;
        samples[1][sampleNum] = dst;
        long key = ((long)src * Integer.MAX_VALUE) + dst;

        edgeToIndex.put(key, sampleNum);
        if(!srcToDsts.containsKey(src)) {
            srcToDsts.put(src, new IntOpenHashSet());
        }
        srcToDsts.get(src).add(dst);

        if(!srcToDsts.containsKey(dst)) {
            srcToDsts.put(dst, new IntOpenHashSet());
        }
        srcToDsts.get(dst).add(src);
    }

    /**
     * Remove an edge from the samples
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     */
    private void deleteEdge(int src, int dst) {

        int sampleNum = edgeToIndex.size();
        long key = ((long)src * Integer.MAX_VALUE) + dst;
        int index = edgeToIndex.remove(key);

        IntOpenHashSet map = srcToDsts.get(src);
        map.remove(dst);
        if(map.isEmpty()) {
            srcToDsts.remove(src);
        }
        map = srcToDsts.get(dst);
        map.remove(src);
        if(map.isEmpty()) {
            srcToDsts.remove(dst);
        }

        if(index < sampleNum - 1) {//IF deleted edge in the sample
            int newSrc = samples[0][index] = samples[0][sampleNum - 1]; //将samples数组的靠后index覆盖靠前的要删除的index
            int newDst = samples[1][index] = samples[1][sampleNum - 1];
            long newKey = ((long) newSrc * Integer.MAX_VALUE) + newDst;
            edgeToIndex.put(newKey, index);
        }
    }

    @Override
    public double getGlobalTriangle() {
        return globalTriangle;
    }

    @Override
    public long getFindTriangleNum() {
        return this.findTriangleNum;
    }
    @Override
    public Int2DoubleOpenHashMap getLocalTriangle() {
        return nodeToTriangles;
    }

    /**
     * counts triangles with the given edge and update estimates
     * @param src the source node of the given edge
     * @param dst the destination node of the given edge
     * @param add true for addition and false for deletion
     */
    //private void count(int src, int dst, boolean add) {
    private void count(int src, int dst) {

        // if this edge has a new node, there cannot be any triangles
        if(!srcToDsts.containsKey(src) || !srcToDsts.containsKey(dst)) {
            return;
        }

        IntOpenHashSet srcSet = srcToDsts.get(src);
        IntOpenHashSet dstSet = srcToDsts.get(dst);

        if(srcSet.size() > dstSet.size()) {
            IntOpenHashSet temp = srcSet;
            srcSet = dstSet;
            dstSet = temp;
        }

        final double weight = 1.0/(p*p);

            double count = 0;
            for (int neighbor : srcSet) {
                if (dstSet.contains(neighbor)) {
                    count += 1;
                    nodeToTriangles.addTo(neighbor, weight);

                }
            }

            if (count > 0) {
                double weightSum = count * weight;
                nodeToTriangles.addTo(src, weightSum);
                nodeToTriangles.addTo(dst, weightSum);
                globalTriangle += weightSum;
                findTriangleNum += count;
            }
    }
}