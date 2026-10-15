package algorithms;

import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphAlgorithmsTest {
    @Test
    void dsuCountsComponents() {
        DSU d = new DSU(5, new Metrics());

        assertTrue(d.union(0, 1));
        assertTrue(d.union(1, 2));
        assertFalse(d.union(0, 2));

        assertEquals(3, d.components());
        assertEquals(d.find(0), d.find(2));
    }

    @Test
    void mstAlgorithmsAgreeOnDisconnectedGraph() {
        Graph g = new Graph(4, false);

        g.addEdge(0, 1, 3);
        g.addEdge(1, 2, 1);
        g.addEdge(0, 2, 5);

        var a = MST.kruskal(g, new Metrics());
        var b = MST.prim(g, new Metrics());

        assertEquals(a.weight(), b.weight());
        assertEquals(2, a.components());
        assertEquals(4, a.weight());
    }

    @Test
    void kosarajuFindsComponents() {
        Graph g = new Graph(5, true);

        g.addEdge(0, 1, 1);
        g.addEdge(1, 0, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 2, 1);

        assertEquals(
                3,
                GraphAlgorithms.kosaraju(
                        g, new Metrics()
                ).components().size()
        );
    }

    @Test
    void kahnDetectsCycle() {
        Graph g = new Graph(3, true);

        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);

        assertTrue(
                GraphAlgorithms.kahn(
                        g, new Metrics()
                ).hasCycle()
        );
    }

    @Test
    void dagPathsAllowNegativeWeights() {
        Graph g = new Graph(4, true);

        g.addEdge(0, 2, 4);
        g.addEdge(1, 2, -3);
        g.addEdge(2, 3, 2);

        var r = GraphAlgorithms.dagShortestPaths(
                g, new Metrics()
        );

        assertEquals(0, r.distance()[0]);
        assertEquals(0, r.distance()[1]);
        assertEquals(-3, r.distance()[2]);
        assertEquals(-1, r.distance()[3]);
    }

    @Test
    void iterativeSccHandlesOneHundredThousandVertices() {
        Graph g = new Graph(100000, true);

        for (int i = 0; i < 99999; i++) {
            g.addEdge(i, i + 1, 1);
        }

        assertEquals(
                100000,
                GraphAlgorithms.kosaraju(
                        g, new Metrics()
                ).components().size()
        );
    }
}