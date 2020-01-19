/*
 * Created on May 14, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.framework.ConnectionFigure;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Figure;

import java.awt.event.MouseEvent;


/**
 * Draws a new Message Connection between two TaskFigures or HSplitFigures
 *
 * @author Lawrence Cabac
 */
public class MessageConnectionTool extends ConnectionTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MessageConnectionTool.class);

    public MessageConnectionTool(DrawingEditor editor,
                                 ConnectionFigure prototype) {
        super(editor, prototype);
    }


    /**
     * Connects the figures if the mouse is released over another
     * figure.
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        Figure c = null;
        if (getFStartConnector() != null) {
            c = findTarget(e.getX(), e.getY(), drawing());
        }

        if (c != null) {
            setFEndConnector(findConnector(e.getX(), e.getY(), c));
            if (getFEndConnector() != null) {
                getFConnection().connectStart(getFStartConnector());
                getFConnection().connectEnd(getFEndConnector());
                getFConnection().updateConnection();

                MessageConnection message = (MessageConnection) getFConnection();
                DiagramFigure start = (DiagramFigure) message.startFigure();
                if (start instanceof TaskFigure) {
                    TaskFigure task = (TaskFigure) start;
                    logger.debug("start (Place) " + task);
                    task.addPeerName("out");
                }
                DiagramFigure end = (DiagramFigure) message.endFigure();
                if (end instanceof TaskFigure) {
                    TaskFigure task = (TaskFigure) end;
                    task.addPeerName("in");
                }
            }
            if (generatePeerInstantaneously(view())) {
                ((TailFigure) c).generatePeers();
            }
        } else if (getFConnection() != null) {
            view().remove(getFConnection());
            noChangesMade();
        }


        setFConnection(null);
        setFStartConnector(null);
        setFEndConnector(null);
        editor().toolDone();
    }
}