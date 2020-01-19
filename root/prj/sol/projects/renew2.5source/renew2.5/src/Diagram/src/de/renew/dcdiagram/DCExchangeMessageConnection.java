package de.renew.dcdiagram;

import CH.ifa.draw.figures.ArrowTip;

import CH.ifa.draw.framework.Figure;

import de.renew.diagram.MessageConnection;
import de.renew.diagram.TaskFigure;

import de.renew.gui.AssocArrowTip;


public class DCExchangeMessageConnection extends MessageConnection {
    public DCExchangeMessageConnection() {
        super();
        //setAttribute("ArrowTip", "DCMessageArrowTip");
        //setEndDecoration(new ArrowTip(0.40, 10, 10, true));
        //setAttribute("ArrowTip", ArrowTip.class.getName());
    }

    @Override
    public void updatePeerNames() {
        Figure start = startFigure();
        if (start instanceof TaskFigure) {
            if (getMessageText().startsWith("web")) {
                ((TaskFigure) start).addPeerName("web");
            } else {
                ((TaskFigure) start).addPeerName("exchange");
            }
        }
        Figure end = endFigure();
        if (end instanceof TaskFigure) {
            if (getMessageText().startsWith("web")) {
                ((TaskFigure) start).addPeerName("web");
            } else {
                ((TaskFigure) start).addPeerName("dc-call");
            }
        }
    }

    @Override
    public boolean canConnect(Figure start, Figure end) {
        return (start instanceof DCTaskFigure && end instanceof DCTaskFigure);

    }
}