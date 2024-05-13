package org.example;

import java.util.concurrent.locks.ReentrantLock;

public class TrackingLock extends ReentrantLock {

    private Thread owner = null;
    private final String lockName;  // Lock'a özgü isim

    public TrackingLock(String lockName) {
        this.lockName = lockName;
    }

    @Override
    public void lock() {

        Thread currentThread = Thread.currentThread();

        if (!isHeldByCurrentThread() && !LockManager.waitingThreads.containsKey(currentThread)) {
            LockManager.registerWait(currentThread, lockName);
        }

        try {

            super.lock();
            owner = currentThread;

            Logger.log(owner.getName() + " has acquired the " + lockName);

            LockManager.registerLock(owner, lockName);
        } finally {
            LockManager.unregisterWait(currentThread);

            //LockManager.printRAG();
        }
    }

    @Override
    public void unlock() {
        if (isHeldByCurrentThread()) {
            Logger.log(owner.getName() + " is releasing the " + lockName);

            try {
                LockManager.unregisterLock(owner, this.lockName);

                super.unlock();
                owner = null;

            } catch (IllegalMonitorStateException ex) {
                Logger.log("Failed to release lock " + lockName + " by " + owner.getName() + ": " + ex.getMessage());
            } finally {
                //LockManager.printRAG();
            }
        } else {
            Logger.log("Attempt to unlock " + lockName + " by non-owner " + Thread.currentThread().getName());
        }
    }
}
