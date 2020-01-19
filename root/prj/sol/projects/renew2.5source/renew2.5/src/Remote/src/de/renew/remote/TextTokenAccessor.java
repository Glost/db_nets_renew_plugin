package de.renew.remote;

import java.rmi.RemoteException;


/**
 * A text token accessor can be wrapped around a
 * {@link de.renew.util.TextToken} implementation, allowing the
 * retrieval of its textual representation.
 * <p>
 * This is a remote interface that allows an implementation
 * that can be used via RMI.
 * </p>
 *
 * TextTokenAccessor.java
 * Created: Tue Oct 7  2003
 * @author Michael Duvigneau
 * @since Renew 2.0
 **/
public interface TextTokenAccessor extends ObjectAccessor {

    /**
     * Returns the textual representation of the token wrapped by
     * this accessor.
     *
     * @return   the token text
     *
     * @exception RemoteException
     *   if an RMI failure occurred.
     */
    public String toTokenText() throws RemoteException;
}