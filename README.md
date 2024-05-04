# DTC: Self-Adaptive and Accurate Distributed Triangle Counting in Fully Dynamic Graph Streams

## User Guide

Our project includes the following:

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

