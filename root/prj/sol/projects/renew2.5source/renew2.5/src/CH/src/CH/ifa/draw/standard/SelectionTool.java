/*
 * @(#)SelectionTool.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.Handle;
import CH.ifa.draw.framework.PartialSelectableFigure;
import CH.ifa.draw.framework.Tool;

import de.renew.util.StringUtil;

import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Tool to select and manipulate figures.
 * A selection tool is in one of three states, e.g., background
 * selection, figure selection, handle manipulation. The different
 * states are handled by different child tools.
 * <hr>
 * <b>Design Patterns</b><P>
 * <img src="images/red-ball-small.gif" width=6 height=6 alt=" o ">
 * <b><a href=../pattlets/sld032.htm>State</a></b><br>
 * SelectionTool is the StateContext and child is the State.
 * The SelectionTool delegates state specific
 * behavior to its current child tool.
 * <hr>
 */
public class SelectionTool extends AbstractTool {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SelectionTool.class);
    private Tool fChild = null;
    private Tool fLastChild = null;
    private DrawingView fFreezedView = null;

    public SelectionTool(DrawingEditor editor) {
        super(editor);
    }

    /**
     * Handles mouse down events and starts the corresponding tracker.
     *
     * Caution: This method freezes the view until a
     * <code>mouseReleased</code> event is received or the tool is
     * <code>deactivate</code>d.
     */
    public void mouseDown(MouseEvent e, int x, int y) {
        // on Windows NT: AWT generates additional mouse down events
        // when the left button is down && right button is clicked.
        // To avoid dead locks we ignore such events
        if (fChild != null) {
            return;
        }


        // Since Java 1.4.2, the AWT event thread dies after an Error or
        // RuntimeExeption. Because the mouseUp event then belongs to a
        // different thread, it cannot unfreeze the view. So, we have to
        // catch any abnormal situation and unfreeze the view immediately.
        try {
            boolean rightclick = (e.getModifiers()
                                 & (InputEvent.BUTTON2_MASK
                                   | InputEvent.BUTTON3_MASK)) != 0;
            if (fLastChild != null && e.getClickCount() == 2 && !rightclick) {
                // If left-double-click: use previous fChild!
                fChild = fLastChild;
                fLastChild = null;
            } else {
                if (fFreezedView == null) {
                    fFreezedView = view();
                    fFreezedView.freezeView();
                }

                Handle handle = null;
                if (!rightclick && view().selectionCount() == 1) {
                    handle = view().findHandle(e.getX(), e.getY());
                }
                if (handle != null) {
                    fChild = createHandleTracker(fEditor, handle);
                    fLastChild = fChild; // store for double-clicks!
                } else {
                    fLastChild = null;
                    Figure figure = drawing().findFigure(e.getX(), e.getY());

                    // also interprete clicks on a figure's handle as clicks
                    // on the figure:
                    if (figure == null) {
                        handle = view().findHandle(e.getX(), e.getY());
                        if (handle != null) {
                            figure = handle.owner();
                        }
                    }
                    boolean selectableFigureFound = false;
                    if (figure != null) {
                        selectableFigureFound = true;
                        if (figure instanceof PartialSelectableFigure) {
                            if (!view().selection().contains(figure)) {
                                selectableFigureFound = false;
                                PartialSelectableFigure partialSelectableFigure = (PartialSelectableFigure) figure;
                                if (partialSelectableFigure.isModifierSelectable()
                                            && e.isAltDown()) {
                                    selectableFigureFound = true;
                                } else if (partialSelectableFigure
                                                       .isSelectableInRegion(e
                                                       .getX(), e.getY())) {
                                    selectableFigureFound = true;
                                }
                            }
                        }
                    }

                    if (selectableFigureFound) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Click-on-figure: " + figure);
                        }
                        if (e.isControlDown()) {
                            if (rightclick) {
                                // FIXME: handle ctrl / right click with editing window for ttt
                            } else {
                                openTargetLocation(figure);
                            }
                        } else {
                            if (e.getClickCount() >= 2 && (!rightclick)) {
                                figure.inspect(view(), false);
                                return;
                            }
                            if (rightclick) {
                                if (alternateInspectFigure(figure)) {
                                    return;
                                }
                            }
                            fChild = createDragTracker(fEditor, figure);
                        }
                    } else { // figure == null
                        if (!e.isShiftDown()) {
                            view().clearSelection();
                        }
                        fChild = createAreaTracker(fEditor);
                    }
                }
            }
            if (fChild != null) { //can be null if a control left click is performed
                fChild.mouseDown(e, x, y);
            }
        } catch (RuntimeException ex) {
            if (fFreezedView != null) {
                fFreezedView.unfreezeView();
                fFreezedView = null;
            }
            throw ex;
        } catch (Error ex) {
            if (fFreezedView != null) {
                fFreezedView.unfreezeView();
                fFreezedView = null;
            }
            throw ex;
        }
    }

    /**
     * @param figure
     */
    private void openTargetLocation(Figure figure) {
        //try to open targetLocation                            
        Object target = figure.getAttribute("targetLocation");
        if (target != null) {
            try {
                URI targetURI = new URI((String) target);
                try {
                    String scheme = targetURI.getScheme();
                    if (logger.isDebugEnabled()) {
                        logger.debug(SelectionTool.class.getSimpleName() + ": "
                                     + "Trying to open: "
                                     + "Scheme | Host | Path = " + scheme
                                     + " | " + targetURI.getHost() + " | "
                                     + targetURI.getPath());
                    }
                    if (scheme == null
                                && !DrawPlugin.getGui().canOpen(targetURI)) {
                        scheme = "file";
                        targetURI = new URI(scheme, targetURI.getHost(),
                                            targetURI.getPath(),
                                            targetURI.getFragment());
                    }
                    if (scheme == null) {
                        URI drawingURI = new File(drawing().getFilename()
                                                      .getCanonicalPath()).toURI();

                        URI uri = drawingURI.resolve(targetURI);
                        DrawPlugin.getGui()
                                  .openOrLoadDrawing(new File(uri.getPath()));
                    } else if ("sim".equals(scheme)) {
                        simAccess(targetURI);
                    } else {
                        Desktop.getDesktop().browse(targetURI);
                    }
                } catch (URISyntaxException urise) {
                    URI drawingURI = new File(drawing().getFilename()
                                                  .getCanonicalPath()).toURI();
                    String path = StringUtil.getPath(drawingURI.getPath());
                    Desktop.getDesktop().open(new File(path, (String) target));
                } catch (IOException e2) {
                    logger.error("Could not find file: " + targetURI.getPath()
                                 + " " + e2.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug(SelectionTool.class.getSimpleName() + ": ",
                                     e2);
                    }
                }
            } catch (Exception e3) {
                logger.error("An error occured during file access (malformed URL?): "
                             + e3.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(SelectionTool.class.getSimpleName() + ": ", e3);
                }
            }
        }
    }

    /**
     * Handles mouse drag events. The events are forwarded to the
     * current tracker.
     */
    public void mouseDrag(MouseEvent e, int x, int y) {
        if (fChild != null) { // JDK1.1 doesn't guarantee mouseDown, mouseDrag, mouseUp
            fChild.mouseDrag(e, x, y);
            fLastChild = null; // don't do double-clicks after dragging
        }
    }

    /**
     * Handles mouse up events. The events are forwarded to the
     * current tracker.
     * <p>
     * Unfreezes the view, if frozen by a <code>mouseDown</code> event.
     * </p>
     */
    public void mouseUp(MouseEvent e, int x, int y) {
        if (fFreezedView != null) {
            fFreezedView.unfreezeView();
            fFreezedView = null;
        }
        if (fChild != null) { // JDK1.1 doesn't guarantee mouseDown, mouseDrag, mouseUp
            fChild.mouseUp(e, x, y);
        }
        fChild = null;
    }

    /**
     * Factory method to create a Handle tracker. It is used to track a handle.
     */
    protected Tool createHandleTracker(DrawingEditor editor, Handle handle) {
        return new HandleTracker(editor, handle);
    }

    /**
     * Factory method to create a Drag tracker. It is used to drag a figure.
     */
    protected Tool createDragTracker(DrawingEditor editor, Figure f) {
        if (f instanceof ChildFigure) {
            return new ChildDragTracker(editor, (ChildFigure) f);
        } else {
            return new DragTracker(editor, f);
        }
    }

    /**
     * Factory method to create an area tracker. It is used to select an
     * area.
     */
    protected Tool createAreaTracker(DrawingEditor editor) {
        return new SelectAreaTracker(editor);
    }

    protected boolean alternateInspectFigure(Figure f) {
        return f.inspect(view(), true);
    }

    /**
     * Delegates the graphical feedback update to the child.
     **/
    public void draw(Graphics g) {
        if (fChild != null) {
            fChild.draw(g);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Unfreezes the view, if frozen by a <code>mouseDown</code> event.
     * </p>
     **/
    public void deactivate() {
        if (fFreezedView != null) {
            fFreezedView.unfreezeView();
            fFreezedView = null;
        }
        super.deactivate();
    }
}