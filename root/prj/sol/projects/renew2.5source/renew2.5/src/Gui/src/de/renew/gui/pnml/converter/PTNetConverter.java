package de.renew.gui.pnml.converter;

import org.w3c.dom.Element;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.SyntaxException;


public class PTNetConverter extends NetConverterAbstract {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PTNetConverter.class);

    public boolean isAttribute(String tagName) {
        boolean result = false;
        if (tagName.equals("type")) {
            result = true;
        }
        return result;
    }

    public boolean isAnnotation(Element label) {
        boolean result = false;
        String tagName = label.getNodeName();
        if (tagName.equals("initialMarking") || tagName.equals("name")
                    || tagName.equals("inscription")) {
            result = true;
        }
        return result;
    }

    public TextFigure convertAnnotationToTextFigure(Element label) {
        TextFigure result = new TextFigure();
        String tagName = label.getNodeName();
        if (tagName.equals("initialMarking")) {
            result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        } else if (tagName.equals("name")) {
            result = new CPNTextFigure(CPNTextFigure.NAME);
        } else if (tagName.equals("inscription")) {
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
                logger.error(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(PTNetConverter.class.getSimpleName() + ": \n"
                                 + e);
                }
            }

            break;
        default:
            throw new RuntimeException("Unkown type");
        }
        return result;
    }

    public static boolean isNetParser(String netType) {
        if (netType.equals("http://www.informatik.hu-berlin.de/top/pntd/ptNetb")) {
            return true;
        }
        return false;
    }

    public boolean isInRenewAnnotation(Element label) {
        boolean result = false;
        return result;
    }
}