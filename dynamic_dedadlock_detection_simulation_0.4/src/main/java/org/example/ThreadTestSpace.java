package org.example;

import java.util.List;

public class ThreadTestSpace {

    public static void main(String[] args) {
        LogProcessor logProcessor = new LogProcessor();
        Thread logInspectorThread = new Thread(logProcessor::processLogs);
        logInspectorThread.start();

        TestSpaceManager testSpaceManager = new TestSpaceManager();
        List<Resource> resources = testSpaceManager.createResources(50);
        List<Thread> threads = testSpaceManager.createThreads(resources, 15);

        for (Thread thread : threads) {
            testSpaceManager.initializeThreadState(thread.getName());
            thread.start();
        }

        //for (Thread thread : threads) {
        //    try {
        //        thread.join();
        //    } catch (InterruptedException e) {
        //        Thread.currentThread().interrupt();
        //    }
        //}
    }
}





