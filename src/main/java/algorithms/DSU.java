package algorithms;

import metrics.Metrics;

public final class DSU {
    private final int[] parent;
    private final int[] size;
    private int components;
    private final Metrics metrics;

    public DSU(int n, Metrics metrics) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative");
        }

        parent = new int[n];
        size = new int[n];
        components = n;
        this.metrics = metrics;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IllegalArgumentException("Invalid vertex");
        }

        metrics.dsuOps++;

        int r = x;

        while (r != parent[r]) {
            r = parent[r];
        }

        while (x != r) {
            int next = parent[x];
            parent[x] = r;
            x = next;
        }

        return r;
    }

    public boolean union(int a, int b) {
        int ra = find(a);
        int rb = find(b);

        if (ra == rb) {
            return false;
        }

        metrics.dsuOps++;

        if (size[ra] < size[rb]) {
            int t = ra;
            ra = rb;
            rb = t;
        }

        parent[rb] = ra;
        size[ra] += size[rb];
        components--;

        return true;
    }

    public int components() {
        return components;
    }
}
