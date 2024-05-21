package org.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LogProcessor {

    public static final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();

    static final Map<String, Set<String>> holdingThreads = new HashMap<>();
    static final Map<String, Set<String>> waitingThreads = new HashMap<>();



    public void processLogs() {
        while (true) {
            String log = logQueue.poll();
            if (log != null) {
                System.out.println("Log: " + log);
                updateRag(log);
            }
        }
    }

    private static void updateRag(String log) {
        String[] parts = log.split(":");
        String threadName = parts[0];
        String action = parts[1];
        String resourceName = parts[2];

        switch (action) {
            case "WAIT":
                waitingThreads.computeIfAbsent(threadName, k -> new HashSet<>()).add(resourceName);
                break;
            case "LOCK":
                waitingThreads.getOrDefault(threadName, new HashSet<>()).remove(resourceName);
                holdingThreads.computeIfAbsent(threadName, k -> new HashSet<>()).add(resourceName);
                break;
            case "UNLOCK":
                holdingThreads.getOrDefault(threadName, new HashSet<>()).remove(resourceName);
                break;
        }

        System.out.println("#############################################################################################################");
        System.out.println("Current Holding Threads: " + holdingThreads);
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println("Current Waiting Threads: " + waitingThreads);
        System.out.println("#############################################################################################################");
    }
}
