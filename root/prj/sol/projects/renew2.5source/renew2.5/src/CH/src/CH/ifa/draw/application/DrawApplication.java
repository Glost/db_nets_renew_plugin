/*
 * @(#)DrawApplication.java 5.1
 *
 */
package CH.ifa.draw.application;

import CH.ifa.draw.DrawPlugin;
import CH.ifa.draw.IOHelper;

import CH.ifa.draw.contrib.DiamondFigure;
import CH.ifa.draw.contrib.PolygonTool;
import CH.ifa.draw.contrib.TriangleFigure;

import CH.ifa.draw.figures.ConnectedTextTool;
import CH.ifa.draw.figures.ElbowConnection;
import CH.ifa.draw.figures.EllipseFigure;
import CH.ifa.draw.figures.ImageFigureCreationTool;
import CH.ifa.draw.figures.LineConnection;
import CH.ifa.draw.figures.LineFigure;
import CH.ifa.draw.figures.PieFigure;
import CH.ifa.draw.figures.RectangleFigure;
import CH.ifa.draw.figures.RoundRectangleFigure;
import CH.ifa.draw.figures.ScribbleTool;
import CH.ifa.draw.figures.TargetTool;
import CH.ifa.draw.figures.TextFigure;
import CH.ifa.draw.figures.TextTool;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.DrawingTypeManager;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.framework.UndoRedoManager;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.ImportHolder;
import CH.ifa.draw.io.PositionedDrawing;
import CH.ifa.draw.io.SimpleFileFilter;
import CH.ifa.draw.io.StatusDisplayer;
import CH.ifa.draw.io.importFormats.ImportFormat;

import CH.ifa.draw.standard.ConnectionTool;
import CH.ifa.draw.standard.CreationTool;
import CH.ifa.draw.standard.FigureEnumerator;
import CH.ifa.draw.standard.FigureException;
import CH.ifa.draw.standard.GridConstrainer;
import CH.ifa.draw.standard.NullDrawingView;
import CH.ifa.draw.standard.SelectionTool;
import CH.ifa.draw.standard.StandardDrawing;
import CH.ifa.draw.standard.StandardDrawingView;
import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.AutosaveManager;
import CH.ifa.draw.util.AutosaveSaver;
import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.DrawingListener;
import CH.ifa.draw.util.DynamicFlowLayout;
import CH.ifa.draw.util.Fontkit;
import CH.ifa.draw.util.GUIProperties;
import CH.ifa.draw.util.HotDrawFocusManager;
import CH.ifa.draw.util.Iconkit;
import CH.ifa.draw.util.Palette;
import CH.ifa.draw.util.PaletteListener;

import de.renew.util.StringUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;


/**
 * DrawApplication defines a standard presentation for
 * standalone drawing editors. The presentation is
 * customized in subclasses.
 * The application is started as follows:
 * <pre>
 * public static void main(String[] args) {
 *     MyDrawApp window = new MyDrawApp();
 * }
 * </pre>
 *
 * <p>
 * <strong>All</strong> methods of this class
 * <strong>must</strong> be called from the AWT/Swing event
 * thread to avoid concurrency problems, including the constructor.
 * A single exception is the method {@link #loadAndOpenCommandLineDrawings}.
 * </p>
 **/
