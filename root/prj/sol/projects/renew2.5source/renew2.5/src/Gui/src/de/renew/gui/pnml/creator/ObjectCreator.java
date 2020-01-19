package de.renew.gui.pnml.creator;

import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;
import CH.ifa.draw.framework.ParentFigure;

import de.renew.gui.CPNTextFigure;
import de.renew.gui.pnml.converter.Converter;
import de.renew.gui.pnml.converter.NetConverter;


public class ObjectCreator extends ElementCreator {
    public ObjectCreator(String tag) {
        super(tag);
    }

    protected FigureWithID getFigure() {
        return (FigureWithID) getObject();
    }

    protected void doCreateElement() {
        getElement()
            .setAttribute("id",
                          "\"" + String.valueOf(getFigure().getID()) + "\"");
        if (getFigure() instanceof ParentFigure) {
            FigureEnumeration chields = ((ParentFigure) getFigure())
                                            .children();
            while (chields.hasMoreElements()) {
                CPNTextFigure annotation = (CPNTextFigure) chields.nextElement();
                NetConverter con = Converter.instance().getNetConverter();
                String pnmlValue = con.convertAnnotationToPNML(annotation);
                if (con.isAttribute(pnmlValue)) {
                    String pnmlAttribute = con.convertRenewNameToAttributeName(pnmlValue,
                                                                               "place");

                    String pnmlAttributeValue = con
                                                    .convertRenewValueToAttributeValue(annotation
                                                                                       .getText(),
                                                                                       pnmlAttribute,
                                                                                       "place");

                    AttributeCreator ac = new AttributeCreator(pnmlAttribute);
                    getElement()
                        .appendChild(ac.createElement(pnmlAttributeValue));
                } else {
                    AnnotationCreator annCreator = new AnnotationCreator(pnmlValue);
                    getElement()
                        .appendChild(annCreator.createElement(annotation));
                }
            }
        }
        doCreateAttribute();
        createGraphic();
        doCreateObject();
    }

    protected void createGraphic() {
        GraphicCreator graphicCreator = new GraphicCreator();
        doCreateGraphic(graphicCreator);
        getElement().appendChild(graphicCreator.createGraphic());
    }

    /**
     *
     * @param creator UNUSED
     *
     * @author Eva Mueller
     * @date Dec 3, 2010
     * @version 0.1
     */
    protected void doCreateGraphic(GraphicCreator creator) {
    }

    protected void doCreateObject() {
    }

    protected void doCreateAttribute() {
    }
}