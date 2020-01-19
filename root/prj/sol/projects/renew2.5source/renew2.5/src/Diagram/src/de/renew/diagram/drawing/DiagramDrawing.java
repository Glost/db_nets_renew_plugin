/*
 * Created on Apr 14, 2003
 */
package de.renew.diagram.drawing;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FilterContainer;

import CH.ifa.draw.io.SimpleFileFilter;

import CH.ifa.draw.standard.StandardDrawing;

import de.renew.diagram.AIPFileFilter;
import de.renew.diagram.LifeLineConnection;
import de.renew.diagram.Locator;
import de.renew.diagram.MessageConnection;
import de.renew.diagram.RoleDescriptorFigure;
import de.renew.diagram.TailFigure;
import de.renew.diagram.TaskFigure;

import de.renew.gui.GuiPlugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;


/**
 * @author Lawrence Cabac
 */
public class DiagramDrawing extends StandardDrawing {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DiagramDrawing.class);
    private static FilterContainer filterContainer;
    public boolean generatePeerInstantaneously = false;
    private Locator loc;

    public DiagramDrawing() {
        super();
        loc = new Locator();


    }

    //	public void init() {
    //		logger.debug("Figures are " + fFigures);
    //		Iterator it = fFigures.iterator();
    //		while (it.hasNext()) {
    //			Figure figure = (Figure) it.next();
    //
    //		}
    //
    //	}
    public Figure add(Figure figure) {
        if (figure instanceof TailFigure) {
            loc.add(figure);
        }
        return super.add(figure);
    }

    //    public Figure add(Figure figure) {
    //        if (figure instanceof IDiagramElement) {
    //            if (figure instanceof TaskFigure) {
    //                Figure dParent = loc.getElementAtPosition(figure.displayBox().x); //+ figure.displayBox().width / 2);
    //                if (dParent != null) {
    //                    TaskFigure fig = (TaskFigure) super.add(figure);
    //
    //
    //                    loc.add(fig);
    //                    RoleDescriptorFigure head = ((TailFigure) dParent).getDHead();
    //                    head.addToTail(fig);
    //                    fig.setDHead(head);
    //                    TailFigure t = (TailFigure) dParent;
    //                    fig.addDParent(t);
    //                    t.addDChild(fig);
    // 
    //                    if (dParent instanceof RoleDescriptorFigure) {
    //                        fig.addPeerName("start");
    //                    }
    //
    //                    if (fig == null) {
    //                        GuiPlugin.getCurrent().showStatus("Draw Task Figures under other Figures!");
    //
    //                    }
    //
    //                    //logger.debug("DiagramDrawing second "+ generatePeerInstantaneously);
    //                    if (generatePeerInstantaneously) {
    //                        ((TailFigure) figure).executeUpdate();
    //                    }
    //                    return fig;
    //                }
    //            } else if (figure instanceof VSplitFigure) {
    //                Figure dParent = loc.getElementAtPosition(figure.displayBox().x
    //                                                          + figure.displayBox().width / 2);
    //                if (dParent != null) {
    //                    VSplitFigure fig = (VSplitFigure) super.add(figure);
    //
    //                    loc.add(fig);
    //                    RoleDescriptorFigure head = ((TailFigure) dParent).getDHead();
    //                    head.addToTail(fig);
    //                    fig.setDHead(head);
    //                    TailFigure t = (TailFigure) dParent;
    //                    fig.addDParent(t);
    //                    t.addDChild(fig);
    //                    //logger.debug("DiagramDrawing third "+ generatePeerInstantaneously);
    //                    if (generatePeerInstantaneously) {
    //                        ((TailFigure) figure).executeUpdate();
    //
    //                    }
    //
    //                    return fig;
    //                }
    //            } else if (figure instanceof RoleDescriptorFigure) {
    //                Figure fig = super.add(figure);
    //                loc.add(fig);
    //                if (generatePeerInstantaneously) {
    //                    ((TailFigure) figure).executeUpdate();
    //
    //                }
    //
    //                return fig;
    //
    //            } else if (figure instanceof MessageConnection) {
    //                Figure fig = super.add(figure);
    //
    //
    //                return fig;
    //
    //            } else if (figure instanceof LifeLineConnection) {
    //                Figure fig = super.add(figure);
    //
    //
    //                return fig;
    //            } else if (figure instanceof DiagramTextFigure) {
    //                Figure fig = super.add(figure);
    //
    //
    //                return fig;
    //
    //            } else if (figure instanceof HSplitFigure) {
    //                Figure fig = super.add(figure);
    //
    //
    //                return fig;
    //
    //
    //            }
    //        } else {
    //            GuiPlugin.getCurrent().showStatus("Draw Diagram Figures in DiagramDrawing!");
    //        }
    //        return null;
    //    }

    /* (non-Javadoc)
    * @see CH.ifa.draw.framework.Drawing#getWindowCategory()
    */
    public String getWindowCategory() {
        return "Diagrams";
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.standard.CompositeFigure#remove(CH.ifa.draw.framework.Figure)
     */
    public Figure remove(Figure figure) {
        Figure removed = super.remove(figure);
        if (removed != null) {
            logger.debug("DiagramDrawing: Figure removed from Locator "
                         + removed);
            logger.debug("Locator " + loc);
            loc.remove(removed);
            loc.show();

            logger.debug("Locator " + loc);
        }
        if (removed instanceof RoleDescriptorFigure) {
            Iterator<Figure> it = ((RoleDescriptorFigure) removed).tail();
            while (it.hasNext()) {
                remove(it.next());
            }
        }

        return removed;
    }

    public Locator getLocator() {
        return loc;
    }

    //------------------------------------------------------------------------------   
    static public FilterContainer getFilterContainer() {
        if (filterContainer == null) {
            return new FilterContainer(new AIPFileFilter());
        } else {
            return filterContainer;
        }
    }

    public SimpleFileFilter getDefaultFileFilter() {
        return getFilterContainer().getDefaultFileFilter();
    }

    public HashSet<SimpleFileFilter> getImportFileFilters() {
        return getFilterContainer().getImportFileFilters();
    }

    public HashSet<SimpleFileFilter> getExportFileFilters() {
        return getFilterContainer().getExportFileFilters();
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.framework.Drawing#getDefaultExtension()
     */
    public String getDefaultExtension() {
        return getDefaultFileFilter().getExtension();
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.standard.CompositeFigure#removeAll(java.util.Vector)
     */
    public void removeAll(Vector<?extends Figure> figures) {
        Iterator<?extends Figure> it = figures.iterator();
        while (it.hasNext()) {
            Figure fig = it.next();
            if (fig instanceof TailFigure) {
                loc.remove(fig);
                TailFigure tailfigure = (TailFigure) fig;
                RoleDescriptorFigure rdf = tailfigure.getDHead();
                if (rdf != null) {
                    rdf.removeFromTail(fig);
                }
            }
        }
        super.removeAll(figures);
    }

    public void generatePeers() {
        try {
            if (!generatePeerInstantaneously) { //%%% change this to recurse all childs after RDF
                GuiPlugin.getCurrent().showStatus("Flushing peers...");
                Iterator<Figure> it = fFigures.iterator();
                while (it.hasNext()) {
                    Figure fig = it.next();
                    if (fig instanceof TailFigure) {
                        TailFigure tailFigure = (TailFigure) fig;
                        tailFigure.flushPeers();
                        tailFigure.flushRelatives();
                        tailFigure.setDHead(null);
                        tailFigure.flushConnections();
                        if (tailFigure instanceof TaskFigure) {
                            ((TaskFigure) tailFigure).flushMessages();
                        }
                        if (tailFigure instanceof RoleDescriptorFigure) {
                            ((RoleDescriptorFigure) tailFigure).flushTail();
                        }
                    }
                }

                // 1. arcs notify the nodes
                //    a) LifeLine as connections in all TailFigure
                GuiPlugin.getCurrent().showStatus("Parsing figures...");
                it = fFigures.iterator();
                while (it.hasNext()) {
                    Figure fig = it.next();
                    if (fig instanceof LifeLineConnection) {
                        LifeLineConnection connection = (LifeLineConnection) fig;
                        connection.notifyConnectorOwners();
                    }
                }

                // 1. arcs notify their nodes
                //    b) Messages as messages in TaskFigures
                GuiPlugin.getCurrent()
                         .showStatus("Parsing figures second stage...");
                it = fFigures.iterator();
                while (it.hasNext()) {
                    Figure fig = it.next();
                    if (fig instanceof MessageConnection) {
                        MessageConnection connection = (MessageConnection) fig;
                        connection.notifyConnectorOwners();
                    }
                }

                //2. starting at RDF parse until end.
                GuiPlugin.getCurrent()
                         .showStatus("Parsing figures third stage...");

                it = fFigures.iterator();
                while (it.hasNext()) {
                    Figure fig = it.next();
                    if (fig instanceof RoleDescriptorFigure) {
                        logger.debug("=================> reached Diagramdrawing generatePeers. line 234");
                        ((TailFigure) fig).findAndNotifyDChildren();


                    }
                }

                //updateAllPeersNames();
                GuiPlugin.getCurrent().showStatus("Updating peer names...");
                it = fFigures.iterator();
                while (it.hasNext()) {
                    Figure fig = it.next();
                    if (fig instanceof TailFigure) {
                        if (!(fig instanceof TaskFigure)) {
                            ((TailFigure) fig).updatePeerNames();
                        }
                    }
                }

                //updateAllPeers();
                GuiPlugin.getCurrent().showStatus("Updating peers...");
                it = fFigures.iterator();
                while (it.hasNext()) {
                    Figure fig = it.next();
                    if (fig instanceof TailFigure) {
                        ((TailFigure) fig).updatePeers();
                    }
                }
            }

            // end of if generateInstant
            GuiPlugin.getCurrent().showStatus("Generating peers...");
            Iterator<Figure> it = fFigures.iterator();
            while (it.hasNext()) {
                Figure fig = it.next();
                if (fig instanceof RoleDescriptorFigure) {
                    ((RoleDescriptorFigure) fig)
                        .createNewDrawingAndGeneratePeers();

                }
            }
            GuiPlugin.getCurrent().showStatus("Done generating peers.");

        } catch (Exception e) {
            GuiPlugin.getCurrent()
                     .showStatus("Sorry, something went wrong during the generation of net componenets.");
            logger.error(e.getMessage(), e);
        }
    }

    //    /**
    //     * Find and return the connection for two given Figures, if exist.
    //     * 
    //	 * @return The ConnectionFigure that connects the two figures,
    //     *         null, if the two figures are not connected to each other.
    //	 */
    //	public ConnectionFigure findConnection(Figure start, Figure end){
    //        Iterator it = fFigures.iterator();
    //        while (it.hasNext()){
    //            Figure figure = (Figure) it.next();
    //            if (figure instanceof LineConnection){
    //                LineConnection line = (LineConnection)figure;    
    //                if (line.startFigure().equals(start) && line.endFigure().equals(end)){
    //                    return line;
    //                }
    //            }
    //        }
    //        return null;        
    //    }
}