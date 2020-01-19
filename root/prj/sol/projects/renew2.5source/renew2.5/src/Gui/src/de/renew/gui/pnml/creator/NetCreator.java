package de.renew.gui.pnml.creator;

import org.w3c.dom.Element;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.DeclarationFigure;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;
import de.renew.gui.VirtualPlaceFigure;
import de.renew.gui.pnml.converter.Converter;
import de.renew.gui.pnml.converter.NetConverter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class NetCreator extends ElementCreator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(NetCreator.class);

    public NetCreator() {
        super("net");
    }

    private void connectNewStart(VirtualPlaceFigure virPlace, ArcConnection arc) {
        while (virPlace.getSemanticPlaceFigure() instanceof VirtualPlaceFigure) {
            virPlace = (VirtualPlaceFigure) virPlace.getSemanticPlaceFigure();
        }
        PlaceFigure figure = virPlace.getSemanticPlaceFigure();
        arc.disconnectStart();
        arc.connectStart(figure.connectorAt(figure.center()));
    }

    private void connectNewEnd(VirtualPlaceFigure virPlace, ArcConnection arc) {
        while (virPlace.getSemanticPlaceFigure() instanceof VirtualPlaceFigure) {
            virPlace = (VirtualPlaceFigure) virPlace.getSemanticPlaceFigure();
        }
        PlaceFigure figure = virPlace.getSemanticPlaceFigure();
        arc.disconnectEnd();
        arc.connectEnd(figure.connectorAt(figure.center()));
    }

    protected void doCreateElement() {
        CPNDrawing net = (CPNDrawing) getObject();
        String id = String.valueOf(System.currentTimeMillis());
        getElement().setAttribute("id", "\"" + "netId" + id + "\"");
        String nettype = Converter.instance().getType();
        getElement().setAttribute("type", "\"" + nettype + "\"");
        FigureEnumeration figures = net.figures();
        Converter.instance().setShadowNet(net);
        Hashtable<Integer, List<Figure>> viranno = new Hashtable<Integer, List<Figure>>();
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextElement();
            if (figure instanceof VirtualPlaceFigure) {
                VirtualPlaceFigure virPlace = (VirtualPlaceFigure) figure;
                VirtualPlaceCreator virCreator = new VirtualPlaceCreator();
                Element toolSpec = PNMLCreator.createElement("toolspecific");
                toolSpec.setAttribute("tool", "\"renew\"");
                toolSpec.setAttribute("version", "\"2.0\"");
                toolSpec.appendChild(virCreator.createElement(virPlace));
                getElement().appendChild(toolSpec);
                Integer orgid = new Integer(virPlace.getSemanticPlaceFigure()
                                                    .getID());
                List<Figure> annos;
                if (viranno.containsKey(orgid)) {
                    annos = viranno.get(orgid);
                } else {
                    annos = new LinkedList<Figure>();
                    viranno.put(orgid, annos);
                }
                FigureEnumeration figureenu = virPlace.children();
                while (figureenu.hasMoreElements()) {
                    Figure element = figureenu.nextFigure();
                    annos.add(element);
                }
            }
        }
        figures = net.figures();
        while (figures.hasMoreElements()) {
            Figure figure = figures.nextElement();
            if (figure instanceof PlaceFigure
                        && !(figure instanceof VirtualPlaceFigure)) {
                PlaceCreator placeCreator = new PlaceCreator();
                PlaceFigure place = (PlaceFigure) figure;
                Element pnmlPlace = placeCreator.createElement(figure);
                if (viranno.containsKey(new Integer(place.getID()))) {
                    List<Figure> annos = viranno.get(new Integer(place.getID()));
                    Iterator<Figure> iter = annos.iterator();
                    while (iter.hasNext()) {
                        Figure element = iter.next();
                        NetConverter con = Converter.instance().getNetConverter();
                        String pnmlValue = con.convertAnnotationToPNML((CPNTextFigure) element);
                        AnnotationCreator annCreator = new AnnotationCreator(pnmlValue);
                        annCreator.setParentVirtual(true);
                        pnmlPlace.appendChild(annCreator.createElement(element));
                    }
                }
                getElement().appendChild(pnmlPlace);
            } else if (figure instanceof TransitionFigure) {
                TransitionCreator transitionCreator = new TransitionCreator();
                getElement().appendChild(transitionCreator.createElement(figure));
            } else if (figure instanceof ArcConnection) {
                ArcConnection arc = (ArcConnection) figure;
                Figure sourceFigure = arc.startFigure();
                Figure targetFigure = arc.endFigure();
                ArcCreator arcCreator = new ArcCreator();
                if (sourceFigure instanceof VirtualPlaceFigure) {
                    VirtualPlaceFigure virPlace = (VirtualPlaceFigure) sourceFigure;
                    connectNewStart(virPlace, arc);
                    arcCreator.setVirStartId(virPlace.getID());
                }
                if (targetFigure instanceof VirtualPlaceFigure) {
                    VirtualPlaceFigure virPlace = (VirtualPlaceFigure) targetFigure;
                    connectNewEnd(virPlace, arc);
                    arcCreator.setVirEndId(virPlace.getID());
                }
                getElement().appendChild(arcCreator.createElement(figure));
            } else if (figure instanceof DeclarationFigure) {
                AnnotationCreator annotationCreator = new AnnotationCreator("declaration");
                getElement().appendChild(annotationCreator.createElement(figure));
            } else if (figure instanceof CPNTextFigure) {
                if (((CPNTextFigure) figure).parent() == null) {
                    logger.error("NetCreator: Unknown type -> "
                                 + figure.getClass());
                }
            }
        }

        // create Name
        AnnotationCreator name = new AnnotationCreator("name");
        getElement().appendChild(name.createAnnotation(net.getName()));

    }
}