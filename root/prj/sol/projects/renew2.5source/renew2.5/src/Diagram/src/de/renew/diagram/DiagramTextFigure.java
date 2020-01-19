/*
 * Created on Apr 16, 2003
 */
package de.renew.diagram;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;

import CH.ifa.draw.standard.LocatorHandle;
import CH.ifa.draw.standard.RelativeLocator;

import de.renew.gui.TokenHandle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import java.util.Vector;


/**
 * A DiagramTextFigure is a TextFigure that can be used in a DiagramDrawing.
 *
 * @author Lawrence Cabac
 */


//TODO: declare abstract
public class DiagramTextFigure extends TextFigure implements IDiagramElement {
    public static final String HIDDEN_TEXT = "HiddenText";
    private static final String ALTERNATE_TEXT = "AlternateText";
    public static final String IS_HIDDEN = "IsHidden";
    private static final String DUMMY = "~";

    //private boolean isClosed = false;
    // private String alternateText = "~";
    /**
     * @return the alternateText
     */
    public String getAlternateText() {
        return (String) getAttribute(ALTERNATE_TEXT);
    }


    /**
     * @param alternateText the alternateText to set
     */
    public void setAlternateText(String alternateText) {
        setAttribute(ALTERNATE_TEXT, alternateText);
    }

    /**
     *
     */
    public void toggleText() {
        if (getAttribute(IS_HIDDEN) == null) {
            setAttribute(IS_HIDDEN, false);
        }
        setAttribute(IS_HIDDEN, !(Boolean) getAttribute(IS_HIDDEN));
        if ((Boolean) getAttribute(IS_HIDDEN)) {
            setAttribute(HIDDEN_TEXT, getText());
            setText((String) getAttribute(ALTERNATE_TEXT));
            setAttribute(IS_HIDDEN, true);
        } else {
            setText((String) getAttribute(HIDDEN_TEXT));
            setAttribute(IS_HIDDEN, false);
        }
    }

    /**
     *
     */
    public void hideText() {
        if (getAttribute(IS_HIDDEN) == null) {
            setAttribute(IS_HIDDEN, false);
        }
        if (!(Boolean) getAttribute(IS_HIDDEN)) {
            setAttribute(HIDDEN_TEXT, getText());
            setText((String) getAttribute(ALTERNATE_TEXT));
            setAttribute(IS_HIDDEN, true);
        }
    }

    /**
     *
     */
    public void unhideText() {
        if (getAttribute(IS_HIDDEN) == null) {
            setAttribute(IS_HIDDEN, false);
            return;
        }
        if ((Boolean) getAttribute(IS_HIDDEN)) {
            setText((String) getAttribute(HIDDEN_TEXT));
            setAttribute(IS_HIDDEN, false);
        }
    }

    public String getRealText() {
        Boolean attribute = (Boolean) getAttribute(IS_HIDDEN);
        if (attribute != null && attribute) {
            return (String) getAttribute(HIDDEN_TEXT);
        } else {
            return super.getText();
        }
    }


    /* (non-Javadoc)
     * @see CH.ifa.draw.figures.TextFigure#drawFrame(java.awt.Graphics)
     */
    @Override
    public void drawFrame(Graphics g) {
        // TODO Auto-generated method stub
        super.drawFrame(g);
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.figures.TextFigure#handles()
     */
    public Vector<Handle> handles() {
        Vector<Handle> v = super.handles();

        //NOTICEsignature
        LocatorHandle handle = new DiagramTextHandle(this, Color.red,
                                                     Color.blue,
                                                     new RelativeLocator(0.1,
                                                                         0.0));
        //handle.draw(g);
        v.add(handle);
        return v;
    }

    public DiagramTextFigure() {
        super();
        setAlternateText(DUMMY);
    }

    public DiagramTextFigure(boolean canBeConnected) {
        super(canBeConnected);
        setAlternateText(DUMMY);
    }

    public DiagramTextFigure(String text) {
        super(text);
        setAlternateText(DUMMY);
    }

    public DiagramTextFigure(String text, boolean isReadOnly) {
        super(text, isReadOnly);
        setAlternateText(DUMMY);
    }

    public boolean inspect(DrawingView view, boolean alternate) {
        if (!alternate) {
            return super.inspect(view, false);
        } else {
            if (acceptsTyping()) {
                Object attr = getAttribute(IS_HIDDEN);
                if (attr != null && (Boolean) attr) {
                    setText((String) getAttribute(HIDDEN_TEXT));
                    setAttribute(IS_HIDDEN, false);
                }
                ((DrawApplication) view.editor()).doTextEdit(this);
                return true;
            }
            return false;
        }
    }

    private final class DiagramTextHandle extends LocatorHandle
            implements TokenHandle {
        //NOTICEsignature
        private DiagramTextHandle(Figure owner, Color fillColor,
                                  Color penColor,
                                  CH.ifa.draw.framework.Locator l) {
            super(owner, l);
        }

        public void invokeStart(MouseEvent e, int x, int y, DrawingView view) {
//            System.out.println("Clicked");
            super.invokeStart(e, x, y, view);
            if (e.getClickCount() == 1) {
                toggleText();
            }
            // Despite the fact that the appearance of the FSFigure changes
            // when nodes are opened or closed, the semantics of the figure do
            // not change. More important, the FSFigure does currently not
            // serialize information about open nodes in its undo snapshot.
            // So we inform the AbstractTool that nothing happened.
            noChangesMade();
            /* (non-Javadoc)
             * @see CH.ifa.draw.standard.AbstractHandle#draw(java.awt.Graphics)
             */
        }

        @Override
        public void draw(Graphics g) {
            // super.draw(g);
            Rectangle r = displayBox();
            g.setColor(Color.gray);
            g.fillRect(r.x, r.y, r.width, r.height);

            g.setColor(Color.red);
            g.drawRect(r.x, r.y, r.width, r.height);
        }
    }
}