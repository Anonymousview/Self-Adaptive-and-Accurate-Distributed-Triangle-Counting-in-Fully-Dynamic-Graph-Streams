import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

public abstract class Triest {

    /**
     * Process the addition of an edge
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     */
    public abstract void processAddition(int src, int dst);

    /**
     * Process the deletion of an edge
     * @param src source node of the given edge
     * @param dst destination node of the given edge
     */
    public abstract void processDeletion(int src, int dst);

    /**
     * Get estimated global triangle count
     * @return estimate of global triangle count
     */
    public abstract double getGlobalTriangle();

    /**
     * Get estimated local triangle counts
     * @return map from nodes to counts
     */
    public abstract Int2DoubleMap getLocalTriangle();

}
