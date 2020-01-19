package de.renew.gui.pnml.converter;

import org.w3c.dom.Element;

import CH.ifa.draw.figures.TextFigure;

import de.renew.gui.CPNTextFigure;


public class DefaultNetConverter extends NetConverterAbstract
        implements NetConverter {
    /* (non-Javadoc)
     * @see de.renew.gui.pnml.NetConverter#convertAnnotationToPNML(de.renew.gui.CPNTextFigure)
     */
    public String convertAnnotationToPNML(CPNTextFigure figure) {
        return null;
    }

    /* (non-Javadoc)
     * @see de.renew.gui.pnml.NetConverter#convertAnnotationToTextFigure(org.w3c.dom.Element)
     */
    public TextFigure convertAnnotationToTextFigure(Element annotation) {
        TextFigure result = new TextFigure();
        if (annotation.getTagName().equals("name")) {
            result = new CPNTextFigure(CPNTextFigure.NAME);
        } else {
            result = new CPNTextFigure(CPNTextFigure.LABEL);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see de.renew.gui.pnml.NetConverter#isAnnotation(org.w3c.dom.Element)
     */
    public boolean isAnnotation(Element label) {
        boolean result = true; // default Value
        String tag = label.getTagName();
        if (tag.equals("name")) {
            result = true;
        }
        return result;
    }

    /* (non-Javadoc)
     * @see de.renew.gui.pnml.NetConverter#isAttribute(org.w3c.dom.Element)
     */
    public boolean isAttribute(String tagName) {
        return false;
    }

    public static boolean isNetParser(String netType) {
        return true;
    }

    public boolean isInRenewAnnotation(Element label) {
        boolean result = false;
        return result;
    }
}