package de.renew.net.event;

public interface TransitionEventProducer {
    public void addTransitionEventListener(TransitionEventListener listener);

    public void removeTransitionEventListener(TransitionEventListener listener);
}