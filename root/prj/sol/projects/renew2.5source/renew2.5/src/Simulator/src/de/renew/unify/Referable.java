package de.renew.unify;

import java.util.Set;


public interface Referable {
    public void addBacklink(Reference reference, StateRecorder recorder);

    public void occursCheck(Unknown that, Set<IdentityWrapper> visited)
            throws Impossible;
}