#include "worker.hpp"

Worker::Worker(int k, unsigned int seed): k(k), n(0), globalCnt(0), generator(seed), distribution(0.0, 1.0) {
    srand(seed+time(NULL));
    samples.reserve(k);
    findTriangle = 0;
};

int Worker::getFindTriangle(){
    return findTriangle;
}

void Worker::updateCnt(const Edge &iEdge){

    VID src = iEdge.src;
    VID dst = iEdge.dst;
    bool add = iEdge.add;
    
    if(nodeToNeighbors.find(src) == nodeToNeighbors.end() || nodeToNeighbors.find(dst) == nodeToNeighbors.end()) {
        return;
    }

    std::set<VID> &srcMap = nodeToNeighbors[src];
    std::set<VID> &dstMap = nodeToNeighbors[dst];

    double count = (n + nb + ng + 0.0) / k * (n + nb + ng - 1.0) / (k - 1.0);
    count = count > 1 ? count : 1;

    double countSum = 0;
    std::set<VID>::iterator srcIt;

    if(add){// process the addition
        for (srcIt = srcMap.begin(); srcIt != srcMap.end(); srcIt++) {
            VID neighbor = *srcIt;
            if (dstMap.find(neighbor) != dstMap.end()) {
                findTriangle++;

                countSum += count;
                if (nodeToCnt.find(neighbor) == nodeToCnt.end()) {
                    nodeToCnt[neighbor] = (float)count;
                } else {
                    nodeToCnt[neighbor] += (float)count;
                }
                // //TEST
                // if(test < 1){
                //     std::cout.flags(ios::fixed);
                //     std::cout.precision(8);
                //     std::cout<<"("<<src<<", "<<dst<<", "<<neighbor<<") n:"<< n <<", k:" << k <<", nb:" << nb <<", ng:" << ng << ", count:"<< count<<endl;
                //     test++;
                // }
            }
        }

        if(countSum > 0) {
            if (nodeToCnt.find(src) == nodeToCnt.end()) {
                nodeToCnt[src] = (float)countSum;
            } else {
                nodeToCnt[src] += (float)countSum;
            }

            if (nodeToCnt.find(dst) == nodeToCnt.end()) {
                nodeToCnt[dst] = (float)countSum;
            } else {
                nodeToCnt[dst] += (float)countSum;
            }

            globalCnt += countSum;
        }
    }else{ //process the deletion
        for (srcIt = srcMap.begin(); srcIt != srcMap.end(); srcIt++) {
            VID neighbor = *srcIt;
            if (dstMap.find(neighbor) != dstMap.end()) {
                findTriangle--;

                countSum += count;
                nodeToCnt[neighbor] -= (float)count;
                if(nodeToCnt[neighbor] < 0){//lower bounding
                    nodeToCnt[neighbor] = 0;
                }
            }
        }

        if(countSum > 0) {
            nodeToCnt[src] -= (float)countSum;
            if(nodeToCnt[src] < 0){
                nodeToCnt[src] = 0;
            }
            
            nodeToCnt[dst] -= (float)countSum;
            if(nodeToCnt[dst] < 0){
                nodeToCnt[dst] = 0;
            }
            
            globalCnt -= countSum;
            if(globalCnt < 0){
                globalCnt = 0;
            }
        }
    }
    return;
}

int Worker::deleteEdge() {
    int index = rand() % k;
    Edge removedEdge = samples[index];

    nodeToNeighbors[removedEdge.src].erase(removedEdge.dst);
    if(nodeToNeighbors[removedEdge.src].size() == 0){
        nodeToNeighbors.erase(removedEdge.src);
    }
    nodeToNeighbors[removedEdge.dst].erase(removedEdge.src);
    if(nodeToNeighbors[removedEdge.dst].size() == 0){
        nodeToNeighbors.erase(removedEdge.dst);
    }
    return index;
}

void Worker::processEdge(const Edge &iEdge){
    VID src = iEdge.src;
    VID dst = iEdge.dst;
    if(src == dst) { //ignore self loop
        return;
    }

    updateCnt(iEdge); //count triangles involved

    if(iEdge.add){
        bool isSampled = false;
        if(nb + ng == 0){
            if(n < k) { // always sample
                isSampled = true;
            }
            else if(distribution(generator) < k / (n + 1.0)) {
                isSampled = true;
            }
        }else if(distribution(generator) < nb / (nb + ng + 0.0)){
            isSampled = true;
            nb--;
        }else{
            ng--;
        }

        if(isSampled) {
            if(samples.size() < k) {
                samples.push_back(Edge(iEdge));
            }else {
                int index = deleteEdge();
                samples[index] = iEdge;
            }

            if(nodeToNeighbors.find(src)==nodeToNeighbors.end()) {
                nodeToNeighbors[src] = std::set<VID>();
            }
            nodeToNeighbors[src].insert(dst);

            if(nodeToNeighbors.find(dst)==nodeToNeighbors.end()) {
                nodeToNeighbors[dst] = std::set<VID>();
            }
            nodeToNeighbors[dst].insert(src);
        }
    }else{
        int idx = 0;
        bool flag = false;
        for(idx = 0; idx < samples.size(); idx++){
            if((samples[idx].src == src && samples[idx].dst == dst) || (samples[idx].src == dst && samples[idx].dst == src)){
                if(idx < samples.size() -1){
                    samples[idx] = samples[samples.size()-1];
                }
                samples.pop_back();

                if(nodeToNeighbors[src].size() == 1){
                    nodeToNeighbors.erase(src);
                }else{
                    nodeToNeighbors[src].erase(dst);
                }

                if(nodeToNeighbors[dst].size() == 1){
                    nodeToNeighbors.erase(dst);
                }else{
                    nodeToNeighbors[dst].erase(src);
                }
                
                flag = true;
                nb++;
                break;
            }
        }
        if(flag == false){
            ng++;
        }
    }

    if(iEdge.add){
        n++;
    }else{
        n--;
    }
    return;
}

void Worker::processEdgeWithoutSampling(const Edge &iEdge)
{
    if(iEdge.src == iEdge.dst) { //ignore self loop
        return;
    }
    updateCnt(iEdge); //count triangles involved
}

double  Worker::getGlobalCnt()
{
    return globalCnt;
}

std::unordered_map<VID, float> & Worker::getLocalCnt()
{
    return nodeToCnt;
}