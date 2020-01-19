package de.renew.unify;

import java.util.Set;


public interface Referer {
    void possiblyCompleted(Set<Notifiable> listeners, StateRecorder recorder)
            throws Impossible;
}