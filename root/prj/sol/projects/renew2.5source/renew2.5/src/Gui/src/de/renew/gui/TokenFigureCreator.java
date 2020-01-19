package de.renew.gui;

import CH.ifa.draw.figures.ImageFigure;

import CH.ifa.draw.framework.Figure;

import de.renew.remote.ObjectAccessor;
import de.renew.remote.ObjectAccessorImpl;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;


/**
 * Delegates the figure representation for {@link Token} implementations.
 *
 * @author Frank Wienberg
 * @author Joern Schumacher
 * @author Michael Duvigneau
 **/
class TokenFigureCreator implements FigureCreator {
    public Figure getTokenFigure(ObjectAccessor tokenAccessor, boolean expanded)
            throws RemoteException {
        Object token = ((ObjectAccessorImpl) tokenAccessor).getObject();
        token = ((Token) token).getTokenRepresentation(expanded);


        // URLs are only handled specially in the context
        // of Token tokens.
        if (token instanceof URL) {
            try {
                token = ((URL) token).getContent();
            } catch (IOException e) {
                token = "UNLOADABLE IMAGE";
            }
        }


        // Images are only handled specially in the context
        // of Token tokens.
        if (token instanceof ImageProducer) {
            token = Toolkit.getDefaultToolkit()
                           .createImage((ImageProducer) token);
        }
        if (token instanceof Image) {
            Image image = (Image) token;
            ImageFigure figure = new ImageFigure(image, null, new Point());

            figure.displayBox(new Point(),
                              new Point(image.getWidth(null),
                                        image.getHeight(null)));
            return figure;
        }

        // If a figure is returned
        if (token instanceof Figure) {
            return (Figure) token;
        }

        // No image. Fall back to the ordinary visualization.
        return null;
    }

    public boolean canCreateFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        return token instanceof ObjectAccessorImpl
               && token.isInstanceOf(Token.class);
    }
}