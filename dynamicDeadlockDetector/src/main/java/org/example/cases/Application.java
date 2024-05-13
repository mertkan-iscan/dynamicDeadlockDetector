package org.example.cases;


import org.example.TrackingLock;

public class Application {
    public static void main(String[] args) {
        TrackingLock lock1 = new TrackingLock("lock1");

        Thread t1 = new Thread(() -> {
            lock1.lock();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            lock1.unlock();

        });

        Thread t2 = new Thread(() -> {
            lock1.lock();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            lock1.unlock();
        });

        t2.start();
        t1.start();
    }
}
