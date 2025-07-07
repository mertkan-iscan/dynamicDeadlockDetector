package org.example;

import java.util.concurrent.locks.ReentrantLock;

public class Resource {
    private final ReentrantLock lock = new ReentrantLock();
    private final String name;

    public Resource(String name) {
        this.name = name;
    }

    public void lockResource() {
        ThreadLogger.log(Thread.currentThread().getName() + ":WAIT:" + name);
        lock.lock();
        ThreadLogger.log(Thread.currentThread().getName() + ":LOCK:" + name);
    }

    public void unlockResource() {
        ThreadLogger.log(Thread.currentThread().getName() + ":UNLOCK:" + name);
        lock.unlock();
    }

    public void holdResource(long milliseconds) {
        if (lock.isHeldByCurrentThread()) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            throw new IllegalStateException("Thread does not hold the lock for resource: " + name);
        }
    }
}
