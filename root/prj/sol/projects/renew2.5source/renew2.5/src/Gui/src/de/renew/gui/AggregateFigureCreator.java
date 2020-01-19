package de.renew.gui;

import CH.ifa.draw.framework.Figure;

import de.renew.remote.ObjectAccessor;

import de.renew.unify.Aggregate;

import java.rmi.RemoteException;


/**
 * Creates a figure representation for aggregate tokens like
 * tuples or lists.
 *
 * @author Frank Wienberg
 * @author Joern Schumacher
 * @author Michael Duvigneau
 **/
class AggregateFigureCreator implements FigureCreator {
    public Figure getTokenFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        return new AggregateFigure(token.asAggregate(), expanded);
    }

    public boolean canCreateFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        return token != null && token.isInstanceOf(Aggregate.class);
    }
}