package de.renew.net.event;

public interface PlaceEventProducer {
    public void addPlaceEventListener(PlaceEventListener listener);

    public void removePlaceEventListener(PlaceEventListener listener);
}