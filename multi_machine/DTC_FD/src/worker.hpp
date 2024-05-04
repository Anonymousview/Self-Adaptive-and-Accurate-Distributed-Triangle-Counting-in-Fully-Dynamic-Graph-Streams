#ifndef _WORKER_HPP_
#define _WORKER_HPP_

#include "source.hpp"
#include <unordered_map>
#include <set>
#include <stdlib.h>     /* srand, rand */
#include <random>
#include <vector>
#include "time.h"

class Worker {
_PRIVATE:
    int findTriangle;
	const int k; // maximum number of edges that can be stored
   // int k_real = 0;//real number of edges stored in memory budget
	std::vector<Edge> samples; // sampled edges
	long n; // maximum number of edges sent to so far with doStore=1
    int nb = 0;
    int ng = 0;


    int test = 0;//only for counting first triangle

	double globalCnt; // global triangle count
	std::unordered_map<VID, float> nodeToCnt; // local triangle count
	std::unordered_map<VID, std::set<VID>> nodeToNeighbors; // sampled graph


	std::default_random_engine generator; // random real number generator
	std::uniform_real_distribution<double> distribution;


public:

	Worker(int k, unsigned int seed);
	
    int getFindTriangle();

	void updateCnt(const Edge &iEdge);

	int deleteEdge();

	// Independent part
	void processEdge(const Edge &iEdge);
	void processEdgeWithoutSampling(const Edge &iEdge);
	double getGlobalCnt();
	std::unordered_map<VID, float> & getLocalCnt();
};



#endif // #ifndef _WORKER_HPP_
