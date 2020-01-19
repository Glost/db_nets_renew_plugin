package de.renew.net.event;

public class PlaceEventAdapter implements PlaceEventListener {
    boolean wantSync;

    public PlaceEventAdapter(boolean wantSync) {
        this.wantSync = wantSync;
    }

    public boolean wantSynchronousNotification() {
        return wantSync;
    }

    public void markingChanged(PlaceEvent pe) {
    }

    public void tokenAdded(TokenEvent te) {
    }

    public void tokenRemoved(TokenEvent te) {
    }

    public void tokenTested(TokenEvent te) {
    }

    public void tokenUntested(TokenEvent te) {
    }
}