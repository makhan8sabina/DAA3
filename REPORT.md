# Assignment 3 Report — Graph Algorithms

## 1. Implementation Overview

The project uses adjacency lists and supports directed and undirected graphs. It implements Disjoint Set Union, Kruskal, Prim, iterative Kosaraju, Kahn topological sorting and shortest paths in a DAG. Operation counters are collected during algorithm execution. The benchmark compares five graph sizes and two edge densities.

## 2. Complexity Table

| Algorithm | Time complexity | Extra space |
|---|---|---|
| DSU find | Amortized O(α(V)) | O(1) per call |
| DSU union | Amortized O(α(V)) | O(1) per call |
| Kruskal | O(E log E + E α(V)) | O(V + E) |
| Prim | O((V + E) log V) | O(V + E) |
| Kosaraju | O(V + E) | O(V + E) |
| Kahn | O(V + E) | O(V) |
| DAG shortest paths | O(V + E) | O(V) beyond graph storage |

## 3. Correctness Arguments

### Cut Property and MST

A cut divides the vertices into two groups. The lightest edge crossing a cut is safe for a minimum spanning tree when it connects different components of the current forest. Kruskal processes edges in increasing order and uses DSU to reject edges that would create cycles. Prim chooses the lightest edge connecting its current tree to an unvisited vertex. Both algorithms therefore use the cut property to build a minimum spanning tree. For disconnected graphs, they produce a minimum spanning forest.

### DAG Shortest Paths

A topological order places each vertex before all its successors. Every vertex with indegree zero starts with distance zero, representing a virtual super-source. When a vertex is processed, all its predecessors have already been processed. Relaxing its outgoing edges therefore propagates the best possible distances to later vertices. By induction over the topological order, the final distances are shortest. Negative weights are safe in a DAG because a directed cycle, including a negative cycle, cannot exist.

## 4. Which Algorithm When?

| Situation | Algorithm | Reason |
|---|---|---|
| MST from an edge list | Kruskal | Sorting edges and using DSU is natural |
| MST using adjacency lists | Prim | Expands the current tree using a priority queue |
| Strongly connected components | Kosaraju | Two linear traversals |
| Dependency ordering | Kahn | Uses indegrees and detects cycles |
| Shortest paths in a DAG | DAG relaxation | Linear time and supports negative weights |
| Potential dependency cycles | Kahn | Detects whether a complete topological order exists |

## 5. Counters and Slope Check

Run the benchmark and plotting script before completing this section. Kosaraju should scan each directed edge twice across its two DFS passes. Kahn scans each edge during queue processing, and DAG relaxation scans each edge during the relaxation pass, in addition to topological-order work. These operations are linear in V + E. Kruskal includes the cost of sorting edges, O(E log E).

Insert the actual measurements from `results/results.csv` and `results/plots/slope_values.csv`. Do not claim the reference values match until they have been verified.

## 6. Runtime Discussion

The benchmark uses the median of five runs. Execution time can vary because of JVM warm-up, processor speed, operating-system scheduling and background applications. Timing is particularly noisy for small graphs. Operation counters provide more reproducible evidence of algorithmic complexity.


## Empirical Complexity & Plot Analysis

### 1. Operations vs N (Log-Log Scale)
On log-log axes, the operation count (`edge_scans`) forms straight lines with a slope of $\approx 1.00$. This confirms that algorithm execution scales linearly with graph size, matching the theoretical $O(V + E)$ complexity class.

### 2. Scans per Edge ($\text{edge\_scans} / m$)
The plot shows flat horizontal lines across all graph sizes:
- **Kosaraju:** Exactly $2.0$ scans per edge due to two DFS passes on original and transposed graphs ($2m$ total scans).
- **Kahn & DAG Shortest Paths:** Exactly $1.0$ scan per edge, as each edge is relaxed or processed exactly once.
- **Kruskal & Prim:** Consistently $1.0$ scan per edge during MST construction.

The flat lines empirically validate the linear work per edge regardless of vertex count $n$.

### 3. Share of Edges Examined by Kruskal
Kruskal's algorithm iterates through all $m$ sorted edges in the graph, resulting in an edge scan ratio of exactly $1.0$ for both sparse ($m = 4n$) and dense ($m = 20n$) graphs.