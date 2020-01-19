package de.renew.gui.fs;

import CH.ifa.draw.figures.ElbowHandle;
import CH.ifa.draw.figures.ElbowTextLocator;
import CH.ifa.draw.figures.PolyLineFigure;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.Locator;

import CH.ifa.draw.standard.ChangeConnectionEndHandle;
import CH.ifa.draw.standard.ChangeConnectionStartHandle;
import CH.ifa.draw.standard.NullHandle;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.formalism.fs.ShadowConcept;
import de.renew.formalism.fs.ShadowIsa;

import de.renew.gui.IsaArrowTip;

import de.renew.shadow.ShadowNetElement;

import java.awt.Color;
import java.awt.Point;

import java.io.IOException;

import java.util.Vector;


public class IsaConnection extends ConceptConnection {

    /**
     * Determines type and appearance of the is-a relation.
     * <p>
     * <code>true</code>:  ??? <br>
     * <code>false</code>: ???
     * </p>
     * @serial
     **/
    private boolean isDisjunctive;

    public IsaConnection() {
        this(true);
    }

    public IsaConnection(boolean isDisjunctive) {
        super(new IsaArrowTip());
        this.isDisjunctive = isDisjunctive;
        setFillColor(Color.white);
    }

    protected ShadowNetElement createShadow(ShadowConcept from, ShadowConcept to) {
        // logger.debug("creating shadow for is-a relation between "+from.getName()+" and "+to.getName());
        return new ShadowIsa(from, to, isDisjunctive);
    }

    public boolean isDisjunctive() {
        return isDisjunctive;
    }

    public void setAttribute(String name, Object value) {
        if (name.equals("IsaType")) {
            isDisjunctive = ((Boolean) value).booleanValue();
            willChange();
            if (!isDisjunctive && fPoints.size() > 2) {
                // remove all intermediate points
                Point start = startPoint();
                Point end = endPoint();
                fPoints.removeAllElements();
                fPoints.addElement(start);
                fPoints.addElement(end);
            }
            updateConnection();
            changed();
        } else {
            super.setAttribute(name, value);
        }
    }

    public void write(StorableOutput dw) {
        super.write(dw);
        dw.writeBoolean(isDisjunctive);
    }

    public void read(StorableInput dr) throws IOException {
        isDisjunctive = false; // prevent updatePoints()
        setFillColor(Color.white);
        super.read(dr);
        isDisjunctive = dr.readBoolean();
    }

    public void updateConnection() {
        super.updateConnection();
        if (isDisjunctive) {
            updatePoints();
        }
    }

    public void layoutConnection() {
        if (!isDisjunctive) {
            super.layoutConnection();
        }
    }

    /**
     * Gets the handles of the figure.
     */
    public Vector<Handle> handles() {
        if (isDisjunctive) {
            Vector<Handle> handles = new Vector<Handle>(fPoints.size() * 2);
            handles.addElement(new ChangeConnectionStartHandle(this));
            for (int i = 1; i < fPoints.size() - 1; i++) {
                handles.addElement(new NullHandle(this, locator(i)));
            }
            handles.addElement(new ChangeConnectionEndHandle(this));
            for (int i = 0; i < fPoints.size() - 1; i++) {
                handles.addElement(new ElbowHandle(this, i));
            }
            return handles;
        } else {
            return super.handles();
        }
    }

    public Locator connectedTextLocator(Figure f) {
        if (isDisjunctive) {
            return new ElbowTextLocator();
        } else {
            return super.connectedTextLocator(f);
        }
    }

    protected void updatePoints() {
        willChange();
        Point start = startPoint();
        Point end = endPoint();
        fPoints.removeAllElements();
        fPoints.addElement(start);

        if (start.x != end.x && start.y != end.y) {
            fPoints.addElement(new Point(start.x, (start.y + end.y) / 2));
            fPoints.addElement(new Point(end.x, (start.y + end.y) / 2));
        }
        fPoints.addElement(end);
        changed();
    }

    /**
     * Handles the connection of a connection.
     * Override this method to handle this event.
     */
    protected void handleConnect(Figure start, Figure end) {
        if (end instanceof ConceptFigure // should be!
                    && "interface".equals(((ConceptFigure) end).getStereotype())) {
            setAttribute("LineStyle", PolyLineFigure.LINE_STYLE_DASHED);
        }
    }
}