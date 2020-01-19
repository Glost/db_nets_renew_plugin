package de.renew.refactoring.wizard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;


/**
 * Simple stack for {@link WizardPage}s, implemented using a {@link Deque}.
 * For use by {@link Wizard}.
 *
 * @see Wizard
 * @author 2mfriedr
 */
final class WizardPageStack {
    private final Deque<WizardPage> _deque = new ArrayDeque<WizardPage>();

    /**
     * Pushes a new page onto the stack.
     *
     * @param page the page
     * @throws NullPointerException if the specified page is null
     */
    void push(final WizardPage page) throws NullPointerException {
        if (page == null) {
            throw new NullPointerException();
        }
        _deque.addFirst(page);
    }

    /**
     * Removs and returns the top page off the stack.
     *
     * @return the top page
     * @throws NoSuchElementException if the stack's size is 0
     */
    WizardPage pop() throws NoSuchElementException {
        return _deque.removeFirst();
    }

    /**
     * Returns the stack's top page without removing it.
     *
     * @return the top page
     */
    WizardPage peek() {
        return _deque.peekFirst();
    }

    /**
     * Returns the stack's size.
     *
     * @return the size
     */
    int size() {
        return _deque.size();
    }
}