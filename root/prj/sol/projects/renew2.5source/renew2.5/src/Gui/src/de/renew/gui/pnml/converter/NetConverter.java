package de.renew.gui.pnml.converter;

import org.w3c.dom.Element;

import CH.ifa.draw.figures.TextFigure;

import de.renew.gui.CPNTextFigure;


public interface NetConverter {
    public boolean isAttribute(Element label);

    public boolean isAttribute(String tagName);

    public boolean isAnnotation(Element label);

    public boolean isInRenewAnnotation(Element label);

    public TextFigure convertAnnotationToTextFigure(Element annotation);

    public String convertAnnotationToPNML(CPNTextFigure figure);

    public String convertAttributNameToRenewName(Element attribute);

    public String convertAttributValueToRenewValue(String value, String type,
                                                   String figure);

    public String convertRenewNameToAttributeName(String type, String figure);

    public String convertRenewValueToAttributeValue(String value, String type,
                                                    String figure);
}