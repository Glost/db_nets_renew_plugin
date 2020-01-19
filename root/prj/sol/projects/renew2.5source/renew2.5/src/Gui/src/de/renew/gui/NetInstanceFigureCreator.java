package de.renew.gui;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.CompositeFigure;
import CH.ifa.draw.standard.TextHolder;

import de.renew.net.NetInstance;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.ObjectAccessor;

import java.rmi.RemoteException;


/**
 * Creates a figure representation for net instance tokens.
 *
 * The figure representation can either be the instance
 * identifier or a graphical representation defined by the
 * associated net drawing.
 *
 * @author Frank Wienberg
 * @author Joern Schumacher
 * @author Michael Duvigneau
 **/
class NetInstanceFigureCreator implements FigureCreator {
    public Figure getTokenFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        NetInstanceAccessor instance = token.asNetInstance();
        CPNDrawing drawing = null;
        Figure iconFigure = null;

        CPNDrawingLoader drawingLoader = ModeReplacement.getInstance()
                                                        .getDrawingLoader();
        if (drawingLoader != null) {
            drawing = drawingLoader.getDrawing(instance.getNet().asString());
        }
        if (drawing != null) {
            iconFigure = drawing.getIconFigure();
        }
        if (iconFigure != null) {
            iconFigure = (Figure) iconFigure.clone();
            updateID(iconFigure, instance);
            return iconFigure;
        }
        return null;
    }

    public boolean canCreateFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        return token != null && token.isInstanceOf(NetInstance.class);
    }

    private void updateID(Figure figure, NetInstanceAccessor instance) {
        if (figure instanceof TextHolder) {
            TextHolder textFig = (TextHolder) figure;

            textFig.setReadOnly(true);
            textFig.setText(CPNInstanceDrawing.expandMacro(textFig.getText(),
                                                           instance));
        } else if (figure instanceof CompositeFigure) {
            // search recursively:
            FigureEnumeration figenumeration = figure.figures();

            while (figenumeration.hasMoreElements()) {
                updateID(figenumeration.nextFigure(), instance);
            }
        }
    }
}