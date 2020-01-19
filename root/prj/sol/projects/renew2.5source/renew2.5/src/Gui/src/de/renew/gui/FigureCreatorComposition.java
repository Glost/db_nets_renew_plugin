package de.renew.gui;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Figure;

import de.renew.remote.ObjectAccessor;

import de.renew.util.TextToken;

import java.rmi.RemoteException;

import java.util.Iterator;
import java.util.Vector;


/**
 * @author joern
 *
 * This is a singleton class that is used to get gui representation
 * for tokens.
 * FigureCreators are registered by the class they can represent
 * and will be called if a token of such class occurs.
 * If a FigureCreator returns null as a result, a TextFigure
 * with content a text version of the token will be created.
 */
public class FigureCreatorComposition implements FigureCreator,
                                                 TextFigureCreator,
                                                 FigureCreatorHolder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FigureCreatorComposition.class);
    private Vector<FigureCreator> _figureCreators = new Vector<FigureCreator>();
    private Vector<TextFigureCreator> _textFigureCreators = new Vector<TextFigureCreator>();

    public void registerCreator(FigureCreator creator) {
        _figureCreators.add(creator);
    }

    public void registerCreator(TextFigureCreator creator) {
        _textFigureCreators.add(creator);
    }

    public void unregisterCreator(FigureCreator creator) {
        _figureCreators.remove(creator);
    }

    public void unregisterCreator(TextFigureCreator creator) {
        _figureCreators.remove(creator);
    }

    public boolean canCreateFigure(ObjectAccessor o, boolean expanded)
            throws RemoteException {
        Iterator<FigureCreator> it = _figureCreators.iterator();
        while (it.hasNext()) {
            FigureCreator fc = it.next();
            if (fc.canCreateFigure(o, expanded)) {
                return true;
            }
        }
        return false;
    }

    public boolean canCreateFigure(InscribableFigure f) {
        Iterator<FigureCreator> it = _figureCreators.iterator();
        while (it.hasNext()) {
            TextFigureCreator fc = (TextFigureCreator) it.next();
            if (fc.canCreateFigure(f)) {
                return true;
            }
        }
        return false;
    }

    public boolean canCreateDefaultInscription(InscribableFigure f) {
        Iterator<FigureCreator> it = _figureCreators.iterator();
        while (it.hasNext()) {
            TextFigureCreator fc = (TextFigureCreator) it.next();
            if (fc.canCreateDefaultInscription(f)) {
                return true;
            }
        }
        return false;
    }

    public TextFigure createTextFigure(InscribableFigure figure) {
        Iterator<TextFigureCreator> registeredClasses = _textFigureCreators
                                                            .iterator();
        TextFigure result = null;
        while (registeredClasses.hasNext()) {
            TextFigureCreator creator = registeredClasses.next();
            if (creator.canCreateFigure(figure)) {
                logger.debug("FigureCreation: " + creator
                             + " creates a text figure for " + figure);
                result = creator.createTextFigure(figure);
                break;
            }
        }
        if (result == null) {
            logger.debug("FigureCreation: creating default text figure for "
                         + figure);
            return new CPNTextFigure();
        }
        return result;
    }

    public String getDefaultInscription(InscribableFigure figure) {
        Iterator<TextFigureCreator> registeredClasses = _textFigureCreators
                                                            .iterator();
        String result = null;
        while (registeredClasses.hasNext()) {
            TextFigureCreator creator = registeredClasses.next();
            if (creator.canCreateDefaultInscription(figure)) {
                logger.debug("FigureCreation: " + creator
                             + " creates an inscription for " + figure);
                result = creator.getDefaultInscription(figure);
                break;
            }
        }
        if (result == null) {
            logger.debug("FigureCreation: creating default inscription for "
                         + figure);
            if (figure instanceof ArcConnection) {
                return "x";
            }
            if (figure instanceof PlaceFigure) {
                return "[]";
            }
            if (figure instanceof TransitionFigure) {
                return ":s()";
            }
            return "";
        }
        return result;
    }

    public Figure getTokenFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        Iterator<FigureCreator> registeredClasses = _figureCreators.iterator();
        Figure result = null;
        while (registeredClasses.hasNext()) {
            FigureCreator creator = registeredClasses.next();
            if (creator.canCreateFigure(token, expanded)) {
                logger.debug(creator
                             + " creates a representation for this token.");
                result = creator.getTokenFigure(token, expanded);
                break;
            }
        }
        if (result == null) {
            logger.debug("creating default representation for this token.");
            return new TextFigure(objectToString(token), true);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("got token figure for token " + token
                         + " --> result figure " + result);
        }
        return result;
    }

    public static String objectToString(ObjectAccessor token)
            throws RemoteException {
        StringBuffer output = new StringBuffer();

        if (token == null) {
            output.append("null");
        } else if (token.isInstanceOf(String.class)) {
            output.append('"').append(token.asString()).append('"');
        } else if (token.isInstanceOf(TextToken.class)) {
            output.append(token.asTextToken().toTokenText());
        } else {
            output.append(token.asString());
        }
        return output.toString();
    }
}