package de.renew.minimap.component;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.figures.AttributeFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingChangeEvent;
import CH.ifa.draw.framework.DrawingChangeListener;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.standard.NullDrawing;
import CH.ifa.draw.standard.StandardDrawingView;

import CH.ifa.draw.util.DrawingHelper;

import de.renew.minimap.MiniMapPlugin;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * A MiniMapPanel shows a small image version of the current drawing in focus.
 * Also the currently clipped viewport is illustrated on the small image version.
 * The image in the MiniMapPanel updates automatically when:
 * - The drawing in focus is manipulated and the manipulation exceeds a threshold,
 * - a zoom mouse action is performed within the MiniMapPanel.
 * The viewport updates automatically when:
 * - A mouse click is made within the MiniMapPanel,
 * - the mouse is dragged within the MiniMapPanel.
 *
 * @author Christian Roeder
 *
 */
public class MiniMapPanel extends JPanel implements DrawingChangeListener,
                                                    ComponentListener,
                                                    MouseListener,
                                                    MouseMotionListener,
                                                    ActionListener,
                                                    MouseWheelListener {
    private static final int MIN_ZOOM_LEVEL = 1;
    private static final int MAX_ZOOM_LEVEL = 5;
    private static final int COMPONENT_UPDATED_THRESHOLD = 1;
    private static final int IMAGE_REGENERATION_THRESHOLD = 2000;
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                          .getLogger(MiniMapPanel.class);
    private static int zoom = 1;

    //The drawing view to be miniMapped.
    private StandardDrawingView drawingView;

    //The viewport of the drawing view.
    private Rectangle viewportFrame;
    private BufferedImage miniMap;
    private boolean dimensionsChanged = true;
    private int regenerateCount = 0;
    private boolean requestFocus = true;

    /**
     * Instantiates a new mini map panel.
     *
     * @param drawApplication the draw application
     */
    public MiniMapPanel(DrawApplication drawApplication) {
        initialize(drawApplication);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        regenerateMiniMap();
        regenerateViewportFrame();
        this.repaint();
        resizeAncestor();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentMoved(ComponentEvent e) {
        regenerateCount++;
        if (regenerateCount > COMPONENT_UPDATED_THRESHOLD) {
            // this.regenerateMiniMap();
            regenerateViewportFrame();
            regenerateCount = 0;
            this.repaint();
        }
        ;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
        regenerateCount++;
        if (regenerateCount > COMPONENT_UPDATED_THRESHOLD) {
            regenerateMiniMap();
            regenerateCount = 0;
            this.repaint();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentShown(ComponentEvent e) {
    }

    /**
     * Invalidating the drawing (by changing its figures) should result
     * in an update of the miniMap.
     *
     * @param e the e
     */
    @Override
    public void drawingInvalidated(DrawingChangeEvent e) {
        regenerateCount++;
        // This update is constrained by the value regenerateCount.
        if (regenerateCount > IMAGE_REGENERATION_THRESHOLD) {
            regenerateMiniMap();
            regenerateCount = 0;
            this.repaint();
        }
    }

    /* (non-Javadoc)
     * @see CH.ifa.draw.framework.DrawingChangeListener#drawingRequestUpdate(CH.ifa.draw.framework.DrawingChangeEvent)
     */
    @Override
    public void drawingRequestUpdate(DrawingChangeEvent e) {
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getHeight()
     */
    @Override
    public int getHeight() {
        if (miniMap == null) {
            return 0;
        }
        return miniMap.getHeight();
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(miniMap.getWidth(), miniMap.getHeight());

    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getWidth()
     */
    @Override
    public int getWidth() {
        if (miniMap == null) {
            return 0;
        }
        return miniMap.getWidth();
    }

    /**
     * When the mouse was clicked inside the miniMapPanel, the user wants to move the
     * clipping viewportFrame to another position.
     *
     * @param e MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (drawingView == null) {
            return;
        }

        // The mouse click indicates the position of the center of the viewportFrame
        int x = scale((e.getX() - viewportFrame.width / 2),
                      1D / calculateScaleFactorX());
        int y = scale((e.getY() - viewportFrame.height / 2),
                      1D / calculateScaleFactorY());

        // I don't know
        drawingView.requestFocusInWindow();
        // Update the drawingView bounds to show the correct clipping of the drawing.
        drawingView.setBounds(-x, -y, drawingView.getBounds().width,
                              drawingView.getBounds().height);
        // this.drawingView.repaint();

        // Focus the drawing
        if (requestFocus) {
            DrawPlugin.getGui().showDrawingViewContainer(drawingView.drawing());
        }
        // the viewportFrame has changed by clicking. Regenerate it.
        regenerateMiniMap();
        regenerateViewportFrame();
        this.repaint();
        e.consume();
        requestFocus = true;
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        requestFocus = false;
        mouseClicked(e);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        // Get current drawingView
        DrawingEditor drawingEditor = DrawPlugin.getCurrent().getDrawingEditor();
        DrawingView drawingView = drawingEditor.view();

        // If the view did not change, do nothing.
        if (this.drawingView == drawingView) {
            return;
        }
        // else: the dravingView changed, e.g. by focusing another drawing
        initialize((DrawApplication) drawingEditor);
        resizeAncestor();
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            zoomOut();
        } else {
            zoomIn();
        }
        regenerateMiniMap();
        regenerateViewportFrame();
        repaint();
        resizeAncestor();
        e.consume();
    }

    /**
     * All all the needed listeners.
     *
     * @param drawApplication the draw application
     */
    private void addAllListeners(DrawApplication drawApplication) {
        Drawing drawing = drawApplication.drawing();
        DrawingView drawingView = drawApplication.getView(drawing);
        StandardDrawingView standardDrawingView;
        if (drawingView instanceof StandardDrawingView) {
            standardDrawingView = (StandardDrawingView) drawingView;
            standardDrawingView.addComponentListener(this);
            this.drawingView = standardDrawingView;

        }
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
    }

    /**
     * Calculate scale factor x.
     *
     * @return the double
     */
    private double calculateScaleFactorX() {
        return zoom * 0.1;
    }

    /**
     * Calculate scale factor y.
     *
     * @return the double
     */
    private double calculateScaleFactorY() {
        return calculateScaleFactorX();
    }

    /**
     * Convert drawing to buffered image.
     *
     * @return the image as BufferedImage.
     */
    private BufferedImage createMiniMapImage() {
        if (drawingView == null) {
            dimensionsChanged = true;
            return new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        }
        Drawing originalDrawing = drawingView.drawing();
        if (originalDrawing instanceof NullDrawing) {
            return miniMap;
        }
        Drawing drawing;
        int thickness = MiniMapPlugin.getCurrent().getProperties()
                                     .getIntProperty(MiniMapPlugin.OPTIMIZE_MINI_MAP,
                                                     -1);
        if (thickness > 1) {
            try {
                drawing = DrawingHelper.cloneDrawing(originalDrawing);
                FigureEnumeration figures = drawing.figures();
                while (figures.hasMoreElements()) {
                    Figure figure = figures.nextElement();
                    figure.setAttribute(AttributeFigure.LINE_WIDTH_KEY,
                                        thickness);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // fallback, just do with the original drawing
                drawing = originalDrawing;
            }
        } else {
            drawing = originalDrawing;
        }
        Rectangle bounds = drawing.getBounds();

        // int x = bounds.x;
        // int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        // png-generation
        logger.debug(MiniMapPanel.class.getName() + ": creating image. Size: "
                     + width + " " + height);
        // create raster-image
        BufferedImage netImage = new BufferedImage(width, height,
                                                   BufferedImage.TYPE_INT_RGB);
        int scaledWidth = scale(width, calculateScaleFactorX());
        int scaledHeight = scale(height, calculateScaleFactorY());

        BufferedImage target = new BufferedImage(scaledWidth, scaledHeight,
                                                 BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = netImage.createGraphics();
        // set renderer to render fast, quality as in Renew
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                                  RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                  RenderingHints.VALUE_COLOR_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                                  RenderingHints.VALUE_RENDER_SPEED);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                  RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        // white background
        graphics.setColor(Color.WHITE);
        // graphics.setBackground(new Color(1,1,1,0));
        graphics.fillRect(0, 0, width, height);

        /*
         * if (removeWhiteSpace) { graphics.translate(x * -1, y * -1); }
         */


        // render drawing
        drawing.draw(graphics);
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(calculateScaleFactorX(), calculateScaleFactorY());
        AffineTransformOp scaleOp = new AffineTransformOp(affineTransform,
                                                          AffineTransformOp.TYPE_BILINEAR);
        target = scaleOp.filter(netImage, target);
        dimensionsChanged = didDimensionsChange(target.getWidth(),
                                                target.getHeight());
        return target;
    }

    /**
     * Check if two dimensions are equal to the dimension of the cached miniMap.
     *
     * @param width the width
     * @param height the height
     * @return true, if successful
     */
    private boolean didDimensionsChange(int width, int height) {
        if (miniMap == null) {
            return true;
        }
        if (miniMap.getWidth() != width || miniMap.getHeight() != height) {
            return true;
        }
        return false;
    }

    /**
     * Initialize the MiniMapPanel.
     *
     * @param drawApplication the draw application
     */
    private void initialize(DrawApplication drawApplication) {
        addAllListeners(drawApplication);
        regenerateMiniMap();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        regenerateViewportFrame();
        repaint();
    }

    /**
     * Regenerate the miniMap by redrawing the image.
     */
    private void regenerateMiniMap() {
        miniMap = createMiniMapImage();
    }

    /**
     * Regenerate the viewportFrame.
     */
    private void regenerateViewportFrame() {
        if (drawingView == null) {
            viewportFrame = new Rectangle();
            return;
        }
        Rectangle visibleRectangle = drawingView.getVisibleRect();
        int scaledX = scale(visibleRectangle.x, calculateScaleFactorX());
        int scaledY = scale(visibleRectangle.y, calculateScaleFactorY());
        int scaledW = scale(visibleRectangle.width, calculateScaleFactorX());
        int scaledH = scale(visibleRectangle.height, calculateScaleFactorY());

        viewportFrame = new Rectangle(scaledX, scaledY, scaledW, scaledH);
    }

    /**
     * Resize the anchestor JFrame.
     */
    private void resizeAncestor() {
        if (!dimensionsChanged) {
            return;
        }
        Container component = getTopLevelAncestor();
        if (component instanceof JFrame && miniMap != null) {
            ((JFrame) getTopLevelAncestor()).pack();
            ;
        }
        dimensionsChanged = false;
    }

    /**
     * Scale a dimension by a scale factor.
     *
     * @param dimension the dimension
     * @param scaleFactor the scale factor
     * @return the int
     */
    private int scale(int dimension, double scaleFactor) {
        Double rD = dimension * scaleFactor;
        return rD.intValue();
    }

    /**
     * Increase the zoom level.
     */
    private void zoomIn() {
        if (zoom >= MAX_ZOOM_LEVEL) {
            return;
        }
        zoom++;
    }

    /**
     * Decrease the zoom level.
     */
    private void zoomOut() {
        if (zoom <= MIN_ZOOM_LEVEL) {
            return;
        }
        zoom--;

    }

    /**
     * Paint this component by drawing the image and drawing the viewportFrame.
     *
     * @param g the g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(miniMap, 0, 0, miniMap.getWidth(), miniMap.getHeight(), null);
        g.drawRect(viewportFrame.x, viewportFrame.y, viewportFrame.width,
                   viewportFrame.height);
        Color color = new Color(55, 55, 55, 10);
        g.setColor(color);
        g.fillRect(viewportFrame.x, viewportFrame.y, viewportFrame.width,
                   viewportFrame.height);

    }
}