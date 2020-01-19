package de.renew.gui.pnml.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import CH.ifa.draw.figures.TextFigure;

import de.renew.gui.pnml.converter.Converter;
import de.renew.gui.pnml.converter.NetConverter;


/**
 * parse Annotations
 */
public class AnnotationParser extends ElementParser {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(AnnotationParser.class);
    private String _text;
    private int _offSetX = 0;
    private int _offSetY = 0;

    /**Create an XMLParser that parses PNML annotations
     * @param annotation XML element to parse
     */
    public AnnotationParser(Element annotation) {
        super(annotation);
    }

    /**
     * @return the text saved for this annotation
     */
    protected String getText() {
        String result = "";
        if (_text != null) {
            result = _text;
        }
        return result;
    }

    /**Save a given text string as this annotations text
     * @param text text to be saved in for this annotation
     */
    protected void setText(String text) {
        _text = text;
    }

    /**
     * @return the x offset of the annotation
     */
    protected int offSetX() {
        return _offSetX;
    }

    /**Set the x offset of the annotaion
     * @param offset to be saved as x offset
     */
    protected void setOffSetX(int offset) {
        _offSetX = offset;
    }

    /**
     * @return the y offset of the annotation
     */
    protected int offSetY() {
        return _offSetY;
    }

    /**Set the y offset of the annotation
     * @param offset to be saved as y offset
     */
    protected void setOffSetY(int offset) {
        _offSetY = offset;
    }

    /**
     * Parse the annotation saved with this object and save the values found
     */
    protected void doParse() {
        NodeList texte = getElement().getElementsByTagName("text");
        if (texte.getLength() == 0) {
            texte = getElement().getElementsByTagName("value");
        }
        for (int pos = 0; pos < texte.getLength(); pos++) {
            Element textEle = (Element) texte.item(pos);
            Text text = (Text) textEle.getFirstChild();
            if (text != null) {
                setText(text.getData());
            }
        }
        NodeList graphics = getElement().getElementsByTagName("graphics");
        for (int pos = 0; pos < graphics.getLength(); pos++) {
            Element graphic = (Element) graphics.item(pos);
            GraphicParser parser = new GraphicParser(graphic);
            parser.parse();
            setOffSetX(parser.offsetX());
            setOffSetY(parser.offsetY());
        }
    }

    /**
     * The TextFigure object returned represents the annotation that has been parsed
     * <p>
     * This method requires that parse has been called before.
     * </p>
     * @return a renew TextFigure
     */
    public TextFigure getFigure() {
        TextFigure result;
        NetConverter con = Converter.instance().getNetConverter();
        result = con.convertAnnotationToTextFigure(getElement());
        result.setText(getText());
        result.moveBy(offSetX(), offSetY());
        if (logger.isDebugEnabled()) {
            logger.debug(AnnotationParser.class.getSimpleName()
                         + ": PMNL text figure detected: "
                         + result.getClass().getSimpleName() + " "
                         + result.getText());
        }
        return result;
    }
}