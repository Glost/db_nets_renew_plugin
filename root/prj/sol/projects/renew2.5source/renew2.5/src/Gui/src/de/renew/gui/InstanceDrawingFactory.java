package de.renew.gui;

import de.renew.remote.NetInstanceAccessor;

import java.rmi.RemoteException;


public interface InstanceDrawingFactory {
    CPNInstanceDrawing getInstanceDrawing(NetInstanceAccessor netInstance,
                                          CPNDrawing drawing)
            throws RemoteException;
}