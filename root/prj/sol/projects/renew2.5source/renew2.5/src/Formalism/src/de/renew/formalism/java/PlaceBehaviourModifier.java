package de.renew.formalism.java;



/**
 * Stores a value for the type of a place to transfer the value
 * from a shadow net inscription to the compiled
 * {@link de.renew.net.Place} object. The valid values are
 * defined as integer constant definition in the <code>Place</code>
 * class.
 * <p>
 * </p>
 * PlaceBehaviourModifier.java
 * Created: Wed Mar 21  2001
 * @author Michael Duvigneau
 */
public class PlaceBehaviourModifier {
    public final int behaviour;

    public PlaceBehaviourModifier(int behaviour) {
        this.behaviour = behaviour;
    }
}