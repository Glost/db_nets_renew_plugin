package CH.ifa.draw.application;

import CH.ifa.draw.figures.ImageFigureCreationDragDropListener;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingView;

import CH.ifa.draw.standard.NullDrawing;
import CH.ifa.draw.standard.NullDrawingView;
import CH.ifa.draw.standard.StandardDrawingView;

import CH.ifa.draw.util.GUIProperties;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;


public class DrawingViewFrame extends JFrame implements DrawingViewContainer {
    private StandardDrawingView drawingView;
    private JScrollPane sp;
    private InputEventForwarder forwarder;

    //private int viewNumber;
    //private static int viewCounter = 0;
    private boolean onDiscardReleaseDrawing = false;
    protected DrawingEditor editor;
    private static final int VERTICAL_SCROLL_INCREMENT = 75;
    private static final int HORIZONTAL_SCROLL_INCREMENT = 75;

    public DrawingViewFrame(DrawingEditor editor,
                            StandardDrawingView drawingView, Drawing drawing) {
        this(editor, drawingView, drawing, null, null);
    }

    public DrawingViewFrame(DrawingEditor editor,
                            StandardDrawingView drawingView, Drawing drawing,
                            Point loc, Dimension siz) {
        super();
        setDefaultCloseOperation(DrawingViewFrame.DO_NOTHING_ON_CLOSE);

        this.editor = editor;

        // Use the menu frame icon for this frame, too.
        Image menuFrameImage = editor.getIconImage();
        if (menuFrameImage != null) {
            Image iconImage = Toolkit.getDefaultToolkit()
                                     .createImage(menuFrameImage.getSource());

            // Is the next check still required?
            if (iconImage != null) {
                setIconImage(iconImage);
            }
        }


        // Set the drawing view and the drawing.
        this.drawingView = drawingView;
        drawingView.setDrawing(drawing);


        // Register the forwarder to catch all mouse and
        // keyboard events for the StandardDrawingView.
        forwarder = new InputEventForwarder(this);
        drawingView.addMouseListener(forwarder);
        drawingView.addMouseMotionListener(forwarder);
        drawingView.addKeyListener(forwarder);
        DropTargetListener dragDropListener = new ImageFigureCreationDragDropListener(this);
        new DropTarget(this.drawingView, dragDropListener);


        //viewNumber = ++viewCounter;
        addWindowListener(new ContainerEventForwarder());

        sp = new JScrollPane(drawingView,
                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.getViewport().setScrollMode(javax.swing.JViewport.BLIT_SCROLL_MODE);

        //Let the user scroll by dragging to outside the window.
        sp.setAutoscrolls(true); //enable synthetic drag events
        sp.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_INCREMENT);
        sp.getHorizontalScrollBar().setUnitIncrement(HORIZONTAL_SCROLL_INCREMENT);
        getContentPane().add(sp);


        // Either use the specified location and size parameters
        // or use default values.
        Rectangle defaults = defaultLocationAndSize(drawingView);
        if (siz == null) {
            siz = new Dimension(defaults.width, defaults.height);
        }
        if (loc == null) {
            loc = new Point(defaults.x, defaults.y);
        }


        // Evaluate the property to avoid reshaping of frames.
        // If set, set the size of the scroll pane instead.
        if (GUIProperties.avoidFrameReshape()) {
            sp.setSize(siz.width, siz.height);
            pack();
        } else {
            setBounds(loc.x, loc.y, siz.width, siz.height);
            validate();
        }
        if (editor.tool() == null) {
            editor.toolDone();
        }
        setTitle(drawing.getName()); //+"["+viewNumber+"]");
        setVisible(true);
        validate();
    }

    public JFrame getFrame() {
        return this;
    }

    public DrawingView view() {
        DrawingView view = drawingView;
        if (view == null) {
            view = NullDrawingView.INSTANCE;
        }
        return view;
    }