public class DrawApplication implements DrawingEditor, PaletteListener,
                                        StatusDisplayer, AutosaveSaver {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawApplication.class);
    private static int windowCount = 0;

    // the image resource path
    private static final String fgDrawPath = "/CH/ifa/draw/";

    /**
     * The image folder path
     */
    public static final String IMAGES = fgDrawPath + "images/";

    /**
     * The index of the file menu in the menu bar.
     */
    public static final int FILE_MENU = 0;

    /**
     * The index of the edit menu in the menu bar.
     */
    public static final int EDIT_MENU = 1;

    /**
     * The index of the alignment menu in the menu bar.
     */
    public static final int LAYOUT_MENU = 2;

    /**
     * The index of the attributes menu in the menu bar.
     */
    public static final int ATTRIBUTES_MENU = 3;

    /**
     * The index of the drawings menu in the menu bar.
     */
    public static final int DRAWINGS_MENU = 4;

    /**
     * The minimum time interval that has to elapse before a
     * drawing is saved automatically.
     */
    private static final int AUTOSAVE_INTERVAL = 120000;

    //    private SimpleFileFilter fLastSelectedFileFilter;
    //    private DrawingTypeManager drawingTypeManager;
    protected JFrame menuFrame;
    protected JFrame toolFrame;
    private JPanel toolPanel;
    static Font fMenuFont;
    private Vector<Drawing> fDrawings = new Vector<Drawing>();
    private Tool fTool;
    private Iconkit fIconkit;

    // private WindowsMenu fDrawingsMenu;
    private JTextField fStatusLine;
    protected Vector<DrawingViewContainer> fViewContainers = new Vector<DrawingViewContainer>();
    protected DrawingViewContainer fViewContainer;
    private boolean fToolButtonSticky = false;
    private boolean fAlwaysSticky = false;
    private ToolButton fDefaultToolButton;
    private ToolButton fSelectedToolButton = null;
    private ToolButton fConnTextTB;
    private ToolButton fTextTB;
    private ToolButton fTargetTextTB;
    protected DrawingViewContainerSupplier dvContainerSupplier;

    /**
     * The autosave manager that is associated to this
     * DrawingEditor.
     */
    private AutosaveManager autosaveManager = new AutosaveManager(this,
                                                                  AUTOSAVE_INTERVAL);

    /**
     * The undo and redo history manager which keeps
     * track of undo/redo snapshots.
     **/
    private UndoRedoManager undoManager = new UndoRedoManager(this);
    private Vector<DrawingListener> drawingListeners;


    /**
     * Constructs a drawing window with a default title.
     * The constructor has to be called in sync with the AWT
     * event queue (see class documentation).
     */
    public DrawApplication() {
        this("JHotDraw");
    }

    /**
     * Constructs a drawing window with the given title.
     * The constructor has to be called in sync with the AWT
     * event queue (see class documentation).
     *
     * @param title the title of the application window.
     */
    public DrawApplication(String title) {
        this(title, new String[0]);
    }

    /**
     * Constructs a drawing window with the given title and loads
     * drawings from the given file names.
     * The constructor has to be called in sync with the AWT
     * event queue (see class documentation).
     *
     * @param title the title of the application window.
     * @param drawings an array of file names to load. May not
     *                          be <code>null</code>, but can be an empty
     *                          array.
     */
    public DrawApplication(String title, String[] drawings) {
        this(title, null, null, drawings, null);
    }

    /**
     * Constructs a drawing window and configures it with the
     * given title, icon and default file type. Then loads
     * drawings from the given file names.
     * <p>
     * The constructor has to be called in sync with the AWT
     * event queue (see class documentation).
     * </p>
     *
     * @param title             the title of the application window.
     * @param defaultDrawingType the name used during registration of
     *                          <code>defaultFileFilter</code> at the
     *                          {@link DrawingTypeManager}.
     *                          This parameter is ignored when
     *                          <code>defaultFileFilter==null</code>.
     *                          Otherwise, it must not be <code>null</code>!
     * @param defaultFileFilter the default file type to use for drawings.
     *                          If <code>null</code>, the default type is
     *                          used.
     * @param drawings          an array of file names to load. May not
     *                          be <code>null</code>, but can be an empty
     *                          array.
     * @param icon              the path to the application icon resource.
     *                          If <code>null</code>, the default icon is
     *                          used.
     **/
    public DrawApplication(String title, String defaultDrawingType,
                           SimpleFileFilter defaultFileFilter,
                           String[] drawings, String icon) {
        DrawPlugin.setGui(this);
        drawingListeners = new Vector<DrawingListener>();
        // get the DrawingTypeManager so that PLugins can Register Drawings and also 
        // export filter to those.
        dvContainerSupplier = new DefaultDrawingViewContainerSupplier();
        //        drawingTypeManager = DrawingTypeManager.getInstance();
        if (defaultFileFilter != null) {
            DrawingTypeManager dtm = DrawingTypeManager.getInstance();
            dtm.register(defaultDrawingType, defaultFileFilter);
            dtm.setDefaultFileFilter(defaultFileFilter);
        }

        //        lastSelectedFileFilter = drawingTypeManager.getDefaultFileFilter();
        DrawingLoadServer dls = null;
        int loadServerPort = GUIProperties.loadServerPort();
        if (loadServerPort != -1) {
            try {
                dls = new DrawingLoadServer(this, loadServerPort);
            } catch (IOException ioe) {
                logger.error("Server Socket is occupied!");
            }
        }

        menuFrame = new JFrame(title);
        fStatusLine = createStatusLine();
        menuFrame.getContentPane().add(wrapStatusLine(fStatusLine), "South");

        //		toolFrame = new Frame("Tools");
        //		toolFrame.setLayout(new BorderLayout());
        toolFrame = menuFrame;
        fIconkit = new Iconkit(toolFrame);
        if (icon != null) {
            Image iconImage = fIconkit.loadImage(icon);
            if (iconImage == null) {
                logger.error("Resource icon " + icon + " could not be loaded!");
            } else {
                menuFrame.setIconImage(iconImage);
            }
        }

        toolPanel = new JPanel();
        toolPanel.setBackground(Color.lightGray);
        toolPanel.setLayout(new DynamicFlowLayout(FlowLayout.LEFT, 2, 0));
        toolFrame.getContentPane().add(toolPanel, "Center");

        createTools(toolPanel);

        JMenuBar mb = new JMenuBar();
        fMenuFont = mb.getFont();
        int menuFontSize = GUIProperties.menuFontSize();
        if (menuFontSize != -1) {
            logger.debug("Setting menu font size to " + menuFontSize + " pt.");
            fMenuFont = Fontkit.getFont("SansSerif", Font.PLAIN, menuFontSize);
        }
        menuFrame.setJMenuBar(mb);
        //        createMenus(mb);
        addListeners();

        /* ***Workaround***:
         * jdk1.4 and greater does not forward Keystrokes to other Frames.
         * So we have to override the DefaultKeyboardFocusManager.
         * This class exists since jdk1.4
         */
        new HotDrawFocusManager(menuFrame.getJMenuBar());

        // This should show the main window. However, there were some problems
        // in the very early versions of Renew, where the appearance of the
        // main window would depend crucially on the order of some
        // commands. It is not clear whether the errors can occur again.
        // This must be monitored.
        menuFrame.pack();

        menuFrame.setVisible(true);

        if (toolFrame != menuFrame) {
            toolFrame.setVisible(true);
        }


        windowCount++;
        toolDone();

        // this should be changed 
        // since DrawApplication cannot access Properties for it is no Plugin yet
        // there is no way to have this optional        
        //        if (fDrawings.size() == 0 ) {
        //            promptNew();
        //        }
        loadAndOpenCommandLineDrawings(drawings);

        if (dls != null) {
            dls.start();
        }

        JMenuItem showMainWindowItem = new JMenuItem("Menu and tools");
        showMainWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                                                                 Toolkit.getDefaultToolkit()
                                                                        .getMenuShortcutKeyMask()));
        showMainWindowItem.addActionListener(new ActionListener() {
                public final void actionPerformed(final ActionEvent e) {
                    JFrame menuFrame = getFrame();
                    menuFrame.setExtendedState(menuFrame.getExtendedState()
                                               & ~JFrame.ICONIFIED);
                    menuFrame.toFront();
                }
            });
        MenuManager.getInstance()
                   .registerMenu(DrawPlugin.WINDOWS_MENU, showMainWindowItem,
                                 DrawPlugin.MENU_PREFIX
                                 + DrawPlugin.WINDOWS_MENU + "Menu and tools");
        MenuManager.getInstance().setGui(this);

        logger.debug("DrawApplication: started gui.");
    }

    // end of Constructor 
    protected Component wrapStatusLine(Component statusLine) {
        return statusLine;
    }

    public Image getIconImage() {
        return menuFrame.getIconImage();
    }

    /**
     * @return the menu frame
     */
    public JFrame getFrame() {
        return menuFrame;
    }

    /**
     * Registers the listeners for the main windows
     */
    protected void addListeners() {
        menuFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        menuFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    requestClose();
                }

                public void windowDeactivated(WindowEvent event) {
                    // logger.debug("DEACTIVATED: " + event);
                }
            });

        menuFrame.addWindowFocusListener(new WindowFocusListener() {
                public void windowGainedFocus(WindowEvent e) {
                }

                public void windowLostFocus(WindowEvent e) {
                    // logger.debug("LOSTFOCUS: " + e);
                }
            });


        /* endif */
        menuFrame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent event) {
                    if (!GUIProperties.avoidFrameReshape()) {
                        Dimension dim = menuFrame.getPreferredSize();
                        Dimension is = menuFrame.getSize();
                        menuFrame.setSize(Math.max(dim.width, is.width),
                                          dim.height);
                    }

                    //menuFrame.pack();
                }
            });

        // Add a drag and drop listener which opens all files that are dragged 
        // onto the menu frame. 
        DropTargetListener dragDropListener = new AbstractFileDragDropListener() {
            @Override
            protected void handleFiles(final File[] files, Point loc) {
                for (File file : files) {
                    if (!file.isDirectory()) {
                        openOrLoadDrawing(file);
                    }
                }
            }
        };
        new DropTarget(menuFrame, dragDropListener);
    }

    //public JFrame menuFrame() {
    //    return menuFrame;
    //}
    public void dispatchEvent(KeyEvent evt) {
        menuFrame.dispatchEvent(evt);
    }


    /**
     * Need the toolPanel for adding a new palette.
     * @return the tools panel
     *
     */
    public JPanel getToolsPanel() {
        return toolPanel;
    }

    /**
     * @return the drawing view container supplier
     */
    public DrawingViewContainerSupplier getDrawingViewContainerSupplier() {
        return dvContainerSupplier;
    }

    /**
     * sets the drawing view container supplier to the given value
     *
     * @param newDVCSupplier new supplier
     */
    public void setDrawingViewContainerSupplier(DrawingViewContainerSupplier newDVCSupplier) {
        this.dvContainerSupplier = newDVCSupplier;
    }

    protected DrawApplication newWindow() {
        return new DrawApplication(menuFrame.getTitle());
    }

    private void updateName(Drawing drawing, File filename) {
        showDrawingViewContainer(drawing);
        try {
            filename = filename.getCanonicalFile();
        } catch (IOException e) {
            logger.error(e);
        }
        String nameOnly = StringUtil.getFilename(filename.getName());
        fViewContainer.setTitle(nameOnly);
        if (fViewContainer instanceof RootPaneContainer) {
            // Improve look and feel under MacOS X by showing document
            // icon in window title bar.
            ((RootPaneContainer) fViewContainer).getRootPane()
             .putClientProperty("Window.documentFile", filename);
        }
        drawing.setName(nameOnly);
        drawing.setFilename(filename);
        getWindowsMenu().setName(drawing, nameOnly);
    }

    void addMenu(JMenu newMenu, int index) {
        menuFrame.getJMenuBar().add(newMenu, index);
        menuFrame.pack();
    }

    void addMenu(JMenu newMenu) {
        menuFrame.getJMenuBar().add(newMenu);
        menuFrame.pack();
    }

    void removeMenu(JMenu remove) {
        menuFrame.getJMenuBar().remove(remove);
        menuFrame.pack();
        //		menuFrame.show();
    }

    //    /**
    //     * Creates the standard menus. Clients add MenuCreator objects to
    //     * add additional menus.
    //     */
    //    protected void createMenus(JMenuBar mb) {
    //        List menuCreators = DrawMenuExtender.getInstance().getMenuCreators();
    //        Iterator cr = menuCreators.iterator();
    //        while (cr.hasNext()) {
    //            MenuCreator c = (MenuCreator) cr.next();
    //            JMenu menu = c.createMenu(this);
    //            logger.debug("created menu " + menu.getText());
    //            mb.add(menu);
    //            registerMenu(menu.getText(), menu);
    //        }
    //        menuFrame.pack();
    //    }
    //    public Collection getMenuNames() {
    //        return _menus.keySet();
    //    }
    //
    //    protected JMenu getMenu(String menuName) {
    //        return (JMenu) _menus.get(menuName);
    //    }
    //
    //    public void updateMenus() {
    //        _menus.clear();
    //        JMenuBar bar = menuFrame.getJMenuBar();
    //        bar.removeAll();
    //        createMenus(bar);
    //    }
    //
    //    void registerMenu(String menuString, JMenu m) {
    //        _menus.put(menuString, m);
    //
    //        JMenuBar bar = getFrame().getJMenuBar();
    //        bar.add(m);
    //    }
    //
    //    public void setWindowMenu(WindowsMenu menu) {
    //        fDrawingsMenu = menu;
    //    }
    private WindowsMenu getWindowsMenu() {
        return MenuManager.getInstance().getWindowsMenu();
    }

    /**
     * Adds a drawing to the list of drawings
     * @param drawing the drawing to add
     */
    public void addDrawing(Drawing drawing) {
        if (!fDrawings.contains(drawing)) {
            fDrawings.addElement(drawing);
            //changed
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(drawing.getName());
            mi.setFont(fMenuFont);
            mi.addActionListener(new DrawingMenuListener(this, drawing));
            getWindowsMenu().addDrawing(drawing, mi);

            // initialize undo history management for the new drawing
            undoManager.newUndoHistory(drawing);

            drawingAdded(drawing);
        }
        /* endif */
    }

    /**
     * Closes the given drawing view frame.
     * If the respective drawing has unsaved modifications, the
     * user is asked to save the drawing. The user also has the
     * choice to cancel the close operation.
     *
     * @param viewContainer the drawing window to close.
     * @return <code>true</code>, if the window has been closed.
     **/
    protected boolean closeViewContainer(DrawingViewContainer viewContainer) {
        boolean closeOK = checkDrawingModifiedOnClose(viewContainer);
        if (closeOK) {
            destroyViewContainer(viewContainer);
        }
        return closeOK;
    }

    /**
     * Checks whether the given drawing has unsaved modifications
     * and provides an opportunity to save the changes.
     *
     * @param viewContainer the drawing window to check
     * @return <code>true</code> if the drawing window may be closed.
     **/
    protected boolean checkDrawingModifiedOnClose(DrawingViewContainer viewContainer) {
        Drawing drawing = viewContainer.view().drawing();
        if (drawing.isModified()) {
            showDrawingViewContainer(drawing);


            // int answer = MessageDialog.invokeDialog(menuFrame, 
            //                                         "Renew: Confirm Close", 
            //                                         new String[] { "The drawing \""
            //                                         + drawing.getName() + "\"", "you are about to close", "has been modified." }, 
            //                                         MessageDialog.LEFT, 
            //                                         new String[] { " Save now ", " Close ", " Cancel " });
            Object[] options = { "Save now", "Close / Do not save", "Cancel" };
            Icon icon = null;
            int answer = JOptionPane.showOptionDialog(viewContainer.getFrame(),
                                                      "Renew: "
                                                      + "The drawing \""
                                                      + drawing.getName()
                                                      + "\""
                                                      + " you are about to close"
                                                      + " has been modified."
                                                      + "\n What do you want to do?",
                                                      "Save drawing?", 0,
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      icon, options, options[0]);

            switch (answer) {
            case 0:
                // User wants to save. Return the success of the
                // save operation, because he can still choose cancel.
                return saveDrawing(drawing);
            case 1:
                // User wants to continue without save.
                break;
            case 2:
                // User wants to cancel.
                return false;
            case JOptionPane.CLOSED_OPTION:
                // User pressed escape or closed the window (probably wants to cancel)
                return false;
            default:
                assert false : "JOptionPane returned unexpected result: "
                + answer;
            }
        }
        return true;
    }

    /**
     * Closes the given drawing view frame immediately.
     * The user is <i>not</i> asked to save changes.
     *
     * @param viewContainer the drawing window to close
     **/
    public void destroyViewContainer(DrawingViewContainer viewContainer) {
        toolDone();
        Drawing drawing = viewContainer.view().drawing();
        drawing.release();
        drawingReleased(drawing);
        viewContainer.discard();

        fViewContainers.removeElement(viewContainer);
        if (viewContainer == fViewContainer) {
            int numViews = fViewContainers.size();
            if (numViews > 0) {
                setCurrentDrawing(fViewContainers.elementAt(numViews - 1));
                fViewContainer.getFrame().setVisible(true);
                fViewContainer.getFrame().setState(JFrame.NORMAL);
                fViewContainer.getFrame().requestFocus(); // !!!
            } else {
                setCurrentDrawing(null);
            }
        }

        int pos = fDrawings.indexOf(drawing);
        fDrawings.removeElementAt(pos);
        getWindowsMenu().removeDrawing(drawing);

        undoManager.removeUndoHistory(drawing);

        autosaveManager.removeDrawing(drawing);
    }

    private void drawingAdded(Drawing drawing) {
        Iterator<DrawingListener> iter = drawingListeners.iterator();
        while (iter.hasNext()) {
            iter.next().drawingAdded(drawing);
        }
    }

    private void drawingReleased(Drawing drawing) {
        Iterator<DrawingListener> iter = drawingListeners.iterator();
        while (iter.hasNext()) {
            iter.next().drawingReleased(drawing);
        }
    }

    /**
     * Adds drawing listener to the list of drawing listeners
     *
     * @param listener the listener to add
     */
    public void addDrawingListener(DrawingListener listener) {
        drawingListeners.add(listener);
    }

    /**
     * @return the menu font used by this application
     */
    public static Font getMenuFont() {
        return fMenuFont;
    }

    // this is such a stupid method...
    /**
     * Creates a menu item with the given name, shortcut and action listener
     *
     * @param name the name of the menu item
     * @param shortcut the shortcut for the menu item
     * @param action the action listener for the menu item
     * @return the created menu item
     */
    public static JMenuItem createMenuItem(String name, int shortcut,
                                           ActionListener action) {
        JMenuItem mi;
        if (shortcut == 0) {
            mi = new JMenuItem(name);
        } else {
            mi = new JMenuItem(name, shortcut);
            mi.setAccelerator(KeyStroke.getKeyStroke(shortcut,
                                                     Toolkit.getDefaultToolkit()
                                                            .getMenuShortcutKeyMask()));
        }
        mi.setFont(fMenuFont);
        mi.addActionListener(action);
        return mi;
    }

    /**
     * Creates a menu item with the given name and action listener.
     *
     * @param name the name of the menu item.
     * @param action the action listener for the menu item
     * @return the created menu item
     */
    public static JMenuItem createMenuItem(String name, ActionListener action) {
        return createMenuItem(name, 0, action);
    }


    /**
     * Creates a command menu with the given name.
     *
     * @param name the name of the new command menu.
     * @return the created command menu.
     */
    public static CommandMenu createCommandMenu(String name) {
        CommandMenu cm = new CommandMenu(name);
        cm.setFont(fMenuFont);
        return cm;
    }

    /**
     * @return the drawings of this application.
     */
    public Enumeration<Drawing> drawings() {
        //cloned to avoid inconsistencies in local drawing Enumerations(!)
        return (new Vector<Drawing>(fDrawings)).elements();
    }

    /**
     * Creates a new drawing view and a new drawing view frame.
     * The default size and location as implemented by the
     * <code>DrawingViewContainer</code> constructor will be used.
     *
     * @param drawing  the drawing to be displayed in the view.
     *
     * @return the new drawing view frame
     **/
    public DrawingViewContainer newDrawingViewContainer(Drawing drawing) {
        return newDrawingViewContainer(drawing, null, null);
    }

    /**
     * Creates a new drawing view and a new drawing view frame.
     *
     * @param drawing  the drawing to be displayed in the view.
     * @param loc      the location of the new drawing view frame
     *                 on the screen.
     *                 If <code>null</code>, the view frame
     *                 defaults will be used.
     * @param size     the size of the new drawing view frame.
     *                 If <code>null</code>, the view frame
     *                 defaults will be used.
     *
     * @return the new drawing view frame
     **/
    public DrawingViewContainer newDrawingViewContainer(Drawing drawing,
                                                        Point loc,
                                                        Dimension size) {
        logger.debug("DrawApplication: newDrawingViewContainer!!!");
        Dimension d = drawing.defaultSize();
        StandardDrawingView drawingView = createDrawingView(d.width, d.height);
        boolean grid = DrawPlugin.getCurrent().getProperties()
                                 .getBoolProperty(DrawPlugin.CH_IFA_DRAW_GRID_DEFAULT);
        if (grid) {
            int gridSize = DrawPlugin.getCurrent().getProperties()
                                     .getIntProperty(DrawPlugin.CH_IFA_DRAW_GRID_SIZE,
                                                     5);
            drawingView.setConstrainer(new GridConstrainer(gridSize, gridSize));
        }
        addDrawing(drawing);
        DrawingViewContainer viewContainer = dvContainerSupplier.getContainer(this,
                                                                              drawingView,
                                                                              drawing,
                                                                              loc,
                                                                              size);
        if ((viewContainer instanceof RootPaneContainer)
                    && (drawing.getFilename() != null)) {
            // Improve look and feel under MacOS X by showing document
            // icon in window title bar.
            ((RootPaneContainer) viewContainer).getRootPane()
             .putClientProperty("Window.documentFile", drawing.getFilename());
        }
        fViewContainers.addElement(viewContainer);
        setCurrentDrawing(viewContainer);
        return viewContainer;
    }

    /**
     * Creates the tools. By default all standard figure creation tools are added.
     * Override this method to remove the standard tools.
     * Call the inherited method to include the standard tools.
     * @param toolPanel the panel where the tool palette is added.
     */
    protected void createTools(JPanel toolPanel) {
        Palette palette = new Palette("Drawing Tools");

        Tool tool = createSelectionTool();
        fDefaultToolButton = createToolButton(IMAGES + "SEL", "Selection Tool",
                                              tool);
        palette.add(fDefaultToolButton);

        tool = new CreationTool(this, new RectangleFigure());
        palette.add(createToolButton(IMAGES + "RECT", "Rectangle Tool", tool));

        tool = new CreationTool(this, new RoundRectangleFigure());
        palette.add(createToolButton(IMAGES + "RRECT", "Round Rectangle Tool",
                                     tool));

        tool = new CreationTool(this, new EllipseFigure());
        palette.add(createToolButton(IMAGES + "ELLIPSE", "Ellipse Tool", tool));

        tool = new CreationTool(this, new PieFigure());
        palette.add(createToolButton(IMAGES + "PIE", "Elliptical Arc/Pie Tool",
                                     tool));

        tool = new CreationTool(this, new DiamondFigure());
        palette.add(createToolButton(IMAGES + "DIAMOND", "Diamond Tool", tool));

        tool = new CreationTool(this, new TriangleFigure());
        palette.add(createToolButton(IMAGES + "TRIANGLE", "Triangle Tool", tool));

        tool = new CreationTool(this, new LineFigure());
        palette.add(createToolButton(IMAGES + "LINE", "Line Tool", tool));

        tool = new ConnectionTool(this, new LineConnection());
        palette.add(createToolButton(IMAGES + "CONN", "Connection Tool", tool));

        tool = new ConnectionTool(this, new ElbowConnection());
        palette.add(createToolButton(IMAGES + "OCONN", "Elbow Connection Tool",
                                     tool));

        tool = new ScribbleTool(this);
        palette.add(createToolButton(IMAGES + "SCRIBBL", "Scribble Tool", tool));

        tool = new PolygonTool(this);
        palette.add(createToolButton(IMAGES + "POLYGON", "Polygon Tool", tool));

        tool = new ImageFigureCreationTool(this, menuFrame);
        palette.add(createToolButton(IMAGES + "IMAGE", "Image Tool", tool));

        TextFigure prototype = new TextFigure(false);
        prototype.setAlignment(TextFigure.LEFT);
        tool = new TextTool(this, prototype);
        fTextTB = createToolButton(IMAGES + "TEXT", "Text Tool", tool);
        palette.add(fTextTB);

        prototype = new TextFigure();
        prototype.setAlignment(TextFigure.CENTER);
        tool = new ConnectedTextTool(this, prototype);
        fConnTextTB = createToolButton(IMAGES + "ATEXT", "Connected Text Tool",
                                       tool);
        palette.add(fConnTextTB);

        tool = new TargetTool(this);
        fTargetTextTB = createToolButton(IMAGES + "TARGETTEXT", "Target Tool",
                                         tool);
        palette.add(fTargetTextTB);

        toolPanel.add(palette.getComponent());
    }

    /**
     * Creates the selection tool used in this editor. Override to use
     * a custom selection tool.
     */
    protected Tool createSelectionTool() {
        return new SelectionTool(this);
    }

    /**
     * Creates a tool button with the given image, tool, and text
     *
     * @param iconName the icon name for the tool button
     * @param toolName the name of the tool button
     * @param tool the tool associated with the tool button
     * @return the created tool button
     */
    public ToolButton createToolButton(String iconName, String toolName,
                                       Tool tool) {
        return new ToolButton(this, iconName, toolName, tool);
    }

    /**
     * Creates the drawing view used in this application.
     * You need to override this method to use a DrawingView
     * subclass in your application. By default a standard
     * DrawingView is returned.
     * @param width  the default width of the new view.
     * @param height the default height of the new view.
     * @return the new view.
     */
    protected StandardDrawingView createDrawingView(int width, int height) {
        return new StandardDrawingView(this, width, height);
    }

    /**
     * Clears the current drawing
     */
    public void unsetCurrentDrawing() {
        if (fViewContainer != null) {
            getWindowsMenu().deactivate(drawing());
            fViewContainer = null;
        }
    }

    public void setCurrentDrawing(DrawingViewContainer viewContainer) {
        if (viewContainer == null) {
            unsetCurrentDrawing();
        } else if (fViewContainer == viewContainer) {
            // do nothing, as the frame is already current.
        } else {
            Drawing frameDrawing = viewContainer.view().drawing();

            logger.debug("Trying to activate drawing " + frameDrawing.getName());
            int pos = fDrawings.indexOf(frameDrawing);

            // Is the drawing still active or do we process a delayed event?
            if (pos >= 0 && fViewContainers.removeElement(viewContainer)) {
                fViewContainers.addElement(viewContainer);
                unsetCurrentDrawing();


                // new: move viewContainer to last position in vector
                // end new
                fViewContainer = viewContainer;
                getWindowsMenu().activate(frameDrawing);

                // check if the setting of this drawing changes anything
                // about the availability of menu items
                recheckMenus();

                //logger.debug("Activated drawing "+frameDrawing.getName());
                //displayZOrder();
                // Adapt menu entries to the new situation
                menuStateChanged();
            }
        }
    }

    /**
     * Check through all the menus, setting their enabledness correctly
     */
    public void recheckMenus() {
        logger.debug("rechecking menu states...");
        JMenuBar bar = getFrame().getJMenuBar();
        int count = bar.getMenuCount();
        for (int i = 0; i < count; i++) {
            //JMenu m = (JMenu) _menus.get(keys.next());
            JMenu m = bar.getMenu(i);
            if (m instanceof CommandMenu) {
                ((CommandMenu) m).checkEnabled();
            }
        }
    }

    /* *****
    private void displayZOrder() {
     for (int i=0; i<fViewContainers.size(); ++i) {
      logger.debug(i+". "+((DrawingViewContainer)fViewContainers.elementAt(i)).view().drawing().getName());
     }
    }
    ***** */


    /**
     * Gets the view container for the given drawing.
     *
     * @param drawing the drawing whose container is searched.
     * @return the view container of the given drawing.
     */
    public DrawingViewContainer getViewContainer(Drawing drawing) {
        Enumeration<DrawingViewContainer> viewContainers = fViewContainers
                                                               .elements();
        while (viewContainers.hasMoreElements()) {
            DrawingViewContainer viewContainer = viewContainers.nextElement();
            if (viewContainer.view().drawing() == drawing) {
                return viewContainer;
            }
        }
        return null;
    }

    /**
     * Gets the view for the given drawing.
     *
     * @param drawing the drawing whose view is searched.
     * @return the view of the given drawing.
     */
    public DrawingView getView(Drawing drawing) {
        DrawingViewContainer viewContainer = getViewContainer(drawing);
        if (viewContainer != null) {
            return viewContainer.view();
        }
        return null;
    }

    /**
     * Sets the specified drawing, its view and view frame as
     * the current drawing and view.
     * A view for the drawing will be created if no one exists.
     * Default position and size for the new view will be taken
     * from the view frame.
     *
     * @param drawing     the drawing to be shown.
     *
     * @return the view frame that is now current.
     **/
    public DrawingViewContainer showDrawingViewContainer(Drawing drawing) {
        return showDrawingViewContainer(drawing, null, null);
    }

    /**
     * Sets the specified drawing, its view and view frame as
     * the current drawing and view.
     * A view for the drawing will be created if no one exists.
     * Default position and size for the new view can be
     * specified.
     *
     * @param drawing     the drawing to be shown.
     * @param defaultLoc  the default location for a new view
     *                    frame, if one has to be created.
     *                    If <code>null</code>, the view frame
     *                    defaults will be used.
     * @param defaultSize the default size of a new view
     *                    frame, if one has to be created.
     *                    If <code>null</code>, the view frame
     *                    defaults will be used.
     *
     * @return the view frame that is now current.
     **/
    public DrawingViewContainer showDrawingViewContainer(Drawing drawing,
                                                         Point defaultLoc,
                                                         Dimension defaultSize) {
        DrawingViewContainer viewContainer = getViewContainer(drawing);
        if (viewContainer != null) {
            setCurrentDrawing(viewContainer);
            // this is a workaround, we have not found a possibility yet to request
            if (DrawPlugin.getCurrent().getProperties()
                                  .getBoolProperty(DrawPlugin.WINDOW_FOCUS_WORKAROUND_KEY)) {
                viewContainer.getFrame().setVisible(false);
            }
            viewContainer.getFrame().setVisible(true);
            viewContainer.getFrame().setState(JFrame.NORMAL);
            viewContainer.getFrame().requestFocus(); // was missing!!!
            if (logger.isDebugEnabled()) {
                logger.debug(DrawApplication.class.getSimpleName() + ": "
                             + viewContainer.getFrame().getTitle()
                             + " requests focus.");
            }
        } else {
            // Nothing was found. Create a new frame.
            viewContainer = newDrawingViewContainer(drawing, defaultLoc,
                                                    defaultSize);
        }
        return viewContainer;
    }

    /**
     * Closes the drawing
     * @param drawing the drawing to close.
     * @return true if the closing was successful.
     */
    public boolean closeDrawing(Drawing drawing) {
        DrawingViewContainer viewContainer = getViewContainer(drawing);
        if (viewContainer != null) {
            return closeViewContainer(viewContainer);
        }
        return false;
    }

    /**
     * Creates the drawing used in this application.
     * You need to override this method to use a Drawing
     * subclass in your application. By default a standard
     * Drawing is returned.
     */
    protected Drawing createDrawing() {
        return new StandardDrawing();
    }

    /**
     * Creates the status line.
     */
    protected JTextField createStatusLine() {
        JTextField field = new JTextField("No Tool", 40); //55
        field.setEditable(false);
        return field;
    }

    /**
     * Handles a user selection in the palette.
     * @see PaletteListener
     */
    public void paletteUserSelected(ToolButton button, boolean doubleclick) {
        setSelected(button);
        setTool(button.tool(), button.name() + button.getHotkey());
        fToolButtonSticky = !fAlwaysSticky && doubleclick
                            || fAlwaysSticky && !doubleclick;
    }

    /**
     * Handles when the mouse enters or leaves a palette button.
     * @see PaletteListener
     */
    public void paletteUserOver(ToolButton button, boolean inside) {
        if (inside) {
            showStatus(button.name() + button.getHotkey());
        } else if (fSelectedToolButton != null) {
            showStatus(fSelectedToolButton.name() + button.getHotkey());
        }
    }

    /**
     * Gets the current drawing.
     * @see DrawingEditor
     */
    public Drawing drawing() {
        return view().drawing();
    }

    /**
     * Gets the current tool.
     * @see DrawingEditor
     */
    public Tool tool() {
        return fTool;
    }

    /**
     * Gets the current drawing view.
     * @see DrawingEditor
     */
    public DrawingView view() {
        DrawingViewContainer viewContainer = fViewContainer;
        if (viewContainer == null) {
            return NullDrawingView.INSTANCE;
        } else {
            return viewContainer.view();
        }
    }

    /**
     * Gets the drawing view that was active before the current drawing view.
     * @see DrawingEditor
     */
    public DrawingView previousView() {
        int numViews = fViewContainers.size();
        if (numViews >= 2) {
            return fViewContainers.elementAt(numViews - 2).view();
        }
        return null;
    }

    /**
     * Toggle the sticky mode.
     */
    public void toggleAlwaysSticky() {
        fAlwaysSticky = !fAlwaysSticky;
        fToolButtonSticky = fAlwaysSticky;
    }

    /**
     * Gets the sticky tool mode.
     */
    public boolean isStickyTools() {
        return fToolButtonSticky;
    }

    /**
     * Set the sticky tools mode.
     */
    public void setStickyTools(boolean sticky) {
        fToolButtonSticky = sticky;
    }

    /**
     * Gets the editor's default tool.
     * This is usually the selection tool.
     */
    public Tool defaultTool() {
        return fDefaultToolButton.tool();
    }

    /**
     * Sets the default tool of the editor.
     * @see DrawingEditor
     */
    public void toolDone() {
        // not while initializing!
        if (windowCount > 0) {
            if (fToolButtonSticky && tool() != null) {
                tool().deactivate();
                tool().activate();
            } else if (fDefaultToolButton != null) {
                setTool(fDefaultToolButton.tool(), fDefaultToolButton.name());
                setSelected(fDefaultToolButton);
            }
        }
    }

    /**
     * Handles a change of the current selection. Updates all
     * menu items that are selection sensitive.
     * @see DrawingEditor
     */
    public void selectionChanged(DrawingView view) {
        menuStateChanged();
    }

    public void menuStateChanged() {
        JMenuBar mb = menuFrame.getJMenuBar();
        CommandMenu fileMenu = (CommandMenu) mb.getMenu(FILE_MENU);
        fileMenu.checkEnabled();
        CommandMenu editMenu = (CommandMenu) mb.getMenu(EDIT_MENU);
        editMenu.checkEnabled();
        CommandMenu layoutMenu = (CommandMenu) mb.getMenu(LAYOUT_MENU);
        layoutMenu.checkEnabled();
        CommandMenu attributeMenu = (CommandMenu) mb.getMenu(ATTRIBUTES_MENU);
        attributeMenu.checkEnabled();
    }

    /**
     * Shows a status message.
     * As an exception to all other gui methods, this one may be called asynchronously.
     * @see DrawingEditor
     */
    public void showStatus(final String string) {
        if (EventQueue.isDispatchThread()) {
            fStatusLine.setText(string);
        } else {
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        fStatusLine.setText(string);
                    }
                });
        }
        logger.debug(string);
    }

    private void showAndPrint(String string) {
        fStatusLine.setText(string);
        logger.info(string);
    }

    private void setTool(Tool t, String name) {
        if (fTool != null) {
            fTool.deactivate();
        }
        fTool = t;
        if (fTool != null) {
            fStatusLine.setText(name);
            fTool.activate();
        }
    }

    private void setSelected(ToolButton button) {
        if (fSelectedToolButton != null) {
            fSelectedToolButton.button().setSelected(false);
        }
        fSelectedToolButton = button;
        if (fSelectedToolButton != null) {
            fSelectedToolButton.button().setSelected(true);
        }
    }

    /**
     * Gets tool button dependent of the type of the given figure.
     * @param figure the figure for which the tool button is searched.
     * @return the tool button for the figure.
     */
    public ToolButton toolButtonForTextFigure(TextFigure figure) {
        if (figure.parent() == null) {
            return fTextTB;
        } else {
            return fConnTextTB;
        }
    }

    /**
     * Opens the text edit for the given figure in the given line and column.
     * @param textFigure the figure which should be edited.
     * @param line the line where the edit should be opened.
     * @param column the column where the edit should be opened.
     */
    public void doTextEdit(TextFigure textFigure, int line, int column) {
        ToolButton tb = toolButtonForTextFigure(textFigure);
        paletteUserSelected(tb, fToolButtonSticky);
        TextTool tt = (TextTool) tb.tool();
        tt.beginEdit(textFigure);
        if (line > 0 && column > 0) {
            tt.setCaretPosition(line, column);
        }
    }

    /**
     * Opens the text edit for the given figure and select part of the text.
     * @param textFigure the figure which should be edited.
     * @param startLine the line where the selection should start.
     * @param startColumn the column where the selection should start.
     * @param endLine the line where the selection should end.
     * @param endColumn the column where the selection should end.
     */
    public void doTextEditSelected(TextFigure textFigure, int startLine,
                                   int startColumn, int endLine, int endColumn) {
        ToolButton tb = toolButtonForTextFigure(textFigure);
        paletteUserSelected(tb, fToolButtonSticky);
        TextTool tt = (TextTool) tb.tool();
        tt.beginEdit(textFigure);
        if ((startLine < endLine)
                    || ((startLine == endLine) && (startColumn < endColumn))) {
            tt.select(startLine, startColumn, endLine, endColumn);
        }
    }

    /**
     * Opens the text edit for the given figure.
     * @param textFigure the figure which should be edited.
     */
    public void doTextEdit(TextFigure textFigure) {
        doTextEdit(textFigure, 0, 0);
    }

    /**
     * This method is called when the user requests application
     * termination via menu or via window decorations.
     * <p>
     * Steps taken:
     * <ol>
     * <li>Check if data would be lost and ask the user if needed
     *     (<code>canClose()</code> method).</li>
     * <li>Close all windows and do other cleanup work
     *     (<code>exit()</code> method, which in turn calls
     *     <code>destroy()</code>).</li>
     * </ol>
     * </p>
     **/
    protected void requestClose() {
        if (canClose()) {
            exit();
        }
    }

    /**
     * Exits the application. You should never override this method.
     * Override <code>destroy()</code> instead, which is called from
     * this method.
     * <p><i>
     * Note: The final <code>System.exit()</code> call has been
     * deactivated to give control back to the Renew plugin manager.
     * </i></p>
     */
    public void exit() {
        // Close all DrawingViewContainers:
        while (true) {
            Enumeration<DrawingViewContainer> viewContainers = fViewContainers
                                                                   .elements();
            if (!viewContainers.hasMoreElements()) {
                break;
            } else {
                DrawingViewContainer viewContainer = viewContainers.nextElement();
                destroyViewContainer(viewContainer);
            }
        }

        // Unregister our special WindowsMenu item
        MenuManager.getInstance()
                   .unregisterMenu(DrawPlugin.MENU_PREFIX
                                   + DrawPlugin.WINDOWS_MENU + "Menu and tools");

        // Close the main menu/tools frame.
        destroy();
        toolFrame.setVisible(false);
        toolFrame.dispose();
        menuFrame.setVisible(false); // hide the Frame
        menuFrame.dispose(); // tell windowing system to free resources
        if (--windowCount <= 0) {
            //            System.exit(0);
        }
    }

    /**
     * Checks whether the application may be terminated. May
     * query the user if needed. The default implementation
     * always returns true.
     *
     * @return <code>true</code>, if no objections are made
     *         against exiting the application.
     **/
    public boolean canClose() {
        boolean closeOK = true;
        Enumeration<DrawingViewContainer> viewContainers = new Vector<DrawingViewContainer>(fViewContainers)
                                                           .elements();
        while (closeOK && viewContainers.hasMoreElements()) {
            DrawingViewContainer viewContainer = viewContainers.nextElement();
            closeOK = checkDrawingModifiedOnClose(viewContainer);
        }
        return closeOK;
    }

    /**
     * Handles additional clean up operations. Override to destroy
     * or release drawing editor resources.
     */
    protected void destroy() {
    }

    /**
     * Creates a new drawing view with a new empty drawing.
     * @return the new drawing.
     */
    public Drawing promptNew() {
        Drawing drawing = createDrawing();
        openDrawing(drawing);
        return drawing;
    }

    /**
     * Creates a new drawing view with a new empty drawing of choosable
     * type. The user is presented with all known drawing types.
     * @return the created drawing. Returns <code>null</code>, if the
     *         dialog was cancelled by the user.
     **/
    public Drawing promptChooseNew() {
        DrawingTypeManager dtm = DrawingTypeManager.getInstance();
        Map<String, SimpleFileFilter> drawingTypes = dtm.getDrawingTypes();
        Collection<String> typeNames = drawingTypes.keySet();
        String[] types = typeNames.toArray(new String[typeNames.size()]);
        String[] descriptions = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            descriptions[i] = drawingTypes.get(types[i]).getDescription();
        }
        NewDrawingDialog dialog = new NewDrawingDialog(DrawPlugin.getGui()
                                                                 .getFrame(),
                                                       descriptions, 0);
        int result = dialog.showDialog();
        if (result >= 0 && result < types.length) {
            Drawing drawing = DrawingTypeManager.getDrawingFromName(types[result]);
            openDrawing(drawing);
            return drawing;
        }
        return null;
    }

    /**
     * Shows a file dialog and opens a drawing.
     * @param ff the file filter for the file dialog.
     */
    public void promptOpen(FileFilter ff) {
        File[] files = getIOHelper().getLoadPath(null, ff, true);
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (ff instanceof SimpleFileFilter) {
                    files[i] = DrawingFileHelper.checkAndAddExtension(files[i],
                                                                      (SimpleFileFilter) ff);
                }
                openOrLoadDrawing(files[i]);
            }
        }
    }

    void promptOpenURL() {
        String header = "Type in URL:\n Accepted protocols are: http, file, jar.\n"
                        + "If not specified I will try to use http.\n"
                        + "Examples:\nhttp://my.server.tld/referencenet.rnw.\n"
                        + " file:///my/path/to/nets/net.rnw.\n"
                        + "jar:file:///my/path/to/jar/the.jar!/path/to/file/net.rnw";
        String in = JOptionPane.showInputDialog(DrawPlugin.getGui().getFrame(),
                                                header);
        if (in != null) {
            // this is maybe to restrictive but rather be on the safe side
            if (!in.startsWith("jar:") && !in.startsWith("file://")) {
                if (!in.startsWith("http://")) {
                    in = "http://" + in; // if none of the ones above, try http
                }
            }

            // logger.debug(in);
            try {
                URL url = new URL(in);
                logger.debug(url);
                getIOHelper().loadAndOpenDrawing(url);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private IOHelper getIOHelper() {
        return DrawPlugin.getCurrent().getIOHelper();
    }

    private ImportHolder getImportHolder() {
        return DrawPlugin.getCurrent().getImportHolder();
    }

    /**
     * Shows a file dialog and inserts the chosen drawing into the current
     * drawing.
     **/
    protected void promptInsert() {
        SimpleFileFilter ff = drawing().getDefaultFileFilter();
        File file = getIOHelper().getLoadPath(null, ff);
        file = DrawingFileHelper.checkAndAddExtension(file, ff);
        if (file != null) {
            loadAndInsertDrawing(file);
        }
    }

    /**
     * Saves the current drawing to a file whose name is queried from the
     * user. The drawing's name can change during this process.
     * @see #saveDrawingAs(Drawing)
     **/
    protected void saveDrawingAs() {
        saveDrawingAs(drawing());
    }

    /**
     * Saves the given drawing to a file whose name is queried from the
     * user. The drawing's name can change during this process.
     * @param drawing the drawing to save.
     * @see #promptSaveAs
     **/
    public void saveDrawingAs(Drawing drawing) {
        File filename = drawing.getFilename();
        if (!promptSaveAs(drawing)) {
            this.showAndPrint("Could not save File: " + filename);
        } else {
            filename = drawing.getFilename();
            saveDrawing(drawing, filename);
        }
    }

    /**
     * Saves the given drawing to its file. If the drawing's file is not
     * known, the user is asked to supply a file name. In this case, the
     * drawing's name can change.
     * @param drawing the drawing to save.
     * @return <code>true</code>,  if the drawing has been saved.<br>
     *         <code>false</code>, if the action was cancelled for
     *                             some reason.
     * @see #saveDrawingAs(Drawing)
     **/
    public boolean saveDrawing(Drawing drawing) {
        File filename = drawing.getFilename();
        if (filename == null) {
            if (!promptSaveAs(drawing)) {
                return false;
            }
            filename = drawing.getFilename();
        }
        saveDrawing(drawing, filename);
        if (logger.isDebugEnabled()) {
            logger.debug(DrawApplication.class.getName() + ": saved drawing: "
                         + filename);
        }
        DrawPlugin.getCurrent()
                  .updateRecentlySavedList(filename.getAbsolutePath());
        return true;
    }

    /**
     * Opens the drawing from the given file. If it is not in the drawing list it will be loaded.
     * @param file the file of the drawing.
     */
    public void openOrLoadDrawing(File file) {
        Enumeration<Drawing> en = drawings();
        while (en.hasMoreElements()) {
            Drawing drawing = en.nextElement();
            if (logger.isDebugEnabled()) {
                logger.debug(DrawApplication.class.getSimpleName()
                             + ": checking equality of loaded drawings and newly opened drawing: "
                             + drawing.getFilename());
                logger.debug(DrawApplication.class.getSimpleName()
                             + ": path = " + file.getAbsolutePath());
            }
            if (drawing.getFilename() != null
                        && drawing.getFilename().getAbsoluteFile()
                                          .equals(file.getAbsoluteFile())) {
                showDrawingViewContainer(drawing);
                return;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(DrawApplication.class.getSimpleName()
                         + ": no matching drawing found proceeding with loadAndOpenDrawing "
                         + file.getAbsolutePath());
        }
        getIOHelper().loadAndOpenDrawing(file);

    }


    /**
     * Opens the drawing with the given path. If it is not in the drawing list it will be loaded.
     * @param path the path of the drawing.
     */
    public void openOrLoadDrawing(String path) {
        openOrLoadDrawing(new File(path));
    }


    /**
    * Saves the given drawing to the given file. There is no user
    * interaction.
    * @param drawing  the drawing to save.
    * @param filename the name of the file to write to.
    **/
    protected void saveDrawing(Drawing drawing, File filename) {
        String text = " drawing " + drawing.getName() + " as " + filename;
        showAndPrint("Saving" + text + "...");
        DrawingViewContainer viewContainer = getViewContainer(drawing);
        PositionedDrawing positionedDrawing = new PositionedDrawing(viewContainer
                                                                    .getLocation(),
                                                                    viewContainer
                                                                    .getSize(),
                                                                    drawing);
        DrawingFileHelper.savePositionedDrawing(positionedDrawing, filename,
                                                this);
        showStatus("Saved" + text + ".");
        autosaveManager.renameDrawing(drawing);
    }

    /**
     * Shows a file dialog for saving the current drawing.
     * @return <code>true</code>,  if a file name was chosen and set.<br>
     *         <code>false</code>, if the action was cancelled for
     *                             some reason.
     * @see #promptSaveAs(Drawing)
     **/
    protected boolean promptSaveAs() {
        return promptSaveAs(drawing());
    }

    /**
     * Shows a file dialog for saving the given drawing. The default file
     * filter of the drawing is used. The chosen file name is checked to
     * end with one of the file filter's allowed extensions.
     * <p>
     * The chosen file name is not returned. Instead, if a file name is
     * determined, the drawing's name (and all related editor information)
     * is set accordingly.
     * </p>
     * @return <code>true</code>,  if a file name was chosen and set.<br>
     *         <code>false</code>, if the action was cancelled for
     *                             some reason.
     **/
    protected boolean promptSaveAs(Drawing drawing) {
        SimpleFileFilter ff = drawing.getDefaultFileFilter();
        File file = drawing.getFilename();
        if (file == null) {
            file = new File(getIOHelper().getLastPath(), drawing.getName());
            file = DrawingFileHelper.checkAndAddExtension(file, ff);
        }
        toolDone();


        // get the save path to know where to store the drawing
        file = getIOHelper().getSavePath(file, ff);

        if (file == null) {
            return false;
        }
        file = DrawingFileHelper.checkAndAddExtension(file, ff);

        // If the file exists already, ask for permission to overwrite
        if (file.exists()) {
            // new Swing confirm dialog
            int answer = JOptionPane.showConfirmDialog(this.getViewContainer(drawing)
                                                           .getFrame(),
                                                       "The file \"" + file
                                                       + "\""
                                                       + " does already exist."
                                                       + "\nDo you want do proceed?",
                                                       "Renew: Confirm overwrite.",
                                                       JOptionPane.YES_NO_OPTION);
            if (answer >= 1) {
                return false;
            }
        }


        // Set the new file name for the drawing
        updateName(drawing, file);


        // OK: Make sure to create another backup.
        drawing.setBackupStatus(false);
        return true;
    }


    /**
     * Prints the drawing.
     */
    public void print() {
        fTool.deactivate();
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(view());
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                logger.debug(e.getMessage(), e);
                JOptionPane.showMessageDialog(getFrame(),
                                              "Printing of this Drawing failed.\n"
                                              + "Reason: " + e.getMessage(),
                                              "Printer Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
        fTool.activate();
    }


    /**
     * Saves the given drawing to the given autosave file.
     * The drawing's dirty flag is not cleared.
     *
     * @param drawing  the drawing to save.
     * @param file     the name of the autosave file.
     * @param loc      the location of the drawing's view window; it will
     *                 be saved to the file, too.
     * @param size     width and height of the drawing's view window; they
     *                 will be saved to the file, too.
     * @exception IOException if an I/O-failure occurs.
     **/
    public void saveAutosaveFile(Drawing drawing, File file, Point loc,
                                 Dimension size) throws IOException {
        DrawingFileHelper.saveAsStorableOutput(drawing, file, loc, size, false);
    }

    /**
     * Loads drawings from files and opens them in the editor. There is
     * no feedback to the caller whether the operation was successful. But
     * the user will be informed about failures in the same way as if he
     * has requested the file by himself.
     * A failure while loading one file will not prevent the other files
     * from being treated.
     * <p>
     * This method is thread-safe, meaning it can be called from outside
     * the AWT event queue. The request will be detached from the calling
     * thread and synchronized with the AWT thread.
     * </p>
     *
     * @param filenames  the names of the files to read, given as array.
     *                   Each array entry stands for exactly one file name.
     *                   The file names are given in OS-specific notation.
     **/
    public void loadAndOpenCommandLineDrawings(final String[] filenames) {
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    for (int i = 0; i < filenames.length; ++i) {
                        try {
                            getIOHelper()
                                .loadAndOpenDrawing(new URL(filenames[i]));
                        } catch (MalformedURLException e2) {
                            openOrLoadDrawing(filenames[i]);
                        }
                    }
                }
            });
    }

    /**
     * Load a drawing from the given url and opens it in the editor. There is
     * no feedback to the caller whether the operation was successful. But
     * the user will be informed about failures in the same way as if he
     * has requested the file by himself.
     * <p>
     * This method is thread-safe, meaning it can be called from outside
     * the AWT event queue. The request will be detached from the calling
     * thread and synchronized with the AWT thread.
     * </p>
     *
     * @param url  the url to read
     **/
    public void loadAndOpenCommandLineDrawing(final URL url) {
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    getIOHelper().loadAndOpenDrawing(url);
                }
            });
    }


    /**
     * Opens the given drawing in the editor.
     *
     * @param posDrawing a <code>Drawing</code> along with its positioning
     *                   information.
     **/
    public synchronized void openDrawing(PositionedDrawing posDrawing) {
        if (posDrawing != null) {
            Drawing drawing = posDrawing.getDrawing();
            if (posDrawing.getWindowLocation() != null
                        && posDrawing.getWindowDimension() != null) {
                // restore old window position
                newDrawingViewContainer(drawing,
                                        posDrawing.getWindowLocation(),
                                        posDrawing.getWindowDimension());
            } else {
                newDrawingViewContainer(drawing);
            }
            autosaveManager.addDrawing(drawing);
        }
    }


    /**
     * Opens the given drawing in the editor.
     * The view frame is created with default position and size.
     *
     * @param drawing  the <code>Drawing</code> to open.
     * @return the drawing.
     **/
    public Drawing openDrawing(Drawing drawing) {
        newDrawingViewContainer(drawing);
        autosaveManager.addDrawing(drawing);
        return drawing;
    }

    /**
     * Loads the given positioned drawing in a new view container.
     * @param drawing the drawing which should be loaded.
     */
    public void loadInViewContainer(PositionedDrawing drawing) {
        newDrawingViewContainer(drawing.getDrawing(),
                                drawing.getWindowLocation(),
                                drawing.getWindowDimension());
        autosaveManager.addDrawing(drawing.getDrawing());
    }

    /**
     * Loads a drawing from the given file and inserts all its figures into
     * the current drawing.
     *
     * @param file  the file name where to retrieve the drawing.
     **/
    protected synchronized void loadAndInsertDrawing(File file) {
        Drawing existingDrawing = drawing();
        DrawingView existingView = view();
        Drawing newDrawing = DrawingFileHelper.loadDrawing(file, null);
        if (newDrawing != null) {
            FigureEnumeration figures = newDrawing.figures();
            existingView.clearSelection();
            while (figures.hasMoreElements()) {
                Figure fig = figures.nextFigure();
                existingDrawing.add(fig);
                existingView.addToSelection(fig);
            }
            existingView.checkDamage();
        }
    }

    public void drawingViewContainerActivated(DrawingViewContainer viewContainer) {
        if (viewContainer != null && viewContainer != fViewContainer) {
            //logger.debug("Current Drawing View Window: "+viewContainer.getTitle());
            setCurrentDrawing(viewContainer);
        }
        if (GUIProperties.fixMenus()) {
            MenuSelectionManager.defaultManager().clearSelectedPath();
        }
    }

    public void drawingViewContainerClosing(DrawingViewContainer viewContainer) {
        closeViewContainer(viewContainer);
    }

    public UndoRedoManager getUndoRedoManager() {
        return undoManager;
    }

    public void prepareUndoSnapshot() {
        Drawing currentDrawing = drawing();
        undoManager.prepareUndoSnapshot(currentDrawing);
    }

    public void commitUndoSnapshot() {
        Drawing currentDrawing = drawing();
        undoManager.commitUndoSnapshot(currentDrawing);
    }

    /**
     * Prohibits the management of undo and redo snapshots
     * for the given drawing.
     **/
    protected void noUndoHistoryFor(Drawing drawing) {
        undoManager.removeUndoHistory(drawing);
    }

    /**
     * Selects the corresponding elements for the given FigureException.
     *
     * @param e the FigureException whose offending elements are searched.
     * @return the offending elements for the exception.
     */
    public boolean selectOffendingElements(FigureException e) {
        if (e.errorDrawing != null && !e.errorFigures.isEmpty()) {
            view().clearSelection();
            showDrawingViewContainer(e.errorDrawing);
            FigureEnumeration errorFigures = new FigureEnumerator(e.errorFigures);
            view().addToSelectionAll(errorFigures);


            // Redraw the newly selected elements.
            view().repairDamage();
            if (e.textErrorFigure != null) {
                view().showElement(e.textErrorFigure);
                doTextEdit(e.textErrorFigure, e.line, e.column);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean canOpen(URI path) {
        Iterator<SimpleFileFilter> iterator = getIOHelper().getFileFilter()
                                                  .getFileFilters().iterator();
        while (iterator.hasNext()) {
            SimpleFileFilter filter = (SimpleFileFilter) iterator.next();
            if (filter.accept(new File(path.getPath()))) {
                return true;
            }
        }
        ImportFormat[] allImportFormats = getImportHolder().allImportFormats();
        for (ImportFormat importFormat : allImportFormats) {
            if (importFormat.canImport(path)) {
                return true;
            }
        }
        return false;
    }

    //	---------------------------------------------------------------------
    // Implementation of the ImportHolder Interface
    // ---------------------------------------------------------------------
    //    protected CommandMenu importMenu;
    //
    //    /**
    //     *
    //     */
    //    public void addImportFormat(ImportFormat importFormat) {
    //        importHolder.addImportFormat(importFormat);
    //    }
    //
    //    /**
    //     *
    //     */
    //    public void removeImportFormat(ImportFormat format) {
    //        importHolder.removeImportFormat(format);
    //    }
    //
    //    /**
    //     *
    //     */
    //    public ImportFormat[] allImportFormats() {
    //        return importHolder.allImportFormats();
    //    }
    //	---------------------------------------------------------------------
    // Implementation of the ExportHolder Interface
    // ----------------------------------------------------------------------
    //    protected CommandMenu exportMenu;
    //
    //    public void addExportFormat(ExportFormat exportFormat) {
    //        exportHolder.addExportFormat(exportFormat);
    //    }
    //
    //    public void removeExportFormat(ExportFormat exportFormat) {
    //        exportHolder.removeExportFormat(exportFormat);
    //    }
    //
    //    public ExportFormat[] allExportFormats() {
    //        ExportFormat[] result = null;
    //        result = exportHolder.allExportFormats();
    //        return result;
    //    }
    // init ExportHolder
    //    private void createDefaultExportFormats() {
    //        addExportFormat(new PSExportFormat());
    //        addExportFormat(new EPSExportFormat());
    //    }
    //-- main -----------------------------------------------------------
    /**
     * Starts an application with the given args.
     *
     * @param args array of arguments for the application.
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new DrawApplication("JHotDraw", args);
                }
            });
    }

    //---------------------- INNER CLASS to supply DefaultDrawingViewContainerSupplier ----------------------
    /**
     * Default implementation of the DrawingViewContainerSupplier
     *
     * @see DrawingViewContainerSupplier
     */
    public static class DefaultDrawingViewContainerSupplier
            implements DrawingViewContainerSupplier {
        public DrawingViewContainer getContainer(DrawApplication appl,
                                                 StandardDrawingView drawingView,
                                                 Drawing drawing, Point loc,
                                                 Dimension size) {
            return new DrawingViewFrame(appl, drawingView, drawing, loc, size);
        }
    }

    public Dimension getSize() {
        return menuFrame.getSize();
    }

    public Point getLocationOnScreen() {
        return menuFrame.getLocationOnScreen();
    }
}

class DrawingMenuListener implements ActionListener {
    private DrawApplication fEditor;
    private Drawing fDrawing;

    DrawingMenuListener(DrawApplication editor, Drawing drawing) {
        fEditor = editor;
        fDrawing = drawing;
    }

    public void actionPerformed(ActionEvent e) {
        fEditor.showDrawingViewContainer(fDrawing);
    }
}

class DrawingLoadServer extends Thread {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(DrawingLoadServer.class);
    private DrawApplication editor;
    private ServerSocket s = null;

    public DrawingLoadServer(DrawApplication editor, int port)
            throws IOException {
        this.editor = editor;
        logger.debug("Drawing Load Server setting up server socket at port "
                     + port + " on loopback interface...");
        // JavaDoc of InetAddress.getByName states that a name of null
        // returns a local loopback address - exactly what we want here.
        s = new ServerSocket(port, 50, InetAddress.getByName(null));
        logger.debug("Drawing Load Server bound to: " + s);
    }

    public void run() {
        logger.debug("Drawing Load Server waiting for parameters...");
        while (true) {
            Socket client = null;
            try {
                client = s.accept();
                logger.debug("Parameter server accepted client.");
                BufferedReader in = new BufferedReader(new InputStreamReader(client
                                                                             .getInputStream()));
                try {
                    String drawingFileName;
                    do {
                        drawingFileName = in.readLine();
                        if (drawingFileName != null) {
                            logger.debug("Received Parameter "
                                         + drawingFileName);
                            editor.loadAndOpenCommandLineDrawings(new String[] { drawingFileName });
                        }
                    } while (drawingFileName != null);
                } catch (IOException e) {
                }
                logger.debug("Connection closed.");
                in.close();
                client.close();
            } catch (Exception e) {
                logger.error("Drawing Load Server threw exception: " + e);
            }
        }
    }
}