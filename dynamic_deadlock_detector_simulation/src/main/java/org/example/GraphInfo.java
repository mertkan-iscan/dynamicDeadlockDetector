package org.example;

import java.util.Map;
import java.util.Set;

public class GraphInfo implements Comparable<GraphInfo> {
    private final int id;
    private final Map<String, Set<String>> graph;
    private final boolean isDeadlock;

    public GraphInfo(int id, Map<String, Set<String>> graph, boolean isDeadlock) {
        this.id = id;
        this.graph = graph;
        this.isDeadlock = isDeadlock;
    }

    public int getId() {
        return id;
    }

    public Map<String, Set<String>> getGraph() {
        return graph;
    }

    public boolean isDeadlock() {
        return isDeadlock;
    }

    @Override
    public int compareTo(GraphInfo other) {
        return Integer.compare(this.id, other.id);
    }
}
