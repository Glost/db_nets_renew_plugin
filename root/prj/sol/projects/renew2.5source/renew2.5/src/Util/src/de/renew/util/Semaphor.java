package de.renew.util;

public class Semaphor {
    private int counter;

    public Semaphor(int counter) {
        this.counter = counter;
    }

    public Semaphor() {
        this(0);
    }

    public synchronized void P() {
        while (counter == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        counter--;
    }

    public synchronized void V() {
        counter++;
        notify();
    }
}