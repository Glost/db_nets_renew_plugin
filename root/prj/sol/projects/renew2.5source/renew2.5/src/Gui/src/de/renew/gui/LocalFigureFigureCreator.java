/**
 *
 */
package de.renew.gui;

import CH.ifa.draw.figures.GroupFigure;
import CH.ifa.draw.figures.LineConnection;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.CompositeFigure;

import de.renew.remote.ObjectAccessor;
import de.renew.remote.ObjectAccessorImpl;

import java.rmi.RemoteException;

import java.util.Enumeration;
import java.util.HashMap;


/**
 * @author duvigneau, cabac
 *
 */
public class LocalFigureFigureCreator implements FigureCreator {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LocalFigureFigureCreator.class);

    /* (non-Javadoc)
     * @see de.renew.gui.FigureCreator#canCreateFigure(de.renew.remote.ObjectAccessor, boolean)
     */
    public boolean canCreateFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        if (token instanceof ObjectAccessorImpl) {
            ObjectAccessorImpl oaimpl = (ObjectAccessorImpl) token;
            Object o = oaimpl.getObject();
            if (o instanceof Figure) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see de.renew.gui.FigureCreator#getTokenFigure(de.renew.remote.ObjectAccessor, boolean)
     */
    public Figure getTokenFigure(ObjectAccessor token, boolean expanded)
            throws RemoteException {
        if (token instanceof ObjectAccessorImpl) {
            ObjectAccessorImpl oaimpl = (ObjectAccessorImpl) token;
            Object o = oaimpl.getObject();
            if (o instanceof CompositeFigure) {
                CompositeFigure composite = (CompositeFigure) o;
                Enumeration<Figure> figures = composite.figures();
                GroupFigure group = new GroupFigure();
                HashMap<Figure, Figure> map = new HashMap<Figure, Figure>();
                while (figures.hasMoreElements()) {
                    Figure figure = (Figure) figures.nextElement();
                    if (figure instanceof LineConnection) {
                        continue;
                    }
                    Figure clone = (Figure) figure.clone();
                    map.put(figure, clone);
                    group.add(clone);
                }
                figures = composite.figures();
                while (figures.hasMoreElements()) {
                    Figure figure = (Figure) figures.nextElement();
                    if (figure instanceof LineConnection) {
                        try {
                            LineConnection arc = (LineConnection) figure;
                            LineConnection arcclone = (LineConnection) arc.clone();
                            Figure startclone = map.get(arc.startFigure());
                            Figure endclone = map.get(arc.endFigure());
                            //set start and end for clone                             
                            arcclone.connectStart(startclone.connectorAt(startclone
                                                                         .center()));
                            arcclone.connectEnd(endclone.connectorAt(endclone
                                .center()));
                            group.add(arcclone);
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            if (logger.isDebugEnabled()) {
                                logger.debug(LocalFigureFigureCreator.class
                                    .getSimpleName() + ": ", e);
                            }
                        }
                    }
                }
                return group;
            }
            if (o instanceof Figure) {
                Figure fig = (Figure) o;
                return fig;
            }
        }
        return null;
    }
}