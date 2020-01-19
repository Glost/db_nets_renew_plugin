/*
 * Created on 28.01.2004
 *
 */
package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureWithID;

import CH.ifa.draw.standard.FigureException;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenuItem;

import de.renew.application.IllegalCompilerException;
import de.renew.application.NoSimulationException;
import de.renew.application.SimulationEnvironment;
import de.renew.application.SimulationRunningException;
import de.renew.application.SimulatorExtensionAdapter;
import de.renew.application.SimulatorPlugin;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.simulator.SimulationThreadPool;
import de.renew.engine.simulator.Simulator;

import de.renew.io.SimulationStateFileFilter;

import de.renew.net.IDRegistry;
import de.renew.net.Net;
import de.renew.net.NetInstance;
import de.renew.net.NetNotFoundException;
import de.renew.net.Place;
import de.renew.net.PlaceInstance;
import de.renew.net.Transition;
import de.renew.net.TransitionInstance;

import de.renew.remote.NetAccessor;
import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.PlaceInstanceAccessor;
import de.renew.remote.RemotePlugin;
import de.renew.remote.TransitionAccessor;
import de.renew.remote.TransitionInstanceAccessor;
import de.renew.remote.TransitionInstanceAccessorImpl;

import de.renew.shadow.SyntaxException;

import de.renew.util.RenewObjectInputStream;
import de.renew.util.RenewObjectOutputStream;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

import java.rmi.RemoteException;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;


/**
 * This class creates the Simulation menu for the Gui Plugin.<br>
 * It contains entries for running and controlling a simulation, saving/loading
 * a simulation to a database, connect to a remotely running simulation and lets
 * you choose the formalism for the present simulation (since 2.0).
 *
 * @author J&ouml;rn Schumacher
 */
