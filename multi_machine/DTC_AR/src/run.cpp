#include "run.hpp"
#include <iomanip> //by xw

double run_mpi(const char *filename, MPIIO &hIO, int workerNum, const Method_t method, int memSize, int totalSpace, double rate, int lenBuf, double tolerance, unsigned int seed, std::vector<float> &oLocalCnt, double &srcCompCost, double &workerCompCostMax, double &workerCompCostSum)
{
    // Computational Cost
    clock_t begin = clock();

    hIO.init(lenBuf, workerNum);

    // Sourcce init
    if (hIO.isMaster()) {
        Source source(workerNum, method, tolerance);
        Edge edge;
        MID dst1(0);
        MID dst2(0);
        bool isBroadCast;
        EdgeParser parser(filename);

        while (parser.getEdge(edge)) { // Stream edges
            if (edge.src != edge.dst) {
                isBroadCast = source.processEdge(edge, dst1, dst2);

                if (!isBroadCast) { // dst1 == dst2
                    hIO.sendEdge(edge, dst1);
                }
                else { // dst != dst2
                    hIO.bCastEdge(edge, dst1, dst2);
                }
            }
        }

        hIO.sendEndSignal();

        // Gather results from curWorkers
        double globalCnt = 0;

        // communication cost for gather
        hIO.recvCnt(source.getMaxVId(), globalCnt, oLocalCnt);

        hIO.recvTime(workerCompCostMax, workerCompCostSum);

        if (method == Method_t::NAIVE)
        {
            globalCnt = globalCnt / workerNum;
            for (auto it = oLocalCnt.begin(); it != oLocalCnt.end(); ++it)
            {
                *it = *it / workerNum;
            }
        }

        srcCompCost = (double(clock() - begin) - hIO.getIOCPUTime()) / CLOCKS_PER_SEC; // source cpu time

        return globalCnt;
    } else {
        Worker worker(memSize, totalSpace, rate, seed + hIO.getWorkerId());
        Edge edge;
        while (hIO.recvEdge(edge)) {
            if (edge.doStore || method == Method_t::NAIVE) {
                worker.processEdge(edge);
            }
            else {
                worker.processEdgeWithoutSampling(edge);
            }
        }

        // send counts to master
        hIO.sendCnt(worker.getGlobalCnt(), worker.getLocalCnt());

        double workerCompCost = (double(clock() - begin) - hIO.getIOCPUTime()) / CLOCKS_PER_SEC; // source cpu time

        hIO.sendTime(workerCompCost);
        return 0;
    }
}

double run_mpi_empty_worker(const char *filename, MPIIO &hIO, int workerNum, const Method_t method, int memSize, int lenBuf, double tolerance, unsigned int seed, std::vector<float> &oLocalCnt, double &srcCompCost, double &workerCompCostMax, double &workerCompCostSum)
{
    // Computational Cost
    clock_t begin = clock();

    hIO.init(lenBuf, workerNum);

    // Sourcce init
    if (hIO.isMaster())
    {
        Source source(workerNum, method, tolerance);
        Edge edge;
        MID dst1(0);
        MID dst2(0);
        bool isBroadCast;
        EdgeParser parser(filename);

        // Stream edges
        while (parser.getEdge(edge)) {
            if (edge.src != edge.dst) {
                isBroadCast = source.processEdge(edge, dst1, dst2);

                if (!isBroadCast) {
                    hIO.sendEdge(edge, dst1);
                }
                else {
                    hIO.bCastEdge(edge, dst1, dst2);
                }
            }
        }

        hIO.sendEndSignal();
        // Gather results from curWorkers
        double globalCnt = 0;

        // communication cost for gather
        hIO.recvCnt(source.getMaxVId(), globalCnt, oLocalCnt);

        // std::cout << source.getMaxVId() << "\t" << globalCnt << "\t" << oLocalCnt.size();

        hIO.recvTime(workerCompCostMax, workerCompCostSum);

        if (method == Method_t::NAIVE)
        {
            globalCnt = globalCnt / workerNum;
            for (auto it = oLocalCnt.begin(); it != oLocalCnt.end(); ++it)
            {
                *it = *it / workerNum;
            }
        }

        srcCompCost = (double(clock() - begin) - hIO.getIOCPUTime()) / CLOCKS_PER_SEC; // source cpu time

        return globalCnt;
    }
    else {
        Edge edge;
        while (hIO.recvEdge(edge)) {
            // do nothing
        }

        unordered_map<VID, float> localCnt;
        hIO.sendCnt(0, localCnt);

        double workerCompCost = (double(clock() - begin) - hIO.getIOCPUTime()) / CLOCKS_PER_SEC; // source cpu time

        hIO.sendTime(workerCompCost);
        return 0;
    }
}

