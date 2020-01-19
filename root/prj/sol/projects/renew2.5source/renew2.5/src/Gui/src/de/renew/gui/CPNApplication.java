package de.renew.gui;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.DrawingViewContainer;

import CH.ifa.draw.figures.ConnectedTextTool;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureWithID;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.ConnectionTool;
import CH.ifa.draw.standard.StandardDrawingView;
import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.DrawingListener;
import CH.ifa.draw.util.Palette;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.searchqueue.TimeListener;

import de.renew.io.RNWFileFilter;

import de.renew.plugin.PluginManager;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.PlaceInstanceAccessor;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.SyntaxException;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.rmi.RemoteException;

import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/** This class represents the main frame of the graphical Renew
 * user interface. It is based on an old and heavily modified
 * version of JHotDraw that can be found in the CH plugin.
 * The frame comprises the menu bar and all editing tools.
 * It also covers all saving, loading, compiling and simulating
 * features of the application (and has grown too fat, by the way).
 *
 * <p> <strong>All</strong> methods of this class
 * <strong>must</strong> be called from the AWT/Swing event
 * thread to avoid concurrency problems, including the
 * constructor.  </p>
 *
 * @author Frank Wienberg
 * @author Olaf Kummer
 * @author Michael Duvigneau
 * @author Joern Schumacher
 * @author Lawrence Cabac
 *
 * @see DrawApplication
 **/
public class CPNApplication extends DrawApplication implements DrawingListener {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(CPNApplication.class);
    static public final String CPNIMAGES = "/de/renew/gui/images/";
    static private final Color INSTANCE_COLOR = new Color(200, 200, 255);
    static private final Color TOKEN_BAG_COLOR = new Color(230, 230, 255);
    private ToolButton fInscrTB;
    private ToolButton fNameTB;
    private ToolButton fDeclTB;
    private ToolButton fAuxTB;
    private ToolButton fCommTB;

    //    private ToolButton fConnTextTB;
    //    private ToolButton fTextTB;
    //    private BreakpointManager bpManager = null;
    //private CPNDrawingLoader fDrawingLoader;
    private GuiPlugin fPlugin;

    //    private HashMap _menus;
    //    private SyntaxExceptionFrame syntaxFrame = null;
    //    private FileFilter fileType;
    // objects that can create top level menus
    // private List _menuCreators = new Vector();
    // objects that extend menus that are already present
    // private List _menuExtenders = new Vector();
    protected CPNApplication(GuiPlugin plugin) {
        this(new String[0], plugin);
    }

    protected CPNApplication(String[] args, GuiPlugin plugin) {
        super("Reference Net Workshop", "de.renew.gui.CPNDrawing",
              new RNWFileFilter(), args, CPNIMAGES + "RENEW.gif");
        addDrawingListener(this);

        //fDrawingLoader = ModeReplacement.getInstance().getDrawingLoader();
        fPlugin = plugin;
        if (fPlugin != null) {
            fPlugin.notifyGuiStart(this);
        }
    }

    public ShadowCompilerFactory getDefaultCompilerFactory() {
        return ModeReplacement.getInstance().getDefaultCompilerFactory();
    }

    protected Drawing createDrawing() {
        return new CPNDrawing();
    }

    protected DrawApplication newWindow() {
        return new CPNApplication(fPlugin);
    }

    //    protected JMenu getMenu(String menuName) {
    //        return super.getMenu(menuName);
    //    }


    /**
     * Informs this gui that the simulation has been terminated.
     * All simulation-related windows will be closed.
     * <p>
     * This method must be called in sync with the AWT event queue.
     * </p>
     **/
    void cleanupSimulationWindows() {
        showStatus("Simulation terminated.");
        BindingSelectionFrame.close();
        closeAllSimulationDrawings();
    }

