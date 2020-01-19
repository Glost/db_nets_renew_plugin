package de.renew.net.event;

public interface PlaceEventListener extends NetEventListener {

    /** This event method is called whenever the marking of
     *  the corresponding PlaceInstance changes dramatically
     *  (i.e. by more than one token).
     */
    public void markingChanged(PlaceEvent pe);

    /** This event occurs when a single token (contained in the
     *  event object) is put into the corresponding PlaceInstance.
     */
    public void tokenAdded(TokenEvent te);

    /** This event occurs when a single token (contained in the
     *  event object) is removed from the corresponding PlaceInstance.
     */
    public void tokenRemoved(TokenEvent te);

    /** This event occurs when a token (contained in the
     *  event object) is tested within the corresponding PlaceInstance.
     */
    public void tokenTested(TokenEvent te);

    /** This event occurs when a token (contained in the
     *  event object) is untested within the corresponding PlaceInstance.
     */
    public void tokenUntested(TokenEvent te);
}