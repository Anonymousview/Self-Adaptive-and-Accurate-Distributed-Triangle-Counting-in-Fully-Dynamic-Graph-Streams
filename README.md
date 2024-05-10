# DTC: Self-Adaptive and Accurate Distributed Triangle Counting in Fully Dynamic Graph Streams

## User Guide


### datasets

| Name |  #Nodes | #Edges| Description|
|:-----|:-----:|:-----:|:-----|
| Arxiv | 34,546 | 420,877 | Citation network  | 
| Facebook | 63,731 | 817,090 |  Social network | 
| Dblp | 317,080 | 1,049,866 |  Collaboration network  | 
| NotreDame | 325,729 | 1,090,108 |  Web graph | 
| BerkStan | 685,230 | 6,649,470 |  Web graph | 
| Youtube | 3,223,589 | 9,376,594 | Social network [[download]](https://socialnetworks.mpi-sws.org/data-imc2007.html) | 
| Skitter | 1,696,415 | 11,095,298 |  Internet graph [[download]](https://snap.stanford.edu/data/as-Skitter.html) | 
| LiveJournal | 3,997,962 | 34,681,189 |  Social network  [[download]](https://snap.stanford.edu/data/com-LiveJournal.html)|  

### preprocess

Eliminate all parallel, self-loop, and directed edges, and generate fully dynamic graph streams for insertion-only graphs using various parameters.

### single_machine

Single-machine algorithms for triangle counting includes
- TRIEST-IMPR, MASCOT: only for insertion edge in graph streams.
- ThinkDAcc, TRIEST-FD: support fully dynamic graph streams.
- MASCOT-FD: adapted from MASCOT to handle fully dynamic graph streams.

### multi_machine
Distributed algorithms for triangle counting includes
- DTC-AR: our proposed distributed streaming algorithm by adaptively resampling.
- DTC-FD: our proposed distributed streaming algorithm for handling fully dynamic graph streams.
- CoCoS: state-of-the-art distributed streaming algorithms.

### result_analysis

Evaluate the accuracy and performance of proposed distributed streaming algorithms over real-world datasets by the following measurable metrics.

- Mean Global Error
- Mean Local Error
- Global variance
- Pearson Correlation Coefficient

## Acknowledgement

Special thanks to kijungs, we draws inspiration from the excellent code structure in [CoCoS](https://github.com/kijungs/cocos).

