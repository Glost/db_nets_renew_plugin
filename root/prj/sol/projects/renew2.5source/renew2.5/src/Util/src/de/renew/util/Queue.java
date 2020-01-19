package de.renew.util;

import java.io.Serializable;

import java.util.LinkedList;
import java.util.NoSuchElementException;


/**
 *  <code>Queue</code> implements a simple FIFO-Queue.
 *
 * @author <a href="mailto:roelke@informatik.uni-hamburg.de">Heiko Roelke</a>
 * @version 1.0
 * @since Renew 1.5
 */
public class Queue<T> implements Serializable {
    static final long serialVersionUID = 3620540645530043407L;
    private LinkedList<T> linkedList;

    public Queue() {
        linkedList = new LinkedList<T>();
    }

    /**
     *  <code>enqueue</code> inserts an element at the queue's
     *  end.
     *
     * @param element an <code>Object</code> value
     */
    public void enqueue(T element) {
        synchronized (linkedList) {
            linkedList.addLast(element);
            linkedList.notifyAll();
        }
    }

    /**
     *  <code>dequeue</code> returns oldest element and removes
     *  it from the queue.
     *
     * @return an <code>Object</code> value
     * @exception NoSuchElementException if queue is empty
     */
    public T dequeue() throws NoSuchElementException {
        synchronized (linkedList) {
            return linkedList.removeFirst();
        }
    }

    /**
     *  <code>front</code> returns the front element
     *  <em>without</em> removing it from the queue.
     *
     * @return an <code>Object</code> value
     * @exception NoSuchElementException if queue is empty.
     */
    public T front() throws NoSuchElementException {
        synchronized (linkedList) {
            return linkedList.getFirst();
        }
    }

    /**
     * Waits until the queue is non-empty, then returns the
     * oldest element and removes it from the queue.
     * <p>
     * If the queue contains an element in the moment this method
     * is called, it will return immediately (as {@link #dequeue}
     * would. But if the queue is empty, this method will block
     * until there is an element.
     * </p>
     * Added 25 Jun 2001 by Michael Duvigneau
     *
     * @return an <code>Object</code> value
     * @exception InterruptedException
     *     if another thread has interrupted the current thread.
     */
    public T waitAndDequeue() throws InterruptedException {
        synchronized (linkedList) {
            while (linkedList.isEmpty()) {
                linkedList.wait();
            }
            return linkedList.removeFirst();
        }
    }

    /**
     * Tells whether the queue doesn't contain any elements.
     *
     * @return <code>true</code>  if the queue is empty, <br>
     *         <code>false</code> if there is at least one
     *                            element in the queue.
     */
    public boolean isEmpty() {
        return linkedList.isEmpty();
    }
}