package de.renew.dcdiagram;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.RelativeLocator;

import de.renew.diagram.ActionTextFigure;
import de.renew.diagram.DCServiceTextFigure;
import de.renew.diagram.DiagramFigure;
import de.renew.diagram.DiagramTextFigure;
import de.renew.diagram.IDiagramElement;
import de.renew.diagram.MessageConnection;
import de.renew.diagram.MessageConnectionHandle;
import de.renew.diagram.RoleDescriptorFigure;
import de.renew.diagram.TaskFigure;
import de.renew.diagram.peer.DrawingPeer;

import java.awt.Color;
import java.awt.Point;

import java.util.Iterator;
import java.util.Vector;


public class DCTaskFigure extends TaskFigure {
    private Vector<Figure> messages;
    private Vector<IDiagramElement> originators;
    private boolean hasStart;
    static final Point START_LOCATION = new Point(800, 400);
    static final Point INIT_LOCATION = new Point(50, 100);

    public DCTaskFigure() {
        super();
        messages = new Vector<Figure>();
        originators = new Vector<IDiagramElement>();
        //setAttribute("LineStyle", LINE_STYLE_DASH_DOTTED);
        hasStart = true;
    }

    @Override
    public void addPeer(String fileName) {
        Point loc = calculateLocation();
        addPeer(new DCDrawingPeer(loc, fileName, this, this));
        if (logger.isDebugEnabled()) {
            logger.debug("------------------------------------------------- ");
            logger.debug(" ");
            logger.debug("(((((((((((((((((((((((((((((((((((((((((((((( ");
            logger.debug("TailableFigure " + this);
            logger.debug("Parent " + getDParents().firstElement());
            logger.debug("Head " + getDHead());
            logger.debug(")))))))))))))))))))))))))))))))))))))))))))))) ");
        }
    }

