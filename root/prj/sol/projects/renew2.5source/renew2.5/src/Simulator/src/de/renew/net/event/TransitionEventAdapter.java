package de.renew.net.event;

public class TransitionEventAdapter implements TransitionEventListener {
    boolean wantSync;

    public TransitionEventAdapter(boolean wantSync) {
        this.wantSync = wantSync;
    }

    public boolean wantSynchronousNotification() {
        return wantSync;
    }

    public void firingStarted(FiringEvent fe) {
    }

    public void firingComplete(FiringEvent fe) {
    }
}