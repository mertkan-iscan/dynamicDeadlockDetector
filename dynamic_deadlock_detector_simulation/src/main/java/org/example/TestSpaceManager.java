package org.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestSpaceManager {
    public List<Resource> createResources(int count) {
        List<Resource> resources = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            resources.add(new Resource("Resource" + i));
        }
        return resources;
    }

    public List<Thread> createThreads(List<Resource> resources, int count) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            threads.add(new Thread(createTask(resources), "Thread" + i));
        }
        return threads;
    }

    public void initializeThreadState(String threadName) {
        LogProcessor.holdingThreads.put(threadName, new HashSet<>());
        LogProcessor.waitingThreads.put(threadName, new HashSet<>());
    }

    private Runnable createTask(List<Resource> resources) {
        return () -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            Resource resource1 = resources.get(random.nextInt(resources.size()));
            Resource resource2 = resources.get(random.nextInt(resources.size()));

            if (random.nextBoolean()) {
                resource1.lockResource();
                resource2.lockResource();
            } else {
                resource2.lockResource();
                resource1.lockResource();
            }

            resource1.unlockResource();
            resource2.unlockResource();
        };
    }
}