    @Override
    public void updatePeers() {
        drawingPeers = new Vector<DrawingPeer>();
        Iterator<IDiagramElement> it = originators.iterator();

        while (it.hasNext()) {
            IDiagramElement figure = it.next();

            //String name = (String) it.next();
            if (figure instanceof RoleDescriptorFigure) {
                addPeer(new DCDrawingPeer(INIT_LOCATION, "dc-init.rnw", this,
                                          this));
                hasStart = false;
            } else {
                if (figure instanceof ActionTextFigure) {
                    if (hasStart) {
                        Point loc = calculateLocation();
                        if (((ActionTextFigure) figure).getText()
                                     .equals(":stop()")
                                    || ((ActionTextFigure) figure).getText()
                                                .equals("stop")) {
                            addPeer(new DrawingPeer(loc, "stop.rnw", this,
                                                    figure));
                        } else if (((ActionTextFigure) figure).getText()
                                            .equals("manual")
                                           || ((ActionTextFigure) figure).getText()
                                                       .equals("manual;")) {
                            addPeer(new DrawingPeer(loc, "manual.rnw", this,
                                                    figure));
                        } else {
                            addPeer(new DrawingPeer(loc,
                                                    "sequence-template.rnw",
                                                    this, figure));
                        }
                    } else if (((ActionTextFigure) figure).getText()
                                        .equals("manual")
                                       || ((ActionTextFigure) figure).getText()
                                                   .equals("manual;")) {
                        addPeer(new DrawingPeer(START_LOCATION, "manual.rnw",
                                                this, figure));
                        hasStart = true;

                    }
                } else if (figure instanceof DCServiceTextFigure && hasStart) {
                    DCServiceTextFigure textFigure = (DCServiceTextFigure) figure;
                    String rnwFile = "exchange.rnw";
                    if (textFigure.getText().startsWith("simple")) {
                        rnwFile = "simple-exchange.rnw";
                    }
                    Point loc = calculateLocation();
                    addPeer(new DrawingPeer(loc, rnwFile, this, figure));
                } else if (figure instanceof MessageConnection
                                   && !(figure instanceof DCAnswerMessageConnection)) {
                    MessageConnection message = (MessageConnection) figure;
                    DiagramFigure start = (DiagramFigure) message.startFigure();
                    DiagramFigure end = (DiagramFigure) message.endFigure();
                    String text = message.getMessageText();

                    if (start instanceof DCTaskFigure
                                && end instanceof DCTaskFigure) {
                        if (text != null) {
                            if (text.startsWith("web inform")) {
                                if (start.equals(this) && hasStart) {
                                    Point loc = calculateLocation();
                                    addPeer(new DCDrawingPeer(loc,
                                                              "dc-web-send-inform.rnw",
                                                              this, figure));
                                }

                                if (end.equals(this)) {
                                    Point loc = null;
                                    if (hasStart) {
                                        loc = calculateLocation();
                                    } else {
                                        loc = START_LOCATION;
                                        hasStart = true;
                                    }
                                    addPeer(new DCDrawingPeer(loc,
                                                              "dc-web-receive-inform.rnw",
                                                              this, figure));
                                }
                            } else if (text.startsWith("web request")) {
                                if (start.equals(this) && hasStart) {
                                    Point loc = calculateLocation();
                                    addPeer(new DCDrawingPeer(loc,
                                                              "dc-web-send-request.rnw",
                                                              this, figure));
                                }

                                if (end.equals(this)) {
                                    Point loc = null;
                                    if (hasStart) {
                                        loc = calculateLocation();
                                    } else {
                                        loc = START_LOCATION;
                                        hasStart = true;
                                    }
                                    addPeer(new DCDrawingPeer(loc,
                                                              "dc-web-receive-request.rnw",
                                                              this, figure));
                                }
                            } else if (text.startsWith("simple")) {
                                if (start.equals(this) && hasStart) {
                                    Point loc = calculateLocation();
                                    addPeer(new DCDrawingPeer(loc,
                                                              "simple-exchange.rnw",
                                                              this, figure));
                                }

                                if (end.equals(this)) {
                                    Point loc = null;
                                    if (hasStart) {
                                        loc = calculateLocation();
                                    } else {
                                        loc = START_LOCATION;
                                        hasStart = true;
                                    }
                                    addPeer(new DCDrawingPeer(loc,
                                                              "dc-simple-call.rnw",
                                                              this, figure));
                                }
                            } else {
                                if (start.equals(this) && hasStart) {
                                    Point loc = calculateLocation();
                                    addPeer(new DCDrawingPeer(loc,
                                                              "exchange.rnw",
                                                              this, figure));
                                }

                                if (end.equals(this)) {
                                    Point loc = null;
                                    if (hasStart) {
                                        loc = calculateLocation();
                                    } else {
                                        loc = START_LOCATION;
                                        hasStart = true;
                                    }
                                    addPeer(new DCDrawingPeer(loc,
                                                              "dc-call.rnw",
                                                              this, figure));
                                }
                            }
                        } else {
                            //If there is no text, the default case is a default dcexchange
                            if (start.equals(this) && hasStart) {
                                Point loc = calculateLocation();
                                addPeer(new DCDrawingPeer(loc, "exchange.rnw",
                                                          this, figure));
                            }

                            if (end.equals(this)) {
                                Point loc = null;
                                if (hasStart) {
                                    loc = calculateLocation();
                                } else {
                                    loc = START_LOCATION;
                                    hasStart = true;
                                }
                                addPeer(new DCDrawingPeer(loc, "dc-call.rnw",
                                                          this, figure));
                            }
                        }
                    } else if (start.equals(this)
                                       && !(end instanceof DCTaskFigure)) {
                        if (hasStart) {
                            Point loc = calculateLocation();

                            addPeer(new DCDrawingPeer(loc, "dc-call.rnw", this,
                                                      figure));
                            Point loc2 = calculateLocation();
                            addPeer(new DCDrawingPeer(loc2, "dc-return.rnw",
                                                      this, figure));

                        }
                    } else if (!(start instanceof DCTaskFigure)
                                       && end.equals(this)) {
                        if (text != null) {
                            if (text.startsWith("simple")) {
                                Point loc = null;
                                if (hasStart) {
                                    loc = calculateLocation();
                                } else {
                                    loc = START_LOCATION;
                                    hasStart = true;
                                }
                                addPeer(new DCDrawingPeer(loc,
                                                          "dc-simple-call.rnw",
                                                          this, figure));

                            } else {
                                Point loc = null;
                                if (hasStart) {
                                    loc = calculateLocation();
                                } else {
                                    loc = START_LOCATION;
                                    hasStart = true;
                                }
                                addPeer(new DCDrawingPeer(loc, "dc-call.rnw",
                                                          this, figure));
                            }
                        } else {
                            Point loc = null;
                            if (hasStart) {
                                loc = calculateLocation();
                            } else {
                                loc = START_LOCATION;
                                hasStart = true;
                            }
                            addPeer(new DCDrawingPeer(loc, "dc-call.rnw", this,
                                                      figure));
                        }
                    }
                } else if (figure instanceof DCAnswerMessageConnection) {
                    MessageConnection message = (MessageConnection) figure;
                    DiagramFigure start = (DiagramFigure) message.startFigure();
                    DiagramFigure end = (DiagramFigure) message.endFigure();
                    String text = message.getMessageText();

                    if (start.equals(this)) {
                        if (text != null && text.startsWith("web")) {
                            Point loc = calculateLocation();
                            addPeer(new DCDrawingPeer(loc,
                                                      "dc-web-send-answer.rnw",
                                                      this, figure));
                        } else {
                            Point loc = calculateLocation();
                            addPeer(new DCDrawingPeer(loc, "dc-return.rnw",
                                                      this, figure));
                        }
                    }

                    if (end.equals(this)) {
                        if (!(start instanceof DCTaskFigure)) {
                            Point loc = calculateLocation();
                            addPeer(new DCDrawingPeer(loc,
                                                      "dc-call-with-id.rnw",
                                                      this, figure));
                        } else if (text != null) {
                            if (text.startsWith("web")) {
                                Point loc = calculateLocation();
                                addPeer(new DCDrawingPeer(loc,
                                                          "dc-web-receive-answer.rnw",
                                                          this, figure));
                            }
                        }
                    }
                }
            }
        }
        logger.debug("Peers = " + getPeers());
        if (!hasStart) {
            logger.warn("DCTaskFigure has no incoming messages or manual start. Manual start automatically generated.");
            addPeer(new DrawingPeer(START_LOCATION, "manual.rnw", this,
                                    new ActionTextFigure("manual")));
            hasStart = true;
        }
    }

