package de.renew.gui;

import CH.ifa.draw.framework.Figure;

import de.renew.remote.ObjectAccessor;

import java.rmi.RemoteException;


/**
 * This interface is implemented by classes that decide how a
 * token type will be represented in the gui.
 * To use a class implementing this, it needs to be registered
 * in the {@link GuiPlugin}.
 *
 * @author joern
 * @since Renew 2.0
 **/
public interface FigureCreator {

    /**
     * Computes a figure to represent the given token.
     * <p>
     * This method is called if and only if a previous call to
     * {@link #canCreateFigure} returned <code>true</code>.
     * </p>
     *
     * @param token     the object accessor of the token to
     *                  represent.
     *
     * @param expanded  whether the token should be displayed
     *                  in an expanded way (e.g. with more
     *                  information or fancier).
     *
     * @return  the figure representing the given token.
     *
     * @exception RemoteException
     *   if the remote connection to the token accessor has any problems.
     **/
    public Figure getTokenFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException;


    /**
     * Queries the <code>FigureCreator</code> whether it knows
     * about how to compute a figure that represents the given
     * token.
     * <p>
     * Only if this method returns true, {@link #getTokenFigure}
     * will be called.
     * </p>
     *
     * @param token     the object accessor of the token to
     *                  represent.
     *
     * @param expanded  whether the token should be displayed
     *                  in an expanded way (e.g. with more
     *                  information or fancier).
     *
     * @return  <code>true</code>, if the <code>FigureCreator</code>
     *          considers itself responsible for the representation
     *          of the given token.
     *
     * @exception RemoteException
     *   if the remote connection to the token accessor has any problems.
     **/
    public boolean canCreateFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException;
}