package de.renew.net;

import de.renew.engine.common.StepIdentifier;
import de.renew.engine.searcher.ChannelTarget;

import java.io.Serializable;


public interface NetInstance extends ChannelTarget, Serializable {
    public void earlyConfirmation();

    public void earlyConfirmationTrace(StepIdentifier stepIdentifier);

    public void lateConfirmation(StepIdentifier stepIdentifier);

    public void createConfirmation(StepIdentifier stepIdentifier);

    public Object getInstance(Object netObject);

    public PlaceInstance getInstance(Place place);

    public TransitionInstance getInstance(Transition transition);

    public Net getNet();

    public String getID();

    public void setID(String id);

    public IDRegistry getRegistry();
}