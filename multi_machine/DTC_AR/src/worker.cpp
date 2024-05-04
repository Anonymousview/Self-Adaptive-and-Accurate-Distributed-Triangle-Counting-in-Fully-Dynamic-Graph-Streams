#include "worker.hpp"

Worker::Worker(int k, int totalSpace, double rate, unsigned int seed) : k(k), totalSpace(totalSpace), rate(rate), n(0), tn(0), globalCnt(0), generator(seed), distribution(0.0, 1.0)
{
    srand(seed + time(NULL));
    samples.reserve(k);
    curRound = 0;
    totalRound = totalSpace / k;
    perK = (int)(k / rate);

    for (int i = 0; i <= totalRound; i++) {
        edgeLocation[i] = std::set<long>();
    }
};

void Worker::updateCnt(const Edge &iEdge)
{

    VID src = iEdge.src;
    VID dst = iEdge.dst;

    if (nodeToNeighbors.find(src) == nodeToNeighbors.end() || nodeToNeighbors.find(dst) == nodeToNeighbors.end()) {
        return;
    }

    std::set<VID> &srcMap = nodeToNeighbors[src];
    std::set<VID> &dstMap = nodeToNeighbors[dst];

    double countSum = 0;
    std::set<VID>::iterator srcIt;
    long key1, key2;
    for (srcIt = srcMap.begin(); srcIt != srcMap.end(); srcIt++) {
        VID neighbor = *srcIt;
        if (dstMap.find(neighbor) != dstMap.end()) {
            if (src < neighbor) {
                key1 = ((long)src * INT32_MAX) + neighbor;
            }
            else {
                key1 = ((long)neighbor * INT32_MAX) + src;
            }

            if (dst < neighbor) {
                key2 = ((long)dst * INT32_MAX) + neighbor;
            }
            else {
                key2 = ((long)neighbor * INT32_MAX) + dst;
            }

            int locate1 = 0;
            int locate2 = 0;
            double prob;

            for (int i = 0; i <= curRound; i++) {
                if (edgeLocation[i].find(key1) != edgeLocation[i].end()) {
                    locate1 = i;
                    break;
                }
            }
            for (int i = 0; i <= curRound; i++) {
                if (edgeLocation[i].find(key2) != edgeLocation[i].end()) {
                    locate2 = i;
                    break;
                }
            }

            if (locate1 == locate2) {
                if (locate1 > 0) {
                    prob = rate * (k - 1) / (perK - 1);
                }
                else {
                    double curSampleNum = k >= n ? n : k;
                    prob = (curSampleNum / n * (curSampleNum - 1) / (n - 1));
                }
            }
            else {
                if (locate1 > 0 && locate2 > 0) {
                    prob = rate * rate;
                }
                else {
                    double curSampleNum = k >= n ? n : k;
                    prob = rate * (curSampleNum / n);
                }
            }

            double count = 1.0 / prob;
            countSum += count;

            if (nodeToCnt.find(neighbor) == nodeToCnt.end()) { // local triangle counting for common neighbor
                nodeToCnt[neighbor] = (float)count;
            }
            else {
                nodeToCnt[neighbor] += (float)count;
            }
        }
    }

    if (countSum > 0)
    {
        if (nodeToCnt.find(src) == nodeToCnt.end()) { // local triangle counting for src
            nodeToCnt[src] = (float)countSum;
        }
        else {
            nodeToCnt[src] += (float)countSum;
        }

        if (nodeToCnt.find(dst) == nodeToCnt.end()) { // local triangle counting for dts
            nodeToCnt[dst] = (float)countSum;
        }
        else {
            nodeToCnt[dst] += (float)countSum;
        }

        globalCnt += countSum; // global triangle counting
    }

    return;
}

int Worker::deleteEdge()
{
    int index = rand() % k;
    Edge removedEdge = samples[index];
    nodeToNeighbors[removedEdge.src].erase(removedEdge.dst);
    nodeToNeighbors[removedEdge.dst].erase(removedEdge.src);

    key = ((long)removedEdge.src * INT32_MAX) + removedEdge.dst;
    edgeLocation[0].erase(key);

    return index;
}

void Worker::processEdge(const Edge &iEdge)
{

    VID src = iEdge.src;
    VID dst = iEdge.dst;

    if (src == dst) { // ignore self loop
        return;
    }

    updateCnt(iEdge); // count triangles involved

    bool isSampled = false;
    if (n < k) { // always sample
        isSampled = true;
    } else {
        if (distribution(generator) < k / (1.0 + n))  {
            isSampled = true;
        }
    }

    if (isSampled) {
        if (n < k) {
            samples.push_back(Edge(iEdge));
        } else {
            int index = deleteEdge();
            samples[index] = iEdge;
        }

        if (nodeToNeighbors.find(src) == nodeToNeighbors.end()) {
            nodeToNeighbors[src] = std::set<VID>();
        }
        nodeToNeighbors[src].insert(dst);

        if (nodeToNeighbors.find(dst) == nodeToNeighbors.end()) {
            nodeToNeighbors[dst] = std::set<VID>();
        }
        nodeToNeighbors[dst].insert(src);

        key = ((long)src * INT32_MAX) + dst;
        edgeLocation[0].insert(key);
    }

    n++;

    // Transfer the current memory budget to the memory budget pool
    if ((n == perK) && (curRound < totalRound)) { 
        tn += n;
        curRound++;
        edgeLocation[curRound] = edgeLocation[0];
        edgeLocation[0].clear();

        samples.clear();
        n = 0;
    }

    return;
}

void Worker::processEdgeWithoutSampling(const Edge &iEdge)
{
    if (iEdge.src == iEdge.dst) { // ignore self loop
        return;
    }

    updateCnt(iEdge); // count triangles involved
}

double Worker::getGlobalCnt()
{
    return globalCnt;
}

std::unordered_map<VID, float> &Worker::getLocalCnt()
{
    return nodeToCnt;
}