package de.renew.gui.pnml.creator;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.ParentFigure;

import de.renew.gui.DeclarationFigure;

import java.awt.Point;


/**
 * @author 0schlein
 */
public class AnnotationCreator extends ElementCreator {
    private boolean parentVirtual = false;

    /**Create an XMLCreator that parses Renew annotations
         * @param tag name to use for saveing the parsed annotation
         */
    public AnnotationCreator(String tag) {
        super(tag);
    }

    public void setParentVirtual(boolean value) {
        parentVirtual = value;
    }

    /**
         * @return the saved object cast to a renew TextFigure
         */
    protected TextFigure getTextFigure() {
        return (TextFigure) getObject();
    }

    /* (non-Javadoc)
         * @see de.renew.gui.pnml.ElementCreator#doCreateElement()
         */
    protected void doCreateElement() {
        Text text = PNMLCreator.createTextNode("text");
        text.setData(getTextFigure().getText());
        createGraphic();
        getElement().appendChild(text);
        if (parentVirtual) {
            Element toolSpec = PNMLCreator.createElement("toolspecific");
            toolSpec.setAttribute("tool", "\"renew\"");
            toolSpec.setAttribute("version", "\"2.0\"");
            Element vir = PNMLCreator.createElement("virtual");
            toolSpec.appendChild(vir);
            getElement().appendChild(toolSpec);
        }
    }

    /**
     * parse the grahpical information saved with this annotation and append it to the created xml element
         */
    protected void createGraphic() {
        GraphicCreator graphicCreator = new GraphicCreator();
        Point center = getTextFigure().center();
        ParentFigure parent = getTextFigure().parent();
        if (parent == null) {
            if (!(getTextFigure() instanceof DeclarationFigure)) {
                graphicCreator.addOffset(center.x, center.y);
            } else {
                Point origin = getTextFigure().getOrigin();
                graphicCreator.addOffset(origin.x, origin.y);
            }
        } else {
            Point centerParent = parent.center();
            graphicCreator.addOffset(center.x - centerParent.x,
                                     center.y - centerParent.y);
        }
        getElement().appendChild(graphicCreator.getElement());
    }

    public Element createAnnotation(String text) {
        Text textEle = PNMLCreator.createTextNode("text");
        textEle.setData(text);
        getElement().appendChild(textEle);
        return getElement();
    }
}