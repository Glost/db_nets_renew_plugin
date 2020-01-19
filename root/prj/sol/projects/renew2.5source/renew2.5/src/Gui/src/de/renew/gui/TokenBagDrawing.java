package de.renew.gui;

import CH.ifa.draw.standard.StandardDrawing;

import de.renew.remote.PlaceInstanceAccessor;

import java.awt.Dimension;

import java.rmi.RemoteException;

import java.util.Hashtable;


public class TokenBagDrawing extends StandardDrawing {
    private static Hashtable<PlaceInstanceAccessor, TokenBagDrawing> drawingsByInstance = new Hashtable<PlaceInstanceAccessor, TokenBagDrawing>();

    /**
     * The place instance whose contents should be displayed
     * in this drawing.
     * @serial
     **/
    private PlaceInstanceAccessor placeInstance = null;

    /**
     * The figure which is used to display the contents of
     * the place instance.
     * @serial
     **/
    private TokenBagFigure tokenBag;

    private TokenBagDrawing(PlaceInstanceAccessor pi) {
        this.placeInstance = pi;
        drawingsByInstance.put(pi, this);

        String name;
        try {
            name = placeInstance.asString();
        } catch (RemoteException e) {
            // Show the user the problem
            name = e.toString();
        }
        setName(name);

        tokenBag = new TokenBagFigure(this, null, pi,
                                      PlaceFigure.EXPANDED_TOKENS);
        add(tokenBag);
    }

    static TokenBagDrawing getTokenBagDrawing(PlaceInstanceAccessor pi) {
        // forbid concurrent searches for instance drawings.
        synchronized (drawingsByInstance) {
            // find instance drawing:
            if (drawingsByInstance.containsKey(pi)) {
                return drawingsByInstance.get(pi);
            } else {
                return new TokenBagDrawing(pi);
            }
        }
    }

    /*
    private Figure nullIsTokenBag(Figure f) {
      if (f==null) return tokenBag; else return f;
    }

    public Figure findFigure(int x, int y) {
      return nullIsTokenBag(tokenBag.findFigure(x,y));
    }

    public Figure findFigure(Rectangle r) {
      return nullIsTokenBag(tokenBag.findFigure(r));
    }

    public Figure findFigureWithout(int x, int y, Figure without) {
      return nullIsTokenBag(tokenBag.findFigureWithout(x,y,without));
    }

    public Figure findFigure(Rectangle r, Figure without) {
      return nullIsTokenBag(tokenBag.findFigure(r,without));
    }

    public Figure findFigureInside(int x, int y) {
      return nullIsTokenBag(tokenBag.findFigureInside(x,y));
    }

    public Figure findFigureInsideWithout(int x, int y, Figure without) {
      return nullIsTokenBag(tokenBag.findFigureInsideWithout(x,y,without));
    }
    */
    public void release() {
        if (placeInstance != null) {
            drawingsByInstance.remove(placeInstance);
            placeInstance = null;
            tokenBag = null;
        }
        super.release();
    }

    public void setAttribute(String name, Object value) {
        tokenBag.setAttribute(name, value);
    }

    /**
     * Returns whether drawing has been modified since last save.
     */
    public boolean isModified() {
        return false; // a TokenBagDrawing should not be saved!
    }

    public Dimension defaultSize() {
        return new Dimension(100, 50);
    }

    public String getWindowCategory() {
        return "Token bags";
    }
}