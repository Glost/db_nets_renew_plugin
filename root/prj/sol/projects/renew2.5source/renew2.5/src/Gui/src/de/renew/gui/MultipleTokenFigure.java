package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;

import de.renew.net.NetInstance;

import de.renew.plugin.PropertyHelper;

import de.renew.remote.ObjectAccessor;

import de.renew.util.TextToken;

import java.awt.Color;
import java.awt.Graphics;

import java.rmi.RemoteException;


public class MultipleTokenFigure extends HorizontalCompositeFigure {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(MultipleTokenFigure.class);

    /**
     * Name of the boolean property that controls whether
     * tokens are displayed with a white background (<code>false</code>) or without
     * background (<code>true</code>).
     * The name is: {@value}.
     */
    public static final String NOTOKENBACKGROUND_PROP_NAME = "de.renew.gui.noTokenBackground";

    public MultipleTokenFigure(int mult, boolean isTested, double time,
                               ObjectAccessor token, boolean expanded) {
        String multStr = getMultString(mult, isTested);
        if (multStr.length() > 0) {
            add(new TextFigure(multStr, true));
        }

        try {
            FigureCreator fc = GuiPlugin.getCurrent().getFigureCreator();

            Figure tokenFig = fc.getTokenFigure(token, expanded);
            add(tokenFig);

            if (token != null && token.isInstanceOf(NetInstance.class)) {
                addClickHandle(new NetInstanceHandle(tokenFig, null,
                                                     token.asNetInstance()));
            }
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        }

        if (time != 0) {
            add(new TextFigure("@" + time, true));
        }

        layout();
    }

    // this method is taken from the JavaMode
    public static String objectToString(Object token) {
        StringBuffer output = new StringBuffer();

        if (token instanceof String) {
            output.append('"').append((String) token).append('"');
        } else if (token instanceof TextToken) {
            output.append(((TextToken) token).toTokenText());
        } else {
            output.append(token);
        }
        return output.toString();
    }

    //   final Object token;
    //   private boolean expanded=true;
    public static String getMultString(int mult, boolean isTested) {
        StringBuffer output = new StringBuffer();

        if (isTested) {
            if (mult > 0) {
                output.append(String.valueOf(mult));
            }
            output.append("(1)");
        } else if (mult > 1) {
            output.append(String.valueOf(mult));
        }
        if (isTested || mult > 1) {
            output.append("'");
        }

        return output.toString();
    }

    /**
     * Finds a top level Figure. Use this call for hit detection that
     * should not descend into the figure's children.
     */
    public Figure findFigure(int x, int y) {
        return null; // don't show your inside!
    }

    public void draw(Graphics g) {
        if (PropertyHelper.getBoolProperty(GuiPlugin.getCurrent().getProperties(),
                                                   NOTOKENBACKGROUND_PROP_NAME)) {
            // do not draw an opaque background rectangle
        } else {
            g.setColor(Color.WHITE);
            g.drawRect(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                       fDisplayBox.height);
            g.fillRect(fDisplayBox.x, fDisplayBox.y, fDisplayBox.width,
                       fDisplayBox.height);
        }
        super.draw(g);
    }

    /*
       public boolean inspect(DrawingView view, boolean alternate) {
         // undo support can be implemented here
         // (currently disabled for instance drawings)
         if (alternate) {
           toggleExpanded();
           return true;
         } else {
            return super.inspect(view,alternate);
         }

       }

       public void toggleExpanded() {
          expanded=!expanded;
          layout();
       }
    */
}