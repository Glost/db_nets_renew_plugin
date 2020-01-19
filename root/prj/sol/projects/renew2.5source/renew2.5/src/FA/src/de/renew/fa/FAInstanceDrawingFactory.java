package de.renew.fa;

import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNInstanceDrawing;
import de.renew.gui.InstanceDrawingFactory;

import de.renew.remote.NetInstanceAccessor;

import java.rmi.RemoteException;


public class FAInstanceDrawingFactory implements InstanceDrawingFactory {
    @Override
    public CPNInstanceDrawing getInstanceDrawing(NetInstanceAccessor netInstance,
                                                 CPNDrawing drawing)
            throws RemoteException {
        return new FAInstanceDrawing(netInstance, drawing);
    }
}