#ifndef _WORKER_HPP_
#define _WORKER_HPP_

#include "source.hpp"
#include <unordered_map>
#include <set>
#include <stdlib.h> /* srand, rand */
#include <random>
#include <vector>
#include "time.h"

class Worker
{
    _PRIVATE :

        const int k; // maximum number of edges that can be stored
    int totalSpace;
    double rate;
    std::vector<Edge> samples; // sampled edges
    long n;                    // maximum number of edges sent to so far with doStore=1; n<= k/rate; extreme condition n no limit.
    long tn;                   // total n

    int curRound;
    int totalRound;
    int perK;                                             // standard memory budget in smapking pool
    std::unordered_map<VID, std::set<long>> edgeLocation; // record the sampling location of the current edge
    long key;

    double globalCnt;                                       // global triangle count
    std::unordered_map<VID, float> nodeToCnt;               // local triangle count
    std::unordered_map<VID, std::set<VID>> nodeToNeighbors; // sampled graph

    std::default_random_engine generator; // random real number generator
    std::uniform_real_distribution<double> distribution;

public:
    Worker(int k, int totalSpace, double rate, unsigned int seed);

    void updateCnt(const Edge &iEdge);

    int deleteEdge();

    // Independent part
    void processEdge(const Edge &iEdge);
    void processEdgeWithoutSampling(const Edge &iEdge);
    double getGlobalCnt();
    std::unordered_map<VID, float> &getLocalCnt();
};

#endif // #ifndef _WORKER_HPP_