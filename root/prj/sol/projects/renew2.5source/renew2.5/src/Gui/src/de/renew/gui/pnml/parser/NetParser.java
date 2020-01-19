package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.pnml.converter.Converter;

import java.awt.Point;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * parse Nets
 */
public class NetParser extends ElementParser {
    //Biggest (int)Id found in ids;
    private int _maxId;

    //Mapping between PNML and renew ids (String => Int);
    private Hashtable<String, Integer> _mapping;
    private Drawing _net;

    public NetParser(Element element) {
        super(element);
        setMap(new Hashtable<String, Integer>());
        setMaxId(0);
    }

    protected int getMaxId() {
        return _maxId;
    }

    protected void setMaxId(int maxId) {
        _maxId = maxId;
    }

    protected Hashtable<String, Integer> map() {
        return _mapping;
    }

    protected void setMap(Hashtable<String, Integer> map) {
        _mapping = map;
    }

    protected void trySetMaxId(Integer id) {
        int value = id.intValue();
        if (getMaxId() < value) {
            setMaxId(value);
        }
    }

    public Drawing getNet() {
        return _net;
    }

    protected void setNet(Drawing net) {
        _net = net;
    }

    private void makeOffset() {
        int offsetX = 0;
        int offsetY = 0;
        FigureEnumeration elements = getNet().figures();

        //Find the highest offset
        while (elements.hasMoreElements()) {
            Figure element = elements.nextElement();
            Point pos = element.center();
            if (pos.x < offsetX) {
                offsetX = pos.x - (element.size().width / 2);
            }
            if (pos.y < offsetY) {
                offsetY = pos.y - (element.size().height / 2);
            }
        }

        //move all figures that dont depend on others by the found offset
        if (offsetX < 0 || offsetY < 0) {
            elements = getNet().figures();
            offsetX = offsetX * -1;
            offsetY = offsetY * -1;
            while (elements.hasMoreElements()) {
                Figure element = elements.nextElement();
                if (element instanceof ChildFigure) {
                    if (((ChildFigure) element).parent() == null) {
                        element.moveBy(offsetX, offsetY);
                    }
                } else {
                    element.moveBy(offsetX, offsetY);
                }
            }
        }
    }

    protected void addAnnotations(ObjectParser parser) {
        while (parser.hasMoreAnnotations()) {
            TextFigure annotation = parser.nextAnnotation();
            setMaxId(getMaxId() + 1);
            annotation.setID(getMaxId());
            getNet().add(annotation);
        }
    }

    protected void work(Iterator<ObjectParser> parsers) {
        while (parsers.hasNext()) {
            ObjectParser objectParser = parsers.next();
            if (!objectParser.hasNumericId()) {
                setMaxId(getMaxId() + 1);
                objectParser.setMappedId(new Integer(getMaxId()));
                map().put(objectParser.getPNMLId(), new Integer(getMaxId()));
            }
            getNet().add(objectParser.getFigure());
            addAnnotations(objectParser);
        }
    }

    public void doParse() {
        setNet(new CPNDrawing());
        String netType = getElement().getAttribute("type");
        Converter.instance().setType(netType);


        //List of found places
        List<ObjectParser> placeParsers = new LinkedList<ObjectParser>();


        //List of found transitions
        List<ObjectParser> transitionParsers = new LinkedList<ObjectParser>();


        // List of found arcs
        List<ObjectParser> arcParsers = new LinkedList<ObjectParser>();


        // List of found vitualPlaces
        List<ObjectParser> virPlaceParsers = new LinkedList<ObjectParser>();


        //Iterator to walk through the places, transitions and arcs
        Iterator<ObjectParser> parsers;

        NodeList places = getElement().getElementsByTagName("place");
        NodeList transitions = getElement().getElementsByTagName("transition");
        NodeList arcs = getElement().getElementsByTagName("arc");
        NodeList toolspecifics = getElement()
                                     .getElementsByTagName("toolspecific");

        // parse places and transitons
        for (int pos = 0; pos < places.getLength(); pos++) {
            Element place = (Element) places.item(pos);
            PlaceParser placeParser = new PlaceParser(place);
            placeParsers.add(placeParser);
            placeParser.parse();
            if (placeParser.hasNumericId()) {
                trySetMaxId(placeParser.getMappedId());
            }
        }
        for (int pos = 0; pos < transitions.getLength(); pos++) {
            Element transition = (Element) transitions.item(pos);
            TransitionParser transitionParser = new TransitionParser(transition);
            transitionParsers.add(transitionParser);
            transitionParser.parse();
            if (transitionParser.hasNumericId()) {
                trySetMaxId(transitionParser.getMappedId());
            }
        }
        for (int pos = 0; pos < arcs.getLength(); pos++) {
            Element arc = (Element) arcs.item(pos);
            ArcParser arcParser = new ArcParser(arc, map(), getNet());
            arcParsers.add(arcParser);
            arcParser.parse();
            if (arcParser.hasNumericId()) {
                trySetMaxId(arcParser.getMappedId());
            }
        }

        for (int pos = 0; pos < toolspecifics.getLength(); pos++) {
            Element toolspecific = (Element) toolspecifics.item(pos);
            if (toolspecific.getAttribute("tool").equals("renew")) {
                NodeList virPlaces = toolspecific.getElementsByTagName("VirtualPlace");
                if (virPlaces.getLength() > 0) {
                    Element virPlace = (Element) virPlaces.item(0);
                    VirtualplaceParser virPlaceParser = new VirtualplaceParser(virPlace,
                                                                               getNet());
                    virPlaceParsers.add(virPlaceParser);
                    virPlaceParser.parse();
                }
            }
        }

        NodeList childs = getElement().getChildNodes();
        for (int pos = 0; pos < childs.getLength(); pos++) {
            Node child = childs.item(pos);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) child;
                if (Converter.instance().getNetConverter().isAnnotation(element)) {
                    AnnotationParser annotationParser = new AnnotationParser((Element) child);
                    annotationParser.parse();
                    FigureWithID figure = annotationParser.getFigure();
                    figure.setID(getMaxId() + 1);
                    setMaxId(getMaxId() + 1);
                    // hide Annotation (Name)
                    if (figure instanceof CPNTextFigure) {
                        CPNTextFigure anno = (CPNTextFigure) figure;
                        if (anno.getType() == CPNTextFigure.INSCRIPTION) {
                            // parsed figure is not a name (i.e. the name of the net) 
                            // but a declaration node; try the next one
                            getNet().add(anno);
                            continue;
                        }
                        if (anno.getType() == CPNTextFigure.NAME) {
                            getNet().setName(anno.getText());
                            break;
                        }
                    }
                    getNet().add(figure);
                    break;
                }
            }
        }

        //Give any place, transition and arc a new int id and add them to the drawing    
        parsers = placeParsers.iterator();
        work(parsers);
        parsers = virPlaceParsers.iterator();
        work(parsers);
        parsers = transitionParsers.iterator();
        work(parsers);
        parsers = arcParsers.iterator();
        work(parsers);

        //Move the figures so all of them are visible
        makeOffset();
    }
}