package de.renew.refactoring.inline;

import java.util.HashSet;
import java.util.Set;


/**
 * Abstract InlineController implementation that provides listener support.
 *
 * @author 2mfriedr
 */
public class InlineStepWithListener implements InlineStep {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(InlineStepWithListener.class);
    private Set<InlineStepListener> _listeners = new HashSet<InlineStepListener>();

    @Override
    public void addListener(InlineStepListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeListener(InlineStepListener listener) {
        _listeners.remove(listener);
    }

    protected Set<InlineStepListener> getListeners() {
        return _listeners;
    }

    /**
     * Sends {@link InlineStepListener#inlineStepCancelled()} to all listeners.
     */
    protected void informListenersCancelled() {
        for (InlineStepListener listener : getListeners()) {
            listener.inlineStepCancelled();
        }
    }

    /**
     * Sends {@link InlineStepListener#inlineStepFinished()} to all listeners.
     */
    protected void informListenersFinished() {
        for (InlineStepListener listener : getListeners()) {
            listener.inlineStepFinished();
        }
    }
}