package de.renew.remote;

import de.renew.application.SimulationEnvironment;

import de.renew.util.TextToken;

import java.rmi.RemoteException;


/**
 * This class implements the <code>TextTokenAccessor</code>
 * interface and nothing more.
 * <p>
 * </p>
 *
 * TextTokenAccessorImpl.java
 * Created: Tue Oct 7  2003
 * @author Michael Duvigneau
 * @since Renew 2.0
 */
public class TextTokenAccessorImpl extends ObjectAccessorImpl
        implements TextTokenAccessor {

    /**
     * The wrapped object, still typed as <code>TextToken</code>.
     **/
    private TextToken textToken;

    /**
     * Creates a new accessor for the given text token.
     *
     * @param textToken    the text token for the accessor.
     *
     * @param environment  the simulation environment where this
     *                     object belongs to.
     *
     * @exception RemoteException
     *   if a RMI failure occured.
     **/
    public TextTokenAccessorImpl(TextToken textToken,
                                 SimulationEnvironment environment)
            throws RemoteException {
        super(textToken, environment);
        this.textToken = textToken;
    }

    /**
     * @see TextTokenAccessor#toTokenText()
     **/
    public String toTokenText() throws RemoteException {
        return textToken.toTokenText();
    }
}