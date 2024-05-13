package org.example.cases;

import org.example.TrackingLock;

public class Application2 {
    public static void main(String[] args) {
        TrackingLock lock1 = new TrackingLock("lock1");
        TrackingLock lock2 = new TrackingLock("lock2");

        Thread t1 = new Thread(() -> {


            lock1.lock();
            lock2.lock();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            lock2.unlock();
            lock1.unlock();

        });

        Thread t2 = new Thread(() -> {
            lock2.lock();
            lock1.lock();


            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            lock1.unlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            lock2.lock();
        });

        t1.start();
    }
}