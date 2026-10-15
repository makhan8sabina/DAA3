package benchmark;

import algorithms.GraphAlgorithms;
import algorithms.MST;
import algorithms.MSTResult;
import graph.Graph;
import metrics.Metrics;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BenchmarkRunner {
    private static final int[] SIZES = {
            1000, 3162, 10000, 31623, 100000
    };

    private static final int[] DENSITIES = {4, 20};
    private static final int SEED = 42;

    private static final String HEADER =
            "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";

    private record Row(
            String algorithm,
            String graph,
            int n,
            int m,
            double time,
            Metrics metrics,
            long result
    ) {}

    public static void main(String[] args) throws Exception {
        Path out = Path.of("results/results.csv");
        Files.createDirectories(out.getParent());

        List<Row> rows = new ArrayList<>();

        for (int n : SIZES) {
            for (int d : DENSITIES) {
                int m = d * n;

                Graph undirected = generateUndirected(
                        n, m, new Random(SEED)
                );

                Metrics km = new Metrics();

                long kt = medianTime(
                        () -> MST.kruskal(
                                undirected,
                                new Metrics()
                        )
                );

                MSTResult kr = MST.kruskal(undirected, km);

                Metrics pm = new Metrics();

                long pt = medianTime(
                        () -> MST.prim(
                                undirected,
                                new Metrics()
                        )
                );

                MSTResult pr = MST.prim(undirected, pm);

                if (kr.weight() != pr.weight()
                        || kr.components() != pr.components()) {
                    throw new IllegalStateException(
                            "Kruskal/Prim mismatch n=" + n + " m=" + m
                    );
                }

                rows.add(new Row(
                        "Kruskal", densityName(d), n, m,
                        kt / 1_000_000.0, km, kr.weight()
                ));

                rows.add(new Row(
                        "Prim", densityName(d), n, m,
                        pt / 1_000_000.0, pm, pr.weight()
                ));

                Graph directed = generateDirected(
                        n, m, new Random(SEED)
                );

                Metrics sm = new Metrics();

                long st = medianTime(
                        () -> GraphAlgorithms.kosaraju(
                                directed,
                                new Metrics()
                        )
                );

                var sr = GraphAlgorithms.kosaraju(directed, sm);

                rows.add(new Row(
                        "Kosaraju", densityName(d), n, m,
                        st / 1_000_000.0, sm,
                        sr.components().size()
                ));

                Graph dag = generateDag(
                        n, m, new Random(SEED)
                );

                Metrics qm = new Metrics();

                long qt = medianTime(
                        () -> GraphAlgorithms.kahn(
                                dag,
                                new Metrics()
                        )
                );

                var qr = GraphAlgorithms.kahn(dag, qm);

                if (qr.hasCycle() || qr.order().size() != n) {
                    throw new IllegalStateException(
                            "Kahn failed on generated DAG"
                    );
                }

                rows.add(new Row(
                        "Kahn", densityName(d), n, m,
                        qt / 1_000_000.0, qm,
                        qr.order().size()
                ));

                Metrics dm = new Metrics();

                long dt = medianTime(
                        () -> GraphAlgorithms.dagShortestPaths(
                                dag,
                                new Metrics()
                        )
                );

                var dr = GraphAlgorithms.dagShortestPaths(dag, dm);

                long sum = 0;

                for (long x : dr.distance()) {
                    sum += x;
                }

                rows.add(new Row(
                        "DagShortestPath", densityName(d), n, m,
                        dt / 1_000_000.0, dm, sum
                ));

                System.out.printf(
                        "n=%d m=%d MST=%d SCC=%d DAG-distance-sum=%d%n",
                        n, m, kr.weight(),
                        sr.components().size(), sum
                );
            }
        }

        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(out))) {
            w.println(HEADER);

            for (Row r : rows) {
                Metrics x = r.metrics();

                w.printf(
                        java.util.Locale.US,
                        "%s,%s,%d,%d,%.6f,%d,%d,%d,%d,%d%n",
                        r.algorithm(),
                        r.graph(),
                        r.n(),
                        r.m(),
                        r.time(),
                        x.dsuOps,
                        x.edgeScans,
                        x.comparisons,
                        x.maxDepth,
                        r.result()
                );
            }
        }

        System.out.println(
                "Wrote " + rows.size() + " rows to " + out
        );
    }

    private static String densityName(int d) {
        return d == 4 ? "sparse" : "dense";
    }

    private interface Timed {
        void run();
    }

    private static long medianTime(Timed task) {
        long[] times = new long[5];

        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            task.run();
            times[i] = System.nanoTime() - start;
        }

        java.util.Arrays.sort(times);
        return times[2];
    }

    private static void shuffle(int[] a, Random rnd) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    private static Graph generateUndirected(
            int n, int m, Random rnd
    ) {
        Graph g = new Graph(n, false);

        int[] perm = new int[n];

        for (int i = 0; i < n; i++) {
            perm[i] = i;
        }

        shuffle(perm, rnd);

        for (int i = 1; i < n; i++) {
            g.addEdge(
                    perm[rnd.nextInt(i)],
                    perm[i],
                    1 + rnd.nextInt(1000)
            );
        }

        while (g.edgeCount() < m) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);

            if (a != b) {
                g.addEdge(a, b, 1 + rnd.nextInt(1000));
            }
        }

        return g;
    }

    private static Graph generateDirected(
            int n, int m, Random rnd
    ) {
        Graph g = new Graph(n, true);

        while (g.edgeCount() < m) {
            g.addEdge(
                    rnd.nextInt(n),
                    rnd.nextInt(n),
                    1 + rnd.nextInt(1000)
            );
        }

        return g;
    }

    private static Graph generateDag(
            int n, int m, Random rnd
    ) {
        Graph g = new Graph(n, true);

        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[i] = i;
        }

        shuffle(rank, rnd);

        while (g.edgeCount() < m) {
            int a = rnd.nextInt(n);
            int b = rnd.nextInt(n);

            if (a == b) {
                continue;
            }

            if (rank[a] > rank[b]) {
                int t = a;
                a = b;
                b = t;
            }

            g.addEdge(a, b, 1 + rnd.nextInt(1000));
        }

        return g;
    }
}