package org.example;

import static org.example.LogProcessor.logQueue;

public class ThreadLogger {
    public static void log(String message) {
        boolean added = logQueue.offer(message);
        if (!added) {
            System.err.println("Failed to log message: " + message);
        }
    }
}