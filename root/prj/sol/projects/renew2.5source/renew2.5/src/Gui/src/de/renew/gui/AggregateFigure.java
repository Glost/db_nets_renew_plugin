package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;

import de.renew.net.NetInstance;

import de.renew.remote.AggregateAccessor;
import de.renew.remote.AggregateAccessor.AggregateEnumerationAccessor;
import de.renew.remote.ObjectAccessor;

import de.renew.unify.Aggregate;
import de.renew.unify.List;

import java.rmi.RemoteException;


public class AggregateFigure extends HorizontalCompositeFigure {
    public AggregateFigure(AggregateAccessor aggregate, boolean expanded)
            throws RemoteException {
        FigureCreator fc = GuiPlugin.getCurrent().getFigureCreator();
        addAggregate(aggregate, fc, expanded);
        layout();
    }

    private void addAggregate(AggregateAccessor aggregate, FigureCreator fc,
                              boolean expanded) throws RemoteException {
        boolean isList = aggregate.isInstanceOf(List.class);
        add(new TextFigure(isList ? "{" : "[", true));
        boolean first = true;
        AggregateEnumerationAccessor elems = aggregate.elements();
        while (elems.hasMoreElements()) {
            if (first) {
                first = false;
            } else {
                add(new TextFigure(",", true));
            }

            ObjectAccessor token = elems.nextElement();
            if ((token != null) && token.isInstanceOf(Aggregate.class)) {
                addAggregate(token.asAggregate(), fc, expanded);
            } else {
                Figure tokenFig = fc.getTokenFigure(token, expanded);
                add(tokenFig);
                if ((token != null) && token.isInstanceOf(NetInstance.class)) {
                    addClickHandle(new NetInstanceHandle(tokenFig, null,
                                                         token.asNetInstance()));
                }
            }
        }
        if (isList) {
            ObjectAccessor openTail = elems.getOpenTail();
            if (openTail != null) {
                add(new TextFigure(":", true));
                add(fc.getTokenFigure(openTail, expanded));
            }
        }
        add(new TextFigure(isList ? "}" : "]", true));
    }

    /*
    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(fDisplayBox.x,fDisplayBox.y,fDisplayBox.width,fDisplayBox.height);
        g.fillRect(fDisplayBox.x,fDisplayBox.y,fDisplayBox.width,fDisplayBox.height);
        super.draw(g);
     }
    */
}