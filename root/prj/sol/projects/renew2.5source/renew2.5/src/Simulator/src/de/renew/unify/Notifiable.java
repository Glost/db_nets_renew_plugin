package de.renew.unify;

public interface Notifiable {
    // Notify if a target object has become bound.
    void boundNotify(StateRecorder recorder) throws Impossible;
}