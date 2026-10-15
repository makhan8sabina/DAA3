# DAA Assignment 3 - Graphs

## Requirements
- JDK 17 or newer
- Maven 3.9 or newer
- Python 3.10 or newer
- Python packages: pandas, matplotlib, numpy

## Build and test

Run from the project root:

```bash
mvn clean test
```

A successful run should end with `BUILD SUCCESS`.

## Run the benchmark

```bash
mvn -q -DskipTests package
java -cp target/classes benchmark.BenchmarkRunner
```

The benchmark writes `results/results.csv`.

## Generate the plots

```bash
python -m pip install pandas matplotlib numpy
python scripts/plot.py
```

The charts are saved in `results/plots/`.

## Input files

The `data/` directory contains disconnected, cyclic and DAG examples. Each graph file starts with `n m directed|undirected`, followed by `m` lines containing `u v weight`.

## CSV format

`algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result`