    protected Rectangle defaultLocationAndSize(DrawingView view) {
        Rectangle result;

        // The default size is derived from the size of the menuFrame
        Dimension menuSize = editor.getSize();
        Point menuLoc = editor.getLocationOnScreen();
        int menuX = menuLoc.x >= 0 ? menuLoc.x : 0;
        int menuY = menuLoc.y >= 0 ? menuLoc.y : 0;
        int x = menuX;
        int y = menuY + menuSize.height;
        result = new Rectangle(x, y, menuSize.width, 600);


        // If the drawing has figures, make the frame big enough
        // to show them all (variable "min") - but don't exceed
        // the screen size (variable "max").
        // We have to pay respect to the insets of the frame and
        // the scroll pane as they will obscure the borders of
        // the visible area.
        if (view.drawing().figures().hasMoreElements()) {
            // getInsets() does not provide meaningful values before
            // the frame has been made displayable via show() or pack().
            if (!isShowing()) {
                pack();
            }
            Insets scrollIns = sp.getInsets();
            Insets frameIns = this.getInsets();
            Dimension border = new Dimension(scrollIns.left + scrollIns.right
                                             + frameIns.left + frameIns.right,
                                             scrollIns.top + scrollIns.bottom
                                             + frameIns.top + frameIns.bottom);
            Dimension min = view.getPreferredSize();
            int width = min.width + border.width + 16;
            int height = min.height + border.height + 16;
            Dimension max = Toolkit.getDefaultToolkit().getScreenSize();

            // the 60 is an more or less arbitrary value for the os menu bar 
            int maxWidth = max.width - x - 60;
            int maxHeight = max.height - y - 60;
            result.setSize(Math.min(width, maxWidth),
                           Math.min(height, maxHeight));
        }

        return result;
    }

    public void onDiscardRelease() {
        onDiscardReleaseDrawing = true;
    }

    // This method cleans up the frame and removes all references
    // to other objects. Frames are not always garbage collected
    // correctly, therefore we must make sure to be as lightweight
    // as possible. This method is not named dispose(), because
    // a dispose call still leave us with a functionally intact object.
    // This call will give up the frame once and for all.
    public void discard() {
        setVisible(false);
        forwarder.discard();
        sp.remove(drawingView);
        this.remove(sp);
        if (onDiscardReleaseDrawing) {
            drawingView.drawing().release();
        }
        drawingView.setDrawing(NullDrawing.INSTANCE);
        drawingView = null;
        sp = null;

        // Maybe dispose has already been called, but let's be safe.
        dispose();
    }

    /* (non-Javadoc)
    * @see java.awt.Frame#setTitle(java.lang.String)
    */
    public void setTitle(Drawing drawing) {
        String cat = drawing.getWindowCategory();

        super.setTitle(cat.substring(0, cat.length() - 1) + "  "
                       + drawing.getName());
    }

    /**
     * An instance of this class is used to catch all input (mouse and
     * keyboard) events and forward them to the StandardDrawingView.
     * For some events, it is checked first that this frame is the
     * application's current draving view frame.
     **/
    private class InputEventForwarder implements KeyListener,
                                                 MouseMotionListener,
                                                 MouseListener {
        private DrawingViewFrame frame;
        private boolean active;

        public InputEventForwarder(DrawingViewFrame frame) {
            this.frame = frame;
            this.active = true;
        }

        // implementation of java.awt.event.MouseListener interface 
        public void mouseClicked(MouseEvent e) {
            if (active) {
                drawingView.mouseClicked(e);
            }
        }

        public void mousePressed(MouseEvent e) {
            if (active) {
                editor.setCurrentDrawing(frame);
            }
            if (active) {
                drawingView.mousePressed(e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (active) {
                drawingView.mouseReleased(e);
            }
        }

        public void mouseEntered(MouseEvent e) {
            if (active) {
                drawingView.mouseEntered(e);
            }
        }

        public void mouseExited(MouseEvent e) {
            if (active) {
                drawingView.mouseExited(e);
            }
        }

        // implementation of java.awt.event.MouseMotionListener interface 
        public void mouseDragged(MouseEvent e) {
            if (active) {
                Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                ((JPanel) e.getSource()).scrollRectToVisible(r);
                drawingView.mouseDragged(e);
            }
        }

        public void mouseMoved(MouseEvent e) {
            if (active) {
                drawingView.mouseMoved(e);
            }
        }

        // implementation of java.awt.event.KeyListener interface 
        public void keyTyped(KeyEvent e) {
            if (active) {
                drawingView.keyTyped(e);
            }
        }

        public void keyPressed(KeyEvent e) {
            if (active) {
                editor.setCurrentDrawing(frame);
            }
            if (active) {
                drawingView.keyPressed(e);
            }
        }

        public void keyReleased(KeyEvent e) {
            if (active) {
                drawingView.keyReleased(e);
            }
        }

        public void discard() {
            active = false;
            frame = null;
        }
    }

    protected class ContainerEventForwarder extends WindowAdapter {
        //public ContainerEventForwarder() {}
        public void windowActivated(WindowEvent e) {
            editor.drawingViewContainerActivated(DrawingViewFrame.this);
        }

        public void windowClosing(WindowEvent e) {
            editor.drawingViewContainerClosing(DrawingViewFrame.this);
        }
    }
}