void run_exp(const char *input, const char *outPath, MPIIO &hIO, int workerNum, Method_t method, int memSize, int totalSpace, double rate, int repeat, int bufLen, double tolerance)
{
    int seed = 0;

    struct timeval diff, startTV, endTV;

    if (hIO.isMaster()) {
        struct stat sb;
        if (stat(outPath, &sb) == 0) {
            if (S_ISDIR(sb.st_mode)) // TODO. directory is exists
                ;
            else if (S_ISREG(sb.st_mode)) // TODO. No directory but a regular file with same name
                ;
            else // TODO. handle undefined cases.
                ;
        } else {
            mkdir(outPath, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
        }
    }

    for (int i = 0; i < repeat; i++) {

        if (hIO.isMaster()) {

            gettimeofday(&startTV, NULL);

            std::vector<float> nodeToCnt;

            double srcCompCost = 0;
            double workerCompCostMax = 0;
            double workerCompCostSum = 0;

            double globalCnt = run_mpi(input, hIO, workerNum, method, memSize, totalSpace, rate, bufLen, tolerance, seed + repeat * workerNum * i, nodeToCnt, srcCompCost, workerCompCostMax, workerCompCostSum);

            gettimeofday(&endTV, NULL);

            timersub(&endTV, &startTV, &diff);

            double elapsedTime = diff.tv_sec * 1000 + diff.tv_usec / 1000;

            print_cnt(outPath, globalCnt, nodeToCnt, i, elapsedTime);
        } else {

            double srcCompCost = 0;
            double workerCompCostMax = 0;
            double workerCompCostSum = 0;
            std::vector<float> nodeToCnt;
            run_mpi(input, hIO, workerNum, method, memSize, totalSpace, rate, bufLen, tolerance, seed + repeat * workerNum * i, nodeToCnt, srcCompCost, workerCompCostMax, workerCompCostSum);
        }
    }
}

void print_cnt(const char *outPath, double globalCnt, const std::vector<float> &localCnt, int id, double elapsedTime)
{
    // Print global count
    std::ostringstream gCntFileName;
    gCntFileName << outPath << "/global" << id << ".txt";
    std::fstream gfp;
    gfp.open(gCntFileName.str(), std::fstream::out | std::fstream::trunc);
    gfp << std::setprecision(std::numeric_limits<double>::max_digits10) << globalCnt << endl;
    gfp.close();

    // Print elapsed time
    std::ostringstream timeFileName;
    timeFileName << outPath << "/time" << id << ".txt";
    std::fstream tfp;
    tfp.open(timeFileName.str(), std::fstream::out | std::fstream::trunc);
    tfp << std::setprecision(std::numeric_limits<double>::max_digits10) << elapsedTime << endl;
    tfp.close();

    // Print local count
    std::ostringstream lCntFileName;
    lCntFileName << outPath << "/local" << id << ".txt";
    std::fstream lfp;
    lfp.open(lCntFileName.str(), std::fstream::out | std::fstream::trunc);

    for (int nid = 0; nid < localCnt.size(); nid++)
    {
        lfp << nid << "\t" << localCnt[nid] << endl;
    }
    lfp.close();
}
