package de.renew.gui;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.ParentFigure;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowDeclarationNode;
import de.renew.shadow.ShadowInscribable;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.ShadowNode;
import de.renew.shadow.ShadowTransition;

import java.awt.Dimension;
import java.awt.Point;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import java.net.URL;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;


public class ShadowNetSystemRenderer {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ShadowNetSystemRenderer.class);

    public static ShadowNetSystem readShadowNetSystem(URL location) {
        ShadowNetSystem netSystem = null;
        InputStream stream = null;
        try {
            stream = location.openStream();
            ObjectInput input = new ObjectInputStream(stream);
            netSystem = (ShadowNetSystem) input.readObject();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            logger.error("Could not load net system from " + location + ".");
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
            logger.error("Could not load net system from " + location + ".");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // ignore it.
                }
            }
        }
        return netSystem;
    }

    public static CPNDrawing[] render(URL location) {
        ShadowNetSystem netSystem = readShadowNetSystem(location);
        Collection<ShadowNet> nets = netSystem.elements();
        Iterator<ShadowNet> iterator = nets.iterator();
        CPNDrawing[] drawings = new CPNDrawing[nets.size()];
        for (int i = 0; iterator.hasNext(); ++i) {
            ShadowNet net = iterator.next();
            drawings[i] = new ShadowNetRenderer(net).drawing;
        }
        return drawings;
    }
}

class ShadowNetRenderer {
    CPNDrawing drawing = new CPNDrawing();
    private Hashtable<ShadowInscribable, ParentFigure> lookup = new Hashtable<ShadowInscribable, ParentFigure>();
    private Point loc = new Point(100, 20);

    ShadowNetRenderer(ShadowNet net) {
        render(net);
    }

    private void render(ShadowNet net) {
        drawing.setName(net.getName());
        Iterator<ShadowNetElement> iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof ShadowNode) {
                renderNode((ShadowNode) element);
            }
            if (element instanceof ShadowDeclarationNode) {
                renderDeclarationNode((ShadowDeclarationNode) element);
            }
        }
        iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof ShadowArc) {
                renderArc((ShadowArc) element);
            }
        }
        iterator = net.elements().iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof ShadowInscription) {
                renderInscription((ShadowInscription) element);
            }
        }
    }

    private void renderDeclarationNode(ShadowDeclarationNode declNode) {
        DeclarationFigure createdFig = new DeclarationFigure();
        createdFig.setText(declNode.inscr);
        drawing.add(createdFig);
    }

    private void renderNode(ShadowNode node) {
        Dimension dim = null;
        ParentFigure createdFig = null;
        if (node instanceof ShadowTransition) {
            createdFig = new TransitionFigure();
            dim = TransitionFigure.defaultDimension();
        } else {
            createdFig = new PlaceFigure();
            dim = PlaceFigure.defaultDimension();
        }
        drawing.add(createdFig);
        if (node.getName() != null) {
            CPNTextFigure nameFig = new CPNTextFigure(CPNTextFigure.NAME);
            nameFig.setText(node.getName());
            int nameWidth = nameFig.displayBox().width;
            if (nameWidth + 8 > dim.width) {
                dim.width = nameWidth + 8;
            }
            drawing.add(nameFig);
            nameFig.setParent(createdFig);
        }
        Point rightLower = new Point(dim.width, dim.height);
        createdFig.displayBox(new Point(), rightLower);
        createdFig.moveBy(loc.x, loc.y);


        //loc.y += 50;
        lookup.put(node, createdFig);
        createdFig.setAttribute("TraceMode", new Boolean(node.getTrace()));
    }

    private void renderArc(ShadowArc arc) {
        int shadowArcType = arc.shadowArcType;

        Figure startFig = lookup.get(arc.place);
        Figure endFig = lookup.get(arc.transition);
        if (!arc.placeToTransition) {
            Figure helpFig = startFig;
            startFig = endFig;
            endFig = helpFig;
        }

        ArcConnection arcFig = null;
        switch (shadowArcType) {
        case ShadowArc.ordinary:
        case ShadowArc.test:
        case ShadowArc.both:
            arcFig = new ArcConnection(shadowArcType);
            break;
        case ShadowArc.inhibitor:
            arcFig = new InhibitorConnection();
            break;
        case ShadowArc.doubleOrdinary:
            arcFig = new DoubleArcConnection();
            break;
        case ShadowArc.doubleHollow:
            arcFig = new HollowDoubleArcConnection();
            break;
        default:
            throw new RuntimeException("Bad shadow arc type.");
        }

        arcFig.startPoint(0, 0);
        arcFig.endPoint(0, 0);
        drawing.add(arcFig);
        lookup.put(arc, arcFig);
        arcFig.connectStart(startFig.connectorAt(0, 0));
        arcFig.connectEnd(endFig.connectorAt(0, 0));
        arcFig.updateConnection();
        arcFig.setAttribute("TraceMode", new Boolean(arc.getTrace()));
    }

    private void renderInscription(ShadowInscription inscription) {
        CPNTextFigure inscrFig = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        inscrFig.setText(inscription.inscr);
        ParentFigure parent = lookup.get(inscription.inscribable);
        drawing.add(inscrFig);
        inscrFig.setParent(parent);
        inscrFig.setAttribute("TraceMode", new Boolean(inscription.getTrace()));
        if (parent instanceof ArcConnection) {
            inscrFig.setAttribute("FillColor",
                                  CH.ifa.draw.util.ColorMap.BACKGROUND);
        }
    }
}