    void closeAllSimulationDrawings() {
        boolean goon;

        do {
            goon = false;
            Enumeration<Drawing> drawenumeration = drawings();

            while (drawenumeration.hasMoreElements()) {
                Drawing drawing = drawenumeration.nextElement();

                if (drawing instanceof CPNInstanceDrawing
                            || drawing instanceof TokenBagDrawing) {
                    closeDrawing(drawing);
                    goon = true;
                    break;
                }
            }
        } while (goon);
    }

    public void openNetPatternDrawing(String netName) {
        openNetPatternDrawing(netName, FigureWithID.NOID);
    }

    public void openNetPatternDrawing(String netName, int elementID) {
        DrawingViewContainer container = internalShowNetPatternDrawing(netName,
                                                                       elementID);

        if (container != null) {
            container.getFrame().toFront();
        }
    }

    protected DrawingViewContainer internalShowNetPatternDrawing(String netName,
                                                                 int elementID) {
        Enumeration<Drawing> drawenumeration = drawings();

        CPNDrawing drawing = null;
        while ((drawenumeration.hasMoreElements()) && (drawing == null)) {
            Object next = drawenumeration.nextElement();
            if (next instanceof CPNDrawing) {
                if (((CPNDrawing) next).getName().equals(netName)) {
                    drawing = (CPNDrawing) next;
                }
            }
        }

        if (drawing == null) {
            drawing = ModeReplacement.getInstance().getDrawingLoader()
                                     .getDrawing(netName);
        }

        if (drawing != null) {
            // bring drawing to front
            DrawingViewContainer netContainer = getViewContainer(drawing);
            DrawingViewContainer viewContainer = showDrawingViewContainer(drawing,
                                                                          netContainer
                                                                          .getLocation(),
                                                                          netContainer
                                                                          .getSize());


            if (elementID != FigureWithID.NOID) {
                Figure elementFigure = (drawing).getFigureWithID(elementID);
                if (elementFigure != null) {
                    DrawingView elementView = viewContainer.view();
                    elementView.clearSelection();
                    elementView.addToSelection(elementFigure);

                    // Redraw the newly selected elements.
                    elementView.repairDamage();
                }
            }

            return viewContainer;
        }

        return null;
    }

    protected StandardDrawingView createDrawingView(int width, int height) {
        return new CPNDrawingView(this, width, height);
    }

