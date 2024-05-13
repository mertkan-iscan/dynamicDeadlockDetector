package org.example;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Map;

public class LockManager {

    static final Map<Thread, List<String>> waitingThreads = new ConcurrentHashMap<>();
    private static final Map<Thread, List<String>> threadLockMap = new ConcurrentHashMap<>();

    public static void registerWait(Thread thread, String lockName) {
        waitingThreads.computeIfAbsent(thread, k -> new ArrayList<>()).add(lockName);
        Logger.log(thread.getName() + " is registered to wait for the " + lockName);
        printRAG();
    }

    public static void unregisterWait(Thread thread) {
        if (waitingThreads.containsKey(thread)) {
            Logger.log(thread.getName() + " has stopped waiting for the " + waitingThreads.get(thread));
            waitingThreads.remove(thread);
            printRAG();
        }
    }

    public static void registerLock(Thread thread, String lockName) {
        threadLockMap.computeIfAbsent(thread, k -> new ArrayList<>()).add(lockName);
        printRAG();
    }

    public static void unregisterLock(Thread thread, String lockName) {
        if (threadLockMap.containsKey(thread)) {
            threadLockMap.get(thread).remove(lockName);
            if (threadLockMap.get(thread).isEmpty()) {
                threadLockMap.remove(thread);
                printRAG();
            }
        }
    }

    public static void printWaitingThreads() {
        synchronized (System.out) {
            StringBuilder sb = new StringBuilder();
            sb.append("    WAITING THREADS: ");
            if (!waitingThreads.isEmpty()) {
                waitingThreads.forEach((thread, locks) -> sb.append(thread.getName()).append(" for ").append(locks).append(", "));
            } else {
                sb.append("None");
            }
            sb.append("\n--------------------------------------------------");
            Logger.log(sb.toString());
        }
    }

    public static void printCurrentLocks() {
        synchronized (System.out) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n--------------------------------------------------\n").append("    CURRENT LOCKS: ");
            if (!threadLockMap.isEmpty()) {
                threadLockMap.forEach((thread, locks) -> sb.append(thread.getName()).append(" holds ").append(locks).append(", "));
            } else {
                sb.append("None");
            }
            Logger.log(sb.toString());
        }
    }

    public static void printRAG(){
        printCurrentLocks();
        printWaitingThreads();
    }
}
