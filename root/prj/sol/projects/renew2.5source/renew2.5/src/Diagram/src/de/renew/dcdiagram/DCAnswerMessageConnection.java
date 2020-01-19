package de.renew.dcdiagram;

import CH.ifa.draw.figures.ArrowTip;

import CH.ifa.draw.framework.Figure;

import de.renew.diagram.MessageConnection;
import de.renew.diagram.TaskFigure;


public class DCAnswerMessageConnection extends MessageConnection {
    public DCAnswerMessageConnection() {
        super();
        //set LineStyle to dashed
        setAttribute("LineStyle", "10");
        //setEndDecoration(new ArrowTip(0.40, 10, 10, true));
        //setAttribute("ArrowTip", ArrowTip.class.getName());
    }

    @Override
    public void updatePeerNames() {
        logger.info("update answer name");

        Figure start = startFigure();
        if (start instanceof TaskFigure) {
            if (getMessageText().startsWith("web")) {
                ((TaskFigure) start).addPeerName("web");
            } else {
                ((TaskFigure) start).addPeerName("exchange");
            }
        }
    }

    @Override
    public boolean canConnect(Figure start, Figure end) {
        return (start instanceof TaskFigure && end instanceof TaskFigure);

    }
}