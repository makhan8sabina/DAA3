package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MST {
    private MST() {}

    public static MSTResult kruskal(Graph g, Metrics m) {
        if (g.directed()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph"
            );
        }

        List<Edge> es = new ArrayList<>(g.edges());
        es.sort(Comparator.comparingInt(Edge::weight));

        DSU dsu = new DSU(g.size(), m);
        List<Edge> chosen = new ArrayList<>();
        long total = 0;

        for (Edge e : es) {
            m.edgeScans++;
            m.comparisons++;

            if (dsu.find(e.from()) != dsu.find(e.to())) {
                dsu.union(e.from(), e.to());
                chosen.add(e);
                total += e.weight();
            }
        }

        return new MSTResult(
                total,
                dsu.components(),
                List.copyOf(chosen)
        );
    }

    public static MSTResult prim(Graph g, Metrics m) {
        if (g.directed()) {
            throw new IllegalArgumentException(
                    "MST requires an undirected graph"
            );
        }

        int n = g.size();
        boolean[] used = new boolean[n];

        PriorityQueue<Edge> pq = new PriorityQueue<>(
                Comparator.comparingInt(Edge::weight)
        );

        List<Edge> chosen = new ArrayList<>();
        long total = 0;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (used[start]) {
                continue;
            }

            components++;
            used[start] = true;

            for (Edge e : g.outgoing(start)) {
                m.edgeScans++;
                pq.add(e);
            }

            while (!pq.isEmpty()) {
                Edge e = pq.poll();

                if (used[e.to()]) {
                    continue;
                }

                used[e.to()] = true;
                chosen.add(e);
                total += e.weight();

                for (Edge next : g.outgoing(e.to())) {
                    m.edgeScans++;
                    pq.add(next);
                }
            }
        }

        return new MSTResult(
                total,
                components,
                List.copyOf(chosen)
        );
    }
}