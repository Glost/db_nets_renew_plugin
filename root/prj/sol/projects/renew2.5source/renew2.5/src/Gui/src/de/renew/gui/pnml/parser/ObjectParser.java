package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.FigureWithID;
import CH.ifa.draw.framework.ParentFigure;

import de.renew.gui.pnml.converter.Converter;
import de.renew.gui.pnml.converter.NetConverter;

import java.awt.Point;

import java.util.LinkedList;
import java.util.List;


/**
 * parse and create PNML Objects (Nodes, Labels)
 */
public abstract class ObjectParser extends ElementParser {
    private FigureWithID _figure;
    private String _pnmlId;
    private Integer _mappedId;
    private List<AnnotationParser> _annotation;
    private List<AttributeParser> _attributes;
    private int _posAnnotation;
    private boolean _isParsed;
    private GraphicParser _parser;

    public ObjectParser(Element object) {
        super(object);
        setIsParsed(false);
        setMappedId(null);
        setAnnotations(new LinkedList<AnnotationParser>());
        setAttributes(new LinkedList<AttributeParser>());
        setPosAnnotations(0);
    }

    protected void setFigure(FigureWithID figure) {
        _figure = figure;
    }

    protected FigureWithID figure() {
        return _figure;
    }

    protected String getPNMLId() {
        return _pnmlId;
    }

    protected void setPNMLId(String id) {
        _pnmlId = id;
    }

    public Integer getMappedId() {
        return _mappedId;
    }

    public void setMappedId(Integer value) {
        _mappedId = value;
    }

    protected List<AnnotationParser> getAnnotations() {
        return _annotation;
    }

    protected void setAnnotations(List<AnnotationParser> list) {
        _annotation = list;
    }

    protected List<AttributeParser> getAttributes() {
        return _attributes;
    }

    protected void setAttributes(List<AttributeParser> list) {
        _attributes = list;
    }

    protected int getPosAnnotations() {
        return _posAnnotation;
    }

    protected void setPosAnnotations(int pos) {
        _posAnnotation = pos;
    }

    public boolean isParsed() {
        return _isParsed;
    }

    protected void setIsParsed(boolean isParsed) {
        _isParsed = isParsed;
    }

    protected GraphicParser getGraphic() {
        return _parser;
    }

    protected void setGraphic(GraphicParser parser) {
        _parser = parser;
    }

    public boolean hasNumericId() {
        boolean result = true;
        if (getMappedId() == null) {
            result = false;
        }
        return result;
    }

    protected void parseId() {
        setPNMLId(getElement().getAttribute("id"));
        try {
            setMappedId(new Integer(getPNMLId()));
        } catch (NumberFormatException e) {
        }
    }

    protected void parseGraphics() {
        NodeList chields = getElement().getChildNodes();
        Element element = null;
        for (int pos = 0; pos < chields.getLength(); pos++) {
            Node chield = chields.item(pos);
            if (chield.getNodeName().equals("graphics")) {
                element = (Element) chield;
                break;
            }
        }
        GraphicParser parser = new GraphicParser(element);
        parser.parse();
        setGraphic(parser);
    }

    private boolean isVirtual(Element anno) {
        boolean result = false;
        NodeList tools = anno.getElementsByTagName("toolspecific");
        for (int pos = 0; pos < tools.getLength(); pos++) {
            Element ele = (Element) tools.item(pos);
            if (ele.getAttribute("tool").equals("renew")) {
                NodeList vir = ele.getElementsByTagName("virtual");
                if (vir.getLength() != 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    protected void parseLabels() {
        NodeList chields = getElement().getChildNodes();
        for (int pos = 0; pos < chields.getLength(); pos++) {
            Node chield = chields.item(pos);
            if (chield.getNodeType() == Node.ELEMENT_NODE) {
                NetConverter con = Converter.instance().getNetConverter();
                Element label = (Element) chield;
                if (con.isAnnotation(label) || con.isInRenewAnnotation(label)) {
                    if (!isVirtual(label)) {
                        AnnotationParser parser = new AnnotationParser(label);
                        parser.parse();
                        getAnnotations().add(parser);
                    }
                } else if (con.isAttribute(label)) {
                    AttributeParser parser = new AttributeParser(label);
                    parser.parse();
                    getAttributes().add(parser);
                }
            }
        }
    }

    protected void parseFigure() {
    }

    protected void doParse() {
        parseId();
        parseGraphics();
        parseLabels();
        parseFigure();
        setIsParsed(true);
    }

    protected abstract FigureWithID createObject();

    protected abstract void initGraphic();

    protected void doInitFigure() {
        initGraphic();
    }

    public FigureWithID getFigure() {
        if (isParsed() == false) {
            parse();
        }
        setFigure(createObject());
        figure().setID(getMappedId().intValue());
        doInitFigure();
        FigureWithID result = figure();
        return result;
    }

    public boolean hasMoreAnnotations() {
        return getAnnotations().size() > getPosAnnotations();
    }

    public TextFigure nextAnnotation() {
        AnnotationParser parser = getAnnotations().get(getPosAnnotations());
        TextFigure result = parser.getFigure();
        Point point = result.displayBox().getLocation();
        result.setParent((ParentFigure) figure());
        if (point.x != 0 || point.y != 0) {
            result.moveBy(point.x, point.y);
        }
        setPosAnnotations(getPosAnnotations() + 1);
        return result;
    }

    protected void moveObject(int x, int y) {
        figure().moveBy(x, y);
    }
}