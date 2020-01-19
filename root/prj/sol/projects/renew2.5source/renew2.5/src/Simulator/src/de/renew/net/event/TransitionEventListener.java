package de.renew.net.event;

public interface TransitionEventListener extends NetEventListener {

    /** This event occurs when a TransitionInstance starts a new
     *  Occurrence. When the event is sent, it is guarantueed
     *  that the firing can take place, i.e., all input tokens have
     *  already been successfully collected.
     *
     *  The Event is sent to all TransitionInstances which are part of the
     *  Occurrence, not only to the triggering TransitionInstance.
     */
    public void firingStarted(FiringEvent fe);

    /** This event signals that a Transition Occurrence has completed.
     *  The Event is sent to all TransitionInstances which are part of the
     *  Occurrence, not only to the triggering TransitionInstance.
     *
     *  The event that is delivered is exactly the same event that was
     *  delivered during the start of the firing.
     */
    public void firingComplete(FiringEvent fe);
}