package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogProcessor {

    public static final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();
    private static final List<GraphInfo> graphInfoList = new ArrayList<>();
    private static int graphCounter = 0;

    static final ConcurrentHashMap<String, Set<String>> holdingThreads = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Set<String>> waitingThreads = new ConcurrentHashMap<>();

    public void processLogs() {
        while (true) {
            String log = logQueue.poll();
            if (log != null) {
                System.out.println("Log: " + log);
                updateRag(log);

                int currentGraphCounter = ++graphCounter;

                new Thread(() -> {
                    Map<String, Set<String>> holdingThreadsCopy;
                    Map<String, Set<String>> waitingThreadsCopy;

                    synchronized (LogProcessor.class) {
                        holdingThreadsCopy = new ConcurrentHashMap<>(holdingThreads);
                        waitingThreadsCopy = new ConcurrentHashMap<>(waitingThreads);
                    }

                    Map<String, Set<String>> waitForGraph = convertRagToWfg(holdingThreadsCopy, waitingThreadsCopy);
                    if (!waitForGraph.isEmpty()) {
                        boolean isDeadlock = searchForCycles(waitForGraph);
                        synchronized (graphInfoList) {
                            graphInfoList.add(new GraphInfo(currentGraphCounter, waitForGraph, isDeadlock));
                            Collections.sort(graphInfoList);
                        }

                        if (isDeadlock) {
                            Set<String> deadlockedThreads = getDeadlockedThreads(waitForGraph);
                            System.out.println("Deadlock detected involving threads:");
                            for (String thread : deadlockedThreads) {
                                System.out.println(thread);
                            }
                        }
                    }
                }).start();
            }
        }
    }

    private static void updateRag(String log) {
        String[] parts = log.split(":");
        String threadName = parts[0];
        String action = parts[1];
        String resourceName = parts[2];

        synchronized (LogProcessor.class) {
            switch (action) {
                case "WAIT":
                    if (!holdingThreads.getOrDefault(threadName, new HashSet<>()).contains(resourceName)) {
                        waitingThreads.computeIfAbsent(threadName, k -> new HashSet<>()).add(resourceName);
                    }
                    break;
                case "LOCK":
                    waitingThreads.getOrDefault(threadName, new HashSet<>()).remove(resourceName);
                    holdingThreads.computeIfAbsent(threadName, k -> new HashSet<>()).add(resourceName);
                    break;
                case "UNLOCK":
                    holdingThreads.getOrDefault(threadName, new HashSet<>()).remove(resourceName);
                    break;
            }
        }
    }

    private Map<String, Set<String>> convertRagToWfg(Map<String, Set<String>> holdingThreads, Map<String, Set<String>> waitingThreads) {
        Map<String, Set<String>> waitForGraph = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : waitingThreads.entrySet()) {
            String waitingThread = entry.getKey();
            Set<String> resources = entry.getValue();

            for (String resource : resources) {
                for (Map.Entry<String, Set<String>> holdEntry : holdingThreads.entrySet()) {
                    String holdingThread = holdEntry.getKey();
                    Set<String> heldResources = holdEntry.getValue();

                    if (heldResources.contains(resource) && !waitingThread.equals(holdingThread)) {
                        waitForGraph.computeIfAbsent(waitingThread, k -> new HashSet<>()).add(holdingThread);
                    }
                }
            }
        }

        return waitForGraph;
    }

    private boolean searchForCycles(Map<String, Set<String>> waitForGraph) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String node : waitForGraph.keySet()) {
            if (detectCycleDFS(node, waitForGraph, visited, recStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycleDFS(String current, Map<String, Set<String>> graph, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(current)) {
            return true;
        }

        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);
        recStack.add(current);

        Set<String> neighbors = graph.getOrDefault(current, new HashSet<>());
        for (String neighbor : neighbors) {
            if (detectCycleDFS(neighbor, graph, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(current);
        return false;
    }

    private Set<String> getDeadlockedThreads(Map<String, Set<String>> waitForGraph) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        Set<String> deadlockedThreads = new HashSet<>();

        for (String node : waitForGraph.keySet()) {
            if (detectCycleDFS(node, waitForGraph, visited, recStack)) {
                deadlockedThreads.addAll(recStack);
            }
        }
        return deadlockedThreads;
    }

    public static List<GraphInfo> getGraphInfoList() {
        return graphInfoList;
    }
}
