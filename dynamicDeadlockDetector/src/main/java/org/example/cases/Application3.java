package org.example.cases;


import org.example.TrackingLock;

public class Application3 {
    public static void main(String[] args) {
        TrackingLock lock1 = new TrackingLock("lock1");
        TrackingLock lock2 = new TrackingLock("lock2");
        TrackingLock lock3 = new TrackingLock("lock3");

        Thread t1 = new Thread(() -> {
            lock1.lock();
            try {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock2.lock();
                try {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    lock3.lock();
                } finally {
                    lock3.unlock();
                }
            } finally {
                lock2.unlock();
            }
            lock1.unlock();
        });

        Thread t2 = new Thread(() -> {
            lock2.lock();
            try {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock3.lock();
                try {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    lock1.lock();
                } finally {
                    lock1.unlock();
                }
            } finally {
                lock3.unlock();
            }
            lock2.unlock();
        });

        Thread t3 = new Thread(() -> {
            lock3.lock();
            try {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock1.lock();
                try {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    lock2.lock();
                } finally {
                    lock2.unlock();
                }
            } finally {
                lock1.unlock();
            }
            lock3.unlock();
        });

        t1.start();
        t2.start();
        t3.start();
    }
}