    @Override
    public void updatePeerNames() {
        //this stays empty
        if (getDParents().firstElement() instanceof RoleDescriptorFigure) {
            addPeerName("dc-init");
            originators.add(getDHead());
        }
    }

    @Override
    public Vector<Handle> handles() {
        Vector<Handle> handles = super.handles();
        //remove the messageConnectionHandle added via supermethod
        handles.remove(handles.size() - 2);
        handles.addElement(new MessageConnectionHandle(this,
                                                       new RelativeLocator(0.5,
                                                                           0.45),
                                                       new MessageConnection()));
        handles.addElement(new MessageConnectionHandle(this,
                                                       new RelativeLocator(0.5,
                                                                           0.65),
                                                       new DCAnswerMessageConnection()));

        return handles;
    }

    /**
     * here the mapping of Figures and peer names takes place.
     * Example: an ActionTextFigure is represented by its peer sequence.
     */
    @Override
    public void handleMessages() {
        updatePeerNames();


        // also add actions...
        Iterator<Figure> iter = children.iterator();
        while (iter.hasNext()) {
            Figure fig = iter.next();
            if (fig instanceof DiagramTextFigure) {
                messages.add(fig);
            }
        }
        sortByYPosition(messages, this);
        Iterator<Figure> it = messages.iterator();
        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof ActionTextFigure) {
                logger.debug("start (Place) " + this);
                addPeerName("sequence-template");
                originators.add((IDiagramElement) o);
            } else if (o instanceof DCServiceTextFigure) {
                logger.debug("Exchange: start (Place) " + this);
                addPeerName("exchange");
                originators.add((IDiagramElement) o);
            } else if (o instanceof MessageConnection) {
                MessageConnection message = (MessageConnection) o;
                DiagramFigure start = (DiagramFigure) message.startFigure();
                String text = message.getMessageText();

                if (start.equals(this)) {
                    if (text != null && text.startsWith("web")) {
                        logger.debug("start (Place) " + this);
                        addPeerName("web");
                        originators.add((IDiagramElement) o);
                    } else {
                        logger.debug("start (Place) " + this);
                        addPeerName("exchange");
                        originators.add((IDiagramElement) o);
                    }
                }
                DiagramFigure end = (DiagramFigure) message.endFigure();
                if (end.equals(this)) {
                    if (text != null && text.startsWith("web")) {
                        logger.debug("start (Place) " + this);
                        addPeerName("web");
                        originators.add((IDiagramElement) o);
                    } else {
                        logger.debug("start (Place) " + this);
                        addPeerName("dc-call");
                        originators.add((IDiagramElement) o);
                    }
                }
            } else if (o instanceof DCAnswerMessageConnection) {
                MessageConnection message = (MessageConnection) o;
                DiagramFigure start = (DiagramFigure) message.startFigure();
                String text = message.getMessageText();

                if (start.equals(this)) {
                    if (text != null && text.startsWith("web")) {
                        logger.debug("start (Place) " + this);
                        addPeerName("web");
                        originators.add((IDiagramElement) o);
                    } else {
                        logger.debug("start (Place) " + this);
                        addPeerName("exchange");
                        originators.add((IDiagramElement) o);
                    }
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.warn(TaskFigure.class.getName() + ": "
                                + "Some (text) figure is "
                                + "attached that is not an Action, DC exchange nor a Message.\n"
                                + "This could be a mistake.");
                    if (logger.isDebugEnabled()) {
                        logger.debug("   class is of type: "
                                     + o.getClass().getName());
                        if (o instanceof TextFigure) {
                            TextFigure textfigure = (TextFigure) o;
                            logger.debug("   text is         : "
                                         + textfigure.getText());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void flushMessages() {
        messages = new Vector<Figure>();
    }

    @Override
    public void flushPeers() {
        originators = new Vector<IDiagramElement>();
        super.flushPeers();
    }

    @Override
    public void addMessage(MessageConnection connection) {
        messages.add(connection);
    }
}