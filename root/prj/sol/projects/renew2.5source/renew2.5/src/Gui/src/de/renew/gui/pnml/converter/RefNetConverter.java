package de.renew.gui.pnml.converter;

import org.w3c.dom.Element;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.DeclarationFigure;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.SyntaxException;


public class RefNetConverter extends NetConverterAbstract {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(RefNetConverter.class);

    public boolean isAttribute(String tagName) {
        boolean result = false;
        if (tagName.equals("type") || tagName.equals("placeType")) {
            result = true;
        }
        return result;
    }

    public boolean isAnnotation(Element label) {
        boolean result = false;
        String tagName = label.getNodeName();
        if (tagName.equals("initialMarking") || tagName.equals("name")
                    || tagName.equals("inscription")
                    || tagName.equals("annotation") || tagName.equals("label")
                    || tagName.equals("guard") || tagName.equals("uplink")
                    || tagName.equals("downlink") || tagName.equals("action")
                    || tagName.equals("create") || tagName.equals("expression")
                    || tagName.equals("declaration")) {
            result = true;
        }
        return result;
    }

    public TextFigure convertAnnotationToTextFigure(Element label) {
        TextFigure result = new TextFigure();
        String tagName = label.getNodeName();
        if (tagName.equals("initialMarking")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("guard")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("name")) {
            result = new CPNTextFigure(CPNTextFigure.NAME);
        } else if (tagName.equals("inscription")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("downlink")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("uplink")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("action")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("create")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("expression")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("declaration")) {
            result = new DeclarationFigure();
        } else if (tagName.equals("placeType")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else {
            result = new CPNTextFigure();
        }
        return result;
    }

    public String convertAnnotationToPNML(CPNTextFigure figure) {
        String result = "";
        ShadowNet net = Converter.instance().getShadowNet();
        int type = (figure.getType());
        switch (type) {
        case CPNTextFigure.NAME:
            result = "name";
            break;
        case CPNTextFigure.INSCRIPTION:
            result = "label";
            Figure parent = figure.parent();
            try {
                if (parent instanceof ArcConnection) {
                    result = net.checkArcInscription(figure.getText(), false);
                } else if (parent instanceof PlaceFigure) {
                    result = net.checkPlaceInscription(figure.getText(), false);
                } else if (parent instanceof TransitionFigure) {
                    result = net.checkTransitionInscription(figure.getText(),
                                                            false);
                }
            } catch (SyntaxException e) {
                logger.error(RefNetConverter.class.getSimpleName() + ": "
                             + e.getMessage() + "\nIt seems that the net "
                             + net.getName() + " contains a syntax error."
                             + "\nPlease, fix before exporting.");
                if (logger.isDebugEnabled()) {
                    logger.debug(RefNetConverter.class.getSimpleName() + ": "
                                 + RefNetConverter.class.getSimpleName() + e);
                }
            }

            break;
        case CPNTextFigure.LABEL:
            result = "label";
            break;
        default:
            throw new RuntimeException("Unkown type");
        }
        if (result == null) {
            result = "create";
        }
        return result;
    }

    public static boolean isNetParser(String netType) {
        if (netType.equals("RefNet")) {
            return true;
        }
        return false;
    }

    public String convertRenewNameToAttributeName(String type, String figure) {
        String result = type;
        if (figure.equals("place")) {
            if (type.equals("type")) {
                result = "placeType";
            }
        } else if (figure.equals("transition")) {
            result = type;
        } else if (figure.equals("arc")) {
            result = "type";
        }
        return result;
    }

    public String convertRenewValueToAttributeValue(String value, String type,
                                                    String figure) {
        String result = value;
        if (figure.equals("place")) {
            result = value;
        } else if (figure.equals("transition")) {
            result = value;
        } else if (figure.equals("arc")) {
            result = value;
        }
        return result;
    }

    public boolean isInRenewAnnotation(Element label) {
        boolean result = false;
        String tagName = label.getNodeName();
        if (tagName.equals("placeType")) {
            result = true;
        }
        return result;
    }
}