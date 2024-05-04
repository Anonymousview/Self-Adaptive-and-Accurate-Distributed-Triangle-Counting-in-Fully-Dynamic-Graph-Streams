import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.Random;

public class MascotFd extends Mascot {

    private Int2ObjectOpenHashMap<IntOpenHashSet> srcToDsts = new Int2ObjectOpenHashMap(); // graph composed of the sampled edges
    private Int2DoubleOpenHashMap nodeToTriangles = new Int2DoubleOpenHashMap(); // local triangle counts
    private double globalTriangle = 0; // global triangle count

    // private long s = 0; // number of current samples
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
    public MascotFd(final int memoryBudget, final double sampleProbability, final int seed) {
        this(memoryBudget, sampleProbability, seed, true);
    }

    public MascotFd(final int memoryBudget, final double sampleProbability, final int seed, final boolean lowerBound) {
        random = new Random(seed);
        this.k = memoryBudget;
        this.p = sampleProbability;
        samples = new int[2][this.k];
        nodeToTriangles.defaultReturnValue(0);
        this.lowerBound = lowerBound;
    }

    @Override
    public void processAddition(final int src, final int dst) {
        processEdge(src, dst, true);
    }

    @Override
    public void processDeletion(final int src, final int dst) {
        processEdge(src, dst, false);
    }

    public void processEdge(int src, int dst, boolean add) {
        if (src == dst) { //ignore self loop
            return;
        }

        if (src > dst) {
            int temp = src;
            src = dst;
            dst = temp;
        }

        count(src, dst, add); //count the added or deleted triangles

        if (add == false) { //deletion
            deleteEdge(src, dst);
        } else if (random.nextDouble() < p) {
            addEdge(src, dst);
            // s++; // the sum of sampled edges
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
        long key = ((long) src * Integer.MAX_VALUE) + dst;

        edgeToIndex.put(key, sampleNum);
        if (!srcToDsts.containsKey(src)) {
            srcToDsts.put(src, new IntOpenHashSet());
        }
        srcToDsts.get(src).add(dst);

        if (!srcToDsts.containsKey(dst)) {
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

        long key = ((long) src * Integer.MAX_VALUE) + dst;
        if (edgeToIndex.containsKey(key)) { //要删除的边，存在
            int index = edgeToIndex.remove(key);

            srcToDsts.get(src).remove(dst);
            if (srcToDsts.get(src).isEmpty()) {
                srcToDsts.remove(src);
            }

            srcToDsts.get(dst).remove(src);
            if (srcToDsts.get(dst).isEmpty()) {
                srcToDsts.remove(dst);
            }
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
    private void count(int src, int dst, boolean add) {

        // if this edge has a new node, there cannot be any triangles
        if (!srcToDsts.containsKey(src) || !srcToDsts.containsKey(dst)) {
            return;
        }

        IntOpenHashSet srcSet = srcToDsts.get(src);
        IntOpenHashSet dstSet = srcToDsts.get(dst);

        if (srcSet.size() > dstSet.size()) {
            IntOpenHashSet temp = srcSet;
            srcSet = dstSet;
            dstSet = temp;
        }

        double weight = 1.0 / (p * p);
        if (add == false) {
            weight = -1 * weight;
        }

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
