package org.example;

import java.util.List;

public class ThreadTestSpace {

    public static void main(String[] args) {

        LogProcessor logProcessor = new LogProcessor();
        Thread logInspectorThread = new Thread(logProcessor::processLogs);

        logInspectorThread.start();

        TestSpaceManager testSpaceManager = new TestSpaceManager();
        List<Resource> resources = testSpaceManager.createResources(10);
        List<Thread> threads = testSpaceManager.createThreads(resources, 40);

        for (Thread thread : threads) {
            testSpaceManager.initializeThreadState(thread.getName());
            thread.start();
        }

        // Delay to allow log processing
        try {
            Thread.sleep(5000); // 5 seconds delay to allow logs to be processed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Initialize the GUI for navigating WFGs
        DeadlockDetectionWithNavigation.initializeGUI();
    }
}