public class SimulationMenuCreator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SimulationMenuCreator.class);

    /**
     * Used to synchronise access to some method calls that start threads. Dont
     * use synchronized for these methods use lock instead.
     */
    static private final Lock lock = new ReentrantLock();

    public Collection<JMenuItem> createMenus(BreakpointManager bpm) {
        SeparatorFactory sepFac = new SeparatorFactory("de.renew.gui.simulation");
        Vector<JMenuItem> result = new Vector<JMenuItem>();

        result.add(DrawApplication.createMenuItem("Run simulation",
                                                  KeyEvent.VK_R,
                                                  new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    lock.lock();
                    GuiPlugin plugin = GuiPlugin.getCurrent();
                    if (!ModeReplacement.getInstance().getSimulation()
                                                .isSimulationActive()) {
                        // Be notified if the simulation is setup and start
                        // running
                        ModeReplacement.getInstance().getSimulation()
                                       .getSimulatorPlugin().addExtension(new SimulatorExtensionAdapter() {
                                public void simulationSetup(SimulationEnvironment env) {
                                    ModeReplacement.getInstance().getSimulation()
                                                   .simulationRun();
                                    ModeReplacement.getInstance().getSimulation()
                                                   .getSimulatorPlugin()
                                                   .removeExtension(this);
                                }
                            });

                        initSimulation();
                    } else {
                        plugin.showStatus("Simulation running.");
                        ModeReplacement.getInstance().getSimulation()
                                       .simulationRun();
                    }
                    lock.unlock();
                }
            }));

        result.add(DrawApplication.createMenuItem("Simulation Step",
                                                  KeyEvent.VK_I,
                                                  new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    SimulationThreadPool.getCurrent().execute(new Thread() {
                            public void run() {
                                GuiPlugin plugin = GuiPlugin.getCurrent();
                                if (!ModeReplacement.getInstance()
                                                            .getSimulation()
                                                            .isSimulationActive()) {
                                    initSimulation();
                                } else {
                                    plugin.showStatus("Searching for a binding...");
                                    int status = ModeReplacement.getInstance()
                                                                .getSimulation()
                                                                .simulationStep();

                                    switch (status) {
                                    case Simulator.statusStopped:
                                        if (ModeReplacement.getInstance()
                                                                   .getSimulation()
                                                                   .isSimulationActive()) {
                                            plugin.showStatus("Simulation halted.");
                                        } else {
                                            plugin.showStatus("Simulation terminated.");
                                        }
                                        break;
                                    case Simulator.statusStepComplete:
                                        plugin.showStatus("Simulation step completed.");
                                        break;
                                    case Simulator.statusLastComplete:
                                        plugin.showStatus("Simulation step completed. "
                                                          + "No more enabled bindings.");
                                        break;
                                    case Simulator.statusCurrentlyDisabled:
                                        plugin.showStatus("No enabled bindings found.");
                                        break;
                                    case Simulator.statusDisabled:
                                        plugin.showStatus("No more enabled bindings.");
                                        break;
                                    }
                                }
                            }
                        });
                }
            }));

        result.add(new CommandMenuItem(new Command("Simulation Net Step") {
                public void execute() {
                    SimulationThreadPool.getCurrent().execute(new Thread() {
                            private HitListener listener;

                            public void run() {
                                GuiPlugin plugin = GuiPlugin.getCurrent();
                                if (!ModeReplacement.getInstance()
                                                            .getSimulation()
                                                            .isSimulationActive()) {
                                    initSimulation();
                                } else {
                                    BreakpointManager bpm = ModeReplacement.getInstance()
                                                                           .getSimulation()
                                                                           .getBreakpointManager();

                                    // add Breakpoint.FIRECOMPLETE to each
                                    // transition in the
                                    // current selected net instance
                                    DrawingView view = DrawPlugin.getCurrent()
                                                                 .getDrawingEditor()
                                                                 .view();
                                    Drawing drawing = view.drawing();
                                    if (drawing instanceof CPNInstanceDrawing) {
                                        FigureEnumeration figenumeration = drawing
                                                                           .figures();

                                        Vector<Breakpoint> addedBPs = new Vector<Breakpoint>();
                                        while (figenumeration.hasMoreElements()) {
                                            Object o = figenumeration
                                                .nextElement();
                                            if (o instanceof TransitionInstanceFigure) {
                                                TransitionInstanceFigure figure = (TransitionInstanceFigure) o;
                                                TransitionInstanceAccessor accessor = figure
                                                                                      .getInstance();
                                                if (!(accessor instanceof TransitionInstanceAccessorImpl)) {
                                                    GuiPlugin.getCurrent()
                                                             .showStatus("The net step feature is only enabled for local simulations.");
                                                    return;
                                                }

                                                TransitionInstance instance = ((TransitionInstanceAccessorImpl) accessor)
                                                                              .getTransitionInstance();
                                                addedBPs.add(bpm
                                                    .createTransitionInstanceBreakpoint(instance,
                                                                                        Breakpoint.FIRECOMPLETE));
                                            }
                                        }

                                        listener = new HitListener(addedBPs);
                                        bpm.addBreakpointHitListener(listener);

                                        // run simulation until a breakpoint
                                        // occurs
                                        plugin.showStatus("waiting for next transition firing in this net instance...");
                                        ModeReplacement.getInstance()
                                                       .getSimulation()
                                                       .simulationRun();
                                    } else {
                                        GuiPlugin.getCurrent()
                                                 .showStatus("A net instance drawing must be selected.");
                                    }
                                }
                            }

                            class HitListener implements BreakpointHitListener {
                                private Vector<Breakpoint> bps;

                                protected HitListener(Vector<Breakpoint> bps) {
                                    this.bps = bps;
                                }

                                public void hitBreakpoint(BreakpointHitEvent event) {
                                    if (bps.contains(event.getBp())) {
                                        event.consume();

                                        if (ModeReplacement.getInstance()
                                                                   .getSimulation()
                                                                   .isSimulationActive()) {
                                            GuiPlugin.getCurrent()
                                                     .showStatus("Simulation halted.");
                                        } else {
                                            GuiPlugin.getCurrent()
                                                     .showStatus("Simulation terminated.");
                                        }

                                        BreakpointManager bpm = ModeReplacement.getInstance()
                                                                               .getSimulation()
                                                                               .getBreakpointManager();


                                        // remove all added breakpoints
                                        Iterator<Breakpoint> i = bps.iterator();
                                        while (i.hasNext()) {
                                            Breakpoint bp = i.next();
                                            bpm.deleteBreakpoint(bp);
                                        }

                                        // remove this listener from
                                        // BreakpointManager
                                        bpm.removeBreakpointHitListener(listener);
                                    }
                                }
                            }
                        });
                }
            }, KeyEvent.VK_I,
                                       Toolkit.getDefaultToolkit()
                                              .getMenuShortcutKeyMask()
                                       + KeyEvent.SHIFT_DOWN_MASK));

        // workaround for the Ctrl-H Problem in Mac OSX
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if ("Mac OS X".equals(System.getProperty("os.name"))) {
            modifier += KeyEvent.SHIFT_DOWN_MASK;
        }

        result.add(new CommandMenuItem(new Command("Halt simulation") {
                public boolean isExecutable() {
                    if (!super.isExecutable()) {
                        return false;
                    }
                    return ModeReplacement.getInstance().getSimulation()
                                          .isSimulationActive();
                }

                public void execute() {
                    ModeReplacement.getInstance().getSimulation()
                                   .simulationStop();
                    GuiPlugin.getCurrent().showStatus("Simulation halted.");
                }
            }, KeyEvent.VK_H, modifier));

        result.add(new CommandMenuItem(new Command("Terminate simulation") {
                public boolean isExecutable() {
                    if (!super.isExecutable()) {
                        return false;
                    }
                    return ModeReplacement.getInstance().getSimulation()
                                          .isSimulationActive();
                }

                public void execute() {
                    ModeReplacement.getInstance().getSimulation()
                                   .simulationTerminate();
                }
            }, KeyEvent.VK_T));

        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new ConfigureSimulationCommand("Configure Simulation...")));
        result.add(bpm.getSimulationMenu());

        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new Command("Save simulation state...") {
                public boolean isExecutable() {
                    if (!super.isExecutable()) {
                        return false;
                    }
                    return ModeReplacement.getInstance().getSimulation()
                                          .isSimulationActive();
                }

                public void execute() {
                    promptSaveSimulationState();
                }
            }));

        result.add(new CommandMenuItem(new Command("Load simulation state...") {
                public void execute() {
                    promptLoadSimulationState();
                }
            }));
        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new OpenServerFrameCommand("Remote Server...")));

        return result;
    }

    /**
     * Initialize the simulation.
     */
    void initSimulation() {
        SimulatorPlugin.lock.lock();
        try {
            SimulationThreadPool.getNew().execute(new Runnable() {
                    public void run() {
                        final GuiPlugin plugin = GuiPlugin.getCurrent();
                        final CPNApplication app = plugin.getGui();
                        app.showStatus("Starting Simulation ...");
                        if (!(app.drawing() instanceof CPNDrawing)) {
                            app.showStatus("Cannot start new simulation with this drawing! Please activate a net window.");
                            return;
                        }

                        String netName = GuiPlugin.getCurrent()
                                                  .getDrawingEditor().drawing()
                                                  .getName();
                        try {
                            final NetInstanceAccessor primaryInstance = getSimulation()
                                                                            .initSimulation(netName);

                            EventQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        if (primaryInstance != null) {
                                            app.openInstanceDrawing(primaryInstance);

                                            if (getSimulation()
                                                            .isSimulationActive()) {
                                                app.showStatus("Simulation initialized.");
                                            } else {
                                                app.showStatus("Simulation initialized. "
                                                               + "No enabled bindings.");
                                            }
                                        } else {
                                            app.showStatus("The simulation could not be initialized.");
                                        }

                                        // closeSyntaxErrorFrame();
                                    }
                                });
                        } catch (final SyntaxException e) {
                            logger.debug(e.getMessage(), e);
                            EventQueue.invokeLater(new Runnable() {
                                    public void run() {
                                        plugin.processSyntaxException(FigureExceptionFactory
                                                                      .createFigureException(e),
                                                                      true);
                                    }
                                });
                        } catch (NetNotFoundException e1) {
                            logger.warn(e1.getMessage(), e1);
                            plugin.showStatus("Net \"" + netName
                                              + "\" not found although the drawing exists (should not happen).");
                        } catch (IllegalCompilerException e1) {
                            logger.warn(e1.getMessage(), e1);
                            plugin.showStatus("No compiler selected. Please choose a formalism.");
                        } catch (NoSimulationException e1) {
                            logger.debug(e1.getMessage(), e1);
                            plugin.showStatus("Simulation aborted during initialization.");
                        } catch (RuntimeException e1) {
                            logger.warn("Simulation initialization failed: "
                                        + e1.toString(), e1);
                            plugin.showStatus("Simulation initialization failed: "
                                              + e1.getMessage());
                        } catch (Error e1) {
                            logger.error("Simulation initialization failed: "
                                         + e1.toString(), e1);
                            plugin.showStatus("Simulation initialization failed: "
                                              + e1.toString());
                        }
                    }
                });
        } finally {
            SimulatorPlugin.lock.unlock();
        }
    }

    /**
     * Shows a load file dialog and replaces the current simulation state with
     * the loaded one.
     *
     * added Apr 12 2000 Michael Duvigneau
     */
    void promptLoadSimulationState() {
        GuiPlugin plugin = GuiPlugin.getCurrent();
        CPNApplication gui = plugin.getGui();
        File path = DrawPlugin.getCurrent().getIOHelper()
                              .getLoadPath(null, new SimulationStateFileFilter());

        if (path != null) {
            // Close all simulation related windows,
            // as the old simulation will be terminated.
            BindingSelectionFrame.close();
            gui.closeAllSimulationDrawings();

            String message = null;
            ObjectInput input = null;

            try {
                GuiPlugin.getCurrent().showStatus("Importing " + path + " ...");

                FileInputStream stream = new FileInputStream(path);

                // As the recursion depth seems to be too high to
                // fit in the Java VM stack space, we try to cut
                // it down by using de.renew.util.RenewObjectInputStream.
                // Additionally, some changes to PlaceInstance are
                // required to save the tokens delayed.
                input = new RenewObjectInputStream(stream);

                // Below the original line instantiating the classic Stream.
                // input = new ObjectInputStream(stream);
                ModeReplacement.getInstance().getSimulation().loadState(input);

                plugin.showStatus("Imported " + path + ".");
            } catch (SimulationRunningException e) {
                message = "Cannot load state into running simulation";
            } catch (ObjectStreamException e) {
                message = "File " + path + " is corrupted or does not "
                          + "contain state information.";
                logger.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                message = "File " + path + " refers to java classes "
                          + "which are currently not available.";
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                message = "Problem occured while reading file " + path + ":"
                          + e;
            } catch (StackOverflowError e) {
                message = "StackOverflowError: Probably the recursion in Java's "
                          + "serialization mechanism went too deep.\n"
                          + "If you are using JDK 1.1, try setting the java options "
                          + " -ss... and/or -oss... to increase native and java stack "
                          + "sizes. Setting -ss1m was appropriate for us.";
            }
            try {
                if (input != null) {
                    input.close();
                }
                if (message == null) {
                    message = "Simulation state restored from " + path + ".";
                }
            } catch (StreamCorruptedException e) {
                // A RenewObjectInputStream can throw this exception
                // if it is not able to read delayed fields on close().
                // If loadState() was not successfull, this is a natural
                // consequence, so ignore it.
                // The stream has then been closed anyway.
                if (message == null) {
                    message = "Problem occurred while reading file " + path
                              + ", but all relevant information was already read. "
                              + "Perhaps the simulation state is valid anyway.";
                    logger.error("Exception while closing: " + e);
                }
            } catch (Exception e) {
                if (message == null) {
                    message = "Problem occurred while reading file " + path
                              + ", but all relevant information was already read. "
                              + "Perhaps the simulation state is valid anyway.";
                }
                logger.error("Exception while closing: " + e);
            }
            plugin.showStatus(message);
            logger.error(message);
        }
    }

    /**
     * Shows a save file dialog and stores the current simulation state.
     *
     * added Jan 20 2000 Michael Duvigneau
     */
    void promptSaveSimulationState() {
        GuiPlugin plugin = GuiPlugin.getCurrent();
        if (!ModeReplacement.getInstance().getSimulation().isSimulationActive()) {
            plugin.showStatus("Need running simulation to save state.");
        } else {
            File file = DrawPlugin.getCurrent().getIOHelper()
                                  .getSavePath(null,
                                               new SimulationStateFileFilter());

            if (file != null) {
                String path = file.getAbsolutePath();
                String message = null;
                Stack<Object> problemLocation = null;
                RenewObjectOutputStream output = null;

                try {
                    plugin.showStatus("Exporting " + path + " ...");

                    // As the recursion depth seems to be too high to
                    // fit in the Java VM stack space, we try to cut
                    // it down by using de.renew.util.RenewObjectOutputStream.
                    FileOutputStream stream = new FileOutputStream(path);

                    output = new RenewObjectOutputStream(stream);

                    ModeReplacement.getInstance().getSimulation()
                                   .saveState(output);
                    plugin.showStatus("Exported " + path + ".");
                } catch (NotSerializableException e) {
                    message = "Some object or class is not serializable: "
                              + e.getMessage() + ".\nWriting of file " + path
                              + " aborted.";
                    // NOTICEnull
                    problemLocation = output != null ? output.getDomainTrace()
                                                     : null;
                    logger.debug(e.getMessage(), e);
                } catch (Exception e) {
                    message = "Problem encountered while writing file " + path
                              + ": " + e;
                    logger.debug(e.getMessage(), e);
                } catch (StackOverflowError e) {
                    message = "StackOverflowError: Probably the recursion in Java's "
                              + "serialization mechanism went too deep.\n"
                              + "If you are using JDK 1.1, try setting the java options "
                              + " -ss... and/or -oss... to increase native and java stack "
                              + "sizes. Setting -ss1m was appropriate for us.";
                    logger.debug(e.getMessage(), e);
                }
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (message == null) {
                        message = "Simulation state saved to " + path + ".";
                    }
                } catch (Exception e) {
                    if (message == null) {
                        message = "Problem encountered while "
                                  + "closing file " + path + ": " + e;
                    }
                }
                if (problemLocation != null) {
                    displaySaveSimError(problemLocation, message);
                } else {
                    plugin.showStatus(message);
                    logger.error(message);
                }
            }
        }
    }

    private void displaySaveSimError(Stack<Object> objectLocation,
                                     String message) {
        try {
            GuiPlugin plugin = GuiPlugin.getCurrent();

            NetAccessor net = null;
            NetInstanceAccessor netInstance = null;
            int id = FigureWithID.NOID;
            String thisShouldNotHappen = null;
            String specialReason = null;
            Object netElement = null;
            CPNDrawing drawing = null;
            String netName = null;
            boolean appendTrace = false;

            // Extract the element at which the problem occurred
            // from the top of the stack. Because the stack
            // contains simulation-local objects, but the GUI
            // needs remote accessors, wrap the information. If
            // some unexpected type of object is found, flag this
            // by setting the String thisShouldNotHappen to a
            // non-null value.
            try {
                netElement = objectLocation.peek();
                RemotePlugin remote = RemotePlugin.getInstance();
                if (netElement instanceof PlaceInstance) {
                    PlaceInstanceAccessor placeInst = remote.wrapInstance((PlaceInstance) netElement);
                    id = placeInst.getPlace().getID().getFigureID();
                    netInstance = placeInst.getNetInstance();
                    net = netInstance.getNet();
                } else if (netElement instanceof TransitionInstance) {
                    TransitionInstanceAccessor transitionInst = remote
                                                                    .wrapInstance((TransitionInstance) netElement);
                    id = transitionInst.getTransition().getID().getFigureID();
                    netInstance = transitionInst.getNetInstance();
                    net = netInstance.getNet();
                } else if (netElement instanceof Place) {
                    thisShouldNotHappen = "place " + netElement;
                    id = ((Place) netElement).getID().getFigureID();
                } else if (netElement instanceof TransitionAccessor) {
                    thisShouldNotHappen = "transition " + netElement;
                    id = ((Transition) netElement).getID().getFigureID();
                } else if (netElement instanceof Net) {
                    thisShouldNotHappen = "net " + netElement;
                    net = remote.wrapNet((Net) netElement);
                } else if (netElement instanceof NetInstance) {
                    thisShouldNotHappen = "net instance " + netElement;
                    netInstance = remote.wrapInstance((NetInstance) netElement);
                    net = netInstance.getNet();
                } else if (netElement instanceof IDRegistry) {
                    thisShouldNotHappen = "a token ID registry";
                } else if (netElement == SimulatorPlugin.class) {
                    thisShouldNotHappen = "instance drawings";
                } else if (netElement == Net.class) {
                    thisShouldNotHappen = "compiled nets";
                } else if (netElement == SearchQueue.class) {
                    thisShouldNotHappen = "search queue entries";
                    if (message.indexOf("de.renew.call.SynchronisationRequest") >= 0) {
                        specialReason = "You tried to save the simulation state "
                                        + "while a Java call to a net method was still "
                                        + "active. Wait until all methods have completed.";
                    }
                } else {
                    thisShouldNotHappen = "(DON'T KNOW - TRACE CONTAINS OBJECT OF UNEXPECTED TYPE: "
                                          + netElement.getClass().getName()
                                          + ")";
                }
            } catch (EmptyStackException e) {
                thisShouldNotHappen = "(DON'T KNOW - TRACE IS EMPTY!?)";
            } catch (NoSimulationException e) {
                thisShouldNotHappen = "(DON'T KNOW - NO LOCAL SIMULATION RU)";
            }

            // Compose the message. This depends on whether an
            // expected element was found on top of the stack
            // trace.
            if (thisShouldNotHappen == null) {
                // NOTICEnull
                netName = net != null ? net.getName() : null;

                message = message + "\nThe problem lies around net element "
                          + netElement + ".";
                drawing = ModeReplacement.getInstance().getDrawingLoader()
                                         .getDrawing(netName);
                appendTrace = true;
            } else {
                if (specialReason == null) {
                    message = message
                              + "\nDid you try to save a state while the "
                              + "simulation was running (concurrent transition "
                              + "firing or binding calculation is not allowed)?";
                } else {
                    message = message + "\n" + specialReason;
                }
                message = message
                          + ("\nThe problem manifested itself while writing "
                            + thisShouldNotHappen + ".");
                appendTrace = true;
            }

            // If appropriate, append a stack trace to the message.
            if (appendTrace) {
                message = message + "\nSerialization trace:";
                try {
                    while (!objectLocation.isEmpty()) {
                        netElement = objectLocation.pop();
                        message = message + ("\n    " + netElement);

                        // Take a guess, to which net the offending figure
                        // belongs.
                        // With chance, the net is listed in the trace.
                        if (net == null && netInstance == null) {
                            try {
                                RemotePlugin remote = RemotePlugin.getInstance();
                                if (netElement instanceof Net) {
                                    net = remote.wrapNet((Net) netElement);
                                } else if (netElement instanceof NetInstance) {
                                    netInstance = remote.wrapInstance((NetInstance) netElement);
                                    net = netInstance.getNet();
                                }
                            } catch (NoSimulationException e) {
                            }
                        }
                    }
                } catch (EmptyStackException e) {
                }
            }

            // Display the message and point the user to the
            // problem location. This again depends on whether an
            // expected element was found on top of the stack
            // trace.
            if (thisShouldNotHappen == null) {
                if (drawing == null) {
                    plugin.showStatus(message);
                    logger.error(message);
                    logger.error("Sorry, cannot show the location of the problem "
                                 + "because net " + netName + " is not loaded.");
                } else {
                    GuiPlugin.getCurrent().getGui()
                             .openInstanceDrawing(netInstance);

                    CPNInstanceDrawing instanceDrawing = CPNInstanceDrawing
                                                             .getInstanceDrawing(netInstance);
                    FigureWithID figure = drawing.getFigureWithID(id);
                    InstanceFigure instanceFigure = instanceDrawing
                                                        .getInstanceFigure(figure);

                    plugin.showStatus(message);
                    logger.error(message);
                    GuiPlugin.getCurrent()
                             .processFigureException(new FigureException("Renew: Save simulation error",
                                                                         message,
                                                                         instanceDrawing,
                                                                         instanceFigure),
                                                     false);
                }
            } else {
                plugin.showStatus(message);
                logger.error(message);
                if ((net != null) && (id != FigureWithID.NOID)) {
                    drawing = ModeReplacement.getInstance().getDrawingLoader()
                                             .getDrawing(net.getName());

                    if (drawing != null) {
                        FigureWithID figure = drawing.getFigureWithID(id);

                        if (figure != null) {
                            if (netInstance != null) {
                                try {
                                    CPNInstanceDrawing instanceDrawing = CPNInstanceDrawing
                                                                         .getInstanceDrawing( // getMode(),
                                    netInstance);

                                    // NOTICEredundant
                                    plugin.processFigureException(new FigureException("Renew: Save simulation error",
                                                                                      message,
                                                                                      instanceDrawing,
                                                                                      figure),
                                                                  false);
                                } catch (RemoteException e) {
                                    logger.error(e.getMessage(), e);
                                    JOptionPane.showMessageDialog(null,
                                                                  "A problem occurred: "
                                                                  + e
                                                                  + "\nSee the console for details.",
                                                                  "Renew",
                                                                  JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                plugin.processFigureException(new FigureException("Renew: Save simulation error",
                                                                                  message,
                                                                                  drawing,
                                                                                  figure),
                                                              false);
                            }
                        }
                    }
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

    protected CPNSimulation getSimulation() {
        return ModeReplacement.getInstance().getSimulation();
    }
}