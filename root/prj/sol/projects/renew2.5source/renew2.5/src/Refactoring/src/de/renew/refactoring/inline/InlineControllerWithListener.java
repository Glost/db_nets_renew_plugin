package de.renew.refactoring.inline;

import java.util.HashSet;
import java.util.Set;


/**
 * Abstract InlineController implementation that provides listener support.
 *
 * @author 2mfriedr
 */
public abstract class InlineControllerWithListener implements InlineController {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(InlineControllerWithListener.class);
    private Set<InlineListener> _listeners = new HashSet<InlineListener>();

    @Override
    public abstract InlineStep nextStep();

    @Override
    public void addListener(InlineListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeListener(InlineListener listener) {
        _listeners.remove(listener);
    }

    protected Set<InlineListener> getListeners() {
        return _listeners;
    }

    /**
     * Sends {@link InlineListener#inlineFinished()} to all listeners.
     */
    protected void informListenersFinished() {
        for (InlineListener listener : getListeners()) {
            listener.inlineFinished();
        }
    }
}