    protected Component wrapStatusLine(Component statusLine) {
        boolean showClock = ModeReplacement.getInstance().getSimulation()
                                           .isStrictlySequential();
        if (showClock) {
            JPanel panel = new JPanel();
            GridBagLayout layout = new GridBagLayout();

            panel.setLayout(layout);

            GridBagConstraints constraints = new GridBagConstraints();

            constraints.fill = GridBagConstraints.HORIZONTAL;

            constraints.weightx = 1;
            layout.setConstraints(statusLine, constraints);
            panel.add(statusLine);
            if (statusLine instanceof JTextField) {
                // make original status line a bit smaller
                JTextField textStatusLine = (JTextField) statusLine;

                textStatusLine.setColumns(textStatusLine.getColumns() - 23);
            }
            final JTextField clock = new JTextField("0.0", 23);

            SearchQueue.insertTimeListener(new TimeListener() {
                    public void timeAdvanced() {
                        EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    clock.setText(String.valueOf(SearchQueue
                                        .getTime()));
                                }
                            });
                    }
                });
            clock.setEditable(false);
            constraints.weightx = 0;
            layout.setConstraints(clock, constraints);
            panel.add(clock);

            return panel;
        } else {
            return statusLine;
        }
    }

    public void drawingAdded(Drawing drawing) {
        if (drawing instanceof CPNDrawing) {
            // make sure the drawing loader has been instantiated.
            ModeReplacement.getInstance().getDrawingLoader();
            logger.debug("drawing loaded: " + drawing.getName());
            ModeReplacement.getInstance().getDrawingLoader()
                           .addDrawing((CPNDrawing) drawing);
        }
        if (drawing instanceof CPNInstanceDrawing
                    || drawing instanceof TokenBagDrawing) {
            noUndoHistoryFor(drawing);
        }
    }

    public void drawingReleased(Drawing drawing) {
        if (drawing instanceof CPNDrawing) {
            ModeReplacement.getInstance().getDrawingLoader()
                           .releaseDrawing((CPNDrawing) drawing);
        }
    }

    boolean syntaxCheck() {
        if (ModeReplacement.getInstance().getSimulation().isSimulationActive()) {
            showStatus("Terminate the current simulation "
                       + "before a syntax check.");
        } else {
            try {
                ModeReplacement.getInstance().getSimulation().syntaxCheckOnly();
                showStatus("Syntax check successful.");
                GuiPlugin.getCurrent().closeSyntaxErrorFrame();
                return true;
            } catch (SyntaxException e) {
                logger.debug(e.getMessage(), e);
                GuiPlugin.getCurrent()
                         .processSyntaxException(FigureExceptionFactory
                    .createFigureException(e), true);
            }
        }
        return false;
    }

    public JFrame getFrame() {
        return super.getFrame();
    }

    //    /*
    //     * we have to redeclare this method here to make it available in this package
    //     * @see CH.ifa.draw.application.DrawApplication#getLoadPath(java.io.File, javax.swing.filechooser.FileFilter)
    //     */
    //	protected File getLoadPath(File filename, FileFilter ff) {
    //		return super.getLoadPath(filename, ff);
    //	}


    /**
     * Opens an instance drawing for a given net instance accessor.
     * @param instance The net instance accessor.
     */
    public void openInstanceDrawing(NetInstanceAccessor instance) {
        assert EventQueue.isDispatchThread() : "Must be called within AWT event thread.";
        try {
            CPNInstanceDrawing instDraw = CPNInstanceDrawing.getInstanceDrawing(instance);

            // If the instance drawing could not be created, don't open a window...
            if (instDraw != null) {
                boolean newWindow = getViewContainer(instDraw) == null;

                // new:
                // position instance net drawing to where net drawing is located.
                // Should provide an explicit getName() for Net.
                CPNDrawing drawing = ModeReplacement.getInstance()
                                                    .getDrawingLoader()
                                                    .getDrawing(instance.getNet()
                                                                        .asString());
                DrawingViewContainer netContainer = getViewContainer(drawing);
                DrawingViewContainer viewContainer = showDrawingViewContainer(instDraw,
                                                                              netContainer
                                                                              .getLocation(),
                                                                              netContainer
                                                                              .getSize());

                if (newWindow) {
                    viewContainer.onDiscardRelease();
                    viewContainer.view().setBackground(INSTANCE_COLOR);
                    viewContainer.validate();
                }
            }
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                                          "A problem occurred: " + e + "\n"
                                          + "See the console for details.",
                                          "Renew", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openTokenBagDrawing(PlaceInstanceAccessor pi)
            throws RemoteException {
        //RenewMode mode = getMode();
        TokenBagDrawing tokenDraw = TokenBagDrawing.getTokenBagDrawing(pi);
        boolean newWindow = getViewContainer(tokenDraw) == null;

        CPNInstanceDrawing instDraw = CPNInstanceDrawing.getInstanceDrawing( //mode, 
        pi.getNetInstance());
        DrawingViewContainer netContainer = getViewContainer(instDraw);
        DrawingViewContainer viewContainer = showDrawingViewContainer(tokenDraw,
                                                                      netContainer
                                                                      .getLocation(),
                                                                      null);

        if (newWindow) {
            viewContainer.onDiscardRelease();
            viewContainer.view().setBackground(TOKEN_BAG_COLOR);
        }
    }

    /*
     * TODO: isnt there a SNS import filter now?
     * if so, this method isnt neccessary;
     * if not, either code one or make the IOHelper (which owns this method now)
     * extendable
     */


    //    public synchronized void loadAndOpenDrawing(URL location) {
    //        if ("sns".equals(StringUtil.getExtension(location.getFile()))) {
    //            importShadow(location);
    //        } else {
    //            super.loadAndOpenDrawing(location);
    //        }
    //    }


    /**
     * Closes the specified drawing.
     * Checks if instance drawings depend on the drawing to be
     * closed and asks the user, if all dependent drawings should
     * be closed, too.
     *
     * @return <code>true</code>,  if the drawing was closed
     * - <code>false</code>, if the operation was aborted
     */
    protected boolean closeViewContainer(DrawingViewContainer viewContainer) {
        Drawing drawing = viewContainer.view().drawing();

        if (drawing instanceof CPNDrawing) {
            Enumeration<CPNInstanceDrawing> dependentDrawings = CPNInstanceDrawing
                                                                .getDependentInstanceDrawings((CPNDrawing) drawing);

            if (dependentDrawings != null) {
                showDrawingViewContainer(drawing);

                int answer = JOptionPane.showConfirmDialog(menuFrame,
                                                           new String[] { "The drawing \""
                                                           + drawing.getName()
                                                           + "\" "
                                                           + "you are about to close", "is needed to display one "
                                                           + "or more instance drawings." },
                                                           "Renew: Confirm Close",
                                                           JOptionPane.OK_CANCEL_OPTION);

                if (answer == JOptionPane.CANCEL_OPTION) {
                    return false;
                }
                while (dependentDrawings.hasMoreElements()) {
                    closeDrawing(dependentDrawings.nextElement());
                }
            }
        }
        return super.closeViewContainer(viewContainer);
    }

    /**
     * {@inheritDoc}
     * <p>
     * There are two different ways of termination:
     * <ul>
     * <li>If the property <code>de.renew.gui.shutdownOnClose</code>
     *     is set, the responsibility for closing the application is
     *     forwarded to the {@link PluginManager}. </li>
     * <li>If the property is not set (or set to <code>false</code>),
     *     the superclass can do its normal termination job.</li>
     * </ul></p>
     * @see DrawApplication#requestClose()
     **/
    protected void requestClose() {
        boolean shutdownPluginSystem = false;
        if (fPlugin != null) {
            shutdownPluginSystem = fPlugin.isShutdownOnClose();
        }
        if (shutdownPluginSystem) {
            showStatus("Shutting down plugin system...");
            fPlugin.doShutdownOnClose();
        } else {
            showStatus("Closing Renew Gui...");
            super.requestClose();
        }
    }

    /**
     * Exits the application.
     * (DrawApplication asks to never override this method,
     * but this method calls its super class.
     * It just closes all simulation windows before.)
     */
    public void exit() {
        BindingSelectionFrame.close();
        closeAllSimulationDrawings();
        super.exit();
    }

    /**
     * Informs the plugin system about gui termination.
     **/
    protected void destroy() {
        if (fPlugin != null) {
            fPlugin.notifyGuiClosed(this);
            fPlugin = null;
        }
    }

    public boolean canClose() {
        // Ask for simulation termination only if it's not the system
        // shutdown anyway. And if there is a running simulation.
        if (!PluginManager.getInstance().isStopping()
                    && ModeReplacement.getInstance().getSimulation()
                                              .isSimulationActive()) {
            int answer = JOptionPane.showConfirmDialog(menuFrame,
                                                       "Renew: "
                                                       + "The simulation engine is still active."
                                                       + "\nIt can continue running without graphical feedback."
                                                       + "\n Do you want to terminate it now?");
            switch (answer) {
            case 0:
                // user told us to terminate the simulation.
                ModeReplacement.getInstance().getSimulation()
                               .simulationTerminate();
                break;
            case 1:
                // user said "no", so simulation may continue.
                break;
            case 2:
                // user said "cancel", so shutdown is not ok
                return false;
            }
        }
        return super.canClose();
    }

    public ToolButton toolButtonForTextFigure(TextFigure figure) {
        if (figure instanceof CPNTextFigure) {
            if (figure instanceof DeclarationFigure) {
                return fDeclTB;
            }
            switch (((CPNTextFigure) figure).getType()) {
            case CPNTextFigure.AUX:
                if (fAuxTB != null) {
                    return fAuxTB;
                } else {
                    return fInscrTB;
                }
            case CPNTextFigure.INSCRIPTION:
                return fInscrTB;
            case CPNTextFigure.NAME:
                return fNameTB;
            case CPNTextFigure.COMM:
                return fCommTB;
            default: {
            }
            }
        }
        return super.toolButtonForTextFigure(figure);
    }

    protected void createTools(JPanel toolPanel) {
        super.createTools(toolPanel);

        Palette palette = new Palette("Petri Net Tools");

        Tool tool = null;

        tool = new TransitionFigureCreationTool(this);
        palette.add(createToolButton(CPNIMAGES + "TRANS", "Transition Tool",
                                     tool));

        tool = new PlaceFigureCreationTool(this);


        palette.add(createToolButton(CPNIMAGES + "PLACE", "Place Tool", tool));

        tool = new VirtualPlaceFigureCreationTool(this);
        palette.add(createToolButton(CPNIMAGES + "VPLACE",
                                     "Virtual Place Tool", tool));

        tool = new ConnectionTool(this, ArcConnection.NormalArc);
        palette.add(createToolButton(CPNIMAGES + "ARC", "Arc Tool", tool));

        tool = new ConnectionTool(this, ArcConnection.TestArc);
        palette.add(createToolButton(IMAGES + "LINE", "Test Arc Tool", tool));

        tool = new ConnectionTool(this, ArcConnection.ReserveArc);
        palette.add(createToolButton(IMAGES + "CONN", "Reserve Arc Tool", tool));

        tool = new ConnectionTool(this, DoubleArcConnection.DoubleArc);
        palette.add(createToolButton(CPNIMAGES + "DARC", "Flexible Arc Tool",
                                     tool));

        tool = new CPNTextTool(this, CPNTextFigure.Inscription);


        // store the inscription text tool button
        // for immediate editing of errors:
        fInscrTB = createToolButton(CPNIMAGES + "INSCR", "Inscription Tool",
                                    tool);
        palette.add(fInscrTB);

        tool = new ConnectedTextTool(this, CPNTextFigure.Name);
        fNameTB = createToolButton(CPNIMAGES + "NAME", "Name Tool", tool);
        palette.add(fNameTB);

        tool = new CPNTextTool(this, new DeclarationFigure(), false);
        fDeclTB = createToolButton(CPNIMAGES + "DECL", "Declaration Tool", tool);
        palette.add(fDeclTB);

        tool = new CPNTextTool(this, CPNTextFigure.Comm);
        fCommTB = createToolButton(CPNIMAGES + "COMM", "Comment Tool", tool);
        palette.add(fCommTB);

        toolPanel.add(palette.getComponent());
    }

    /**
     * Creates the selection tool used in this editor.
     */
    protected Tool createSelectionTool() {
        return new CPNSelectionTool(this);
    }

    /**
     * Checks the items of all menus if they are are
     * selection sensitive and updates them.
     */
    public void menuStateChanged() {
        JMenuBar mb = menuFrame.getJMenuBar();
        int count = mb.getMenuCount();

        for (int i = 0; i < count; i++) {
            JMenu menu = (mb.getMenu(i));

            if (menu instanceof CommandMenu) {
                ((CommandMenu) menu).checkEnabled();
                ((CommandMenu) menu).updateCommandText();
            }
        }
    }

    // -- main -----------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("The CPNApplication class does no longer provide a stand-alone application.");
        System.out.println("Please run de.renew.plugin.PluginManager with the command \"gui\".");
        System.out.println("A simple command line is:");
        System.out.println("       java -jar loader.jar gui [filenames]");
    }
}