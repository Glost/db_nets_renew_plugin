package de.renew.gui;

import CH.ifa.draw.util.AWTSynchronizedUpdate;
import CH.ifa.draw.util.Fontkit;

import de.renew.engine.searchqueue.SearchQueue;
import de.renew.engine.searchqueue.TimeListener;
import de.renew.engine.simulator.SimulationThreadPool;

import de.renew.remote.BindingAccessor;
import de.renew.remote.RemoteTriggerable;
import de.renew.remote.TransitionInstanceAccessor;
import de.renew.remote.TriggerableForwarder;
import de.renew.remote.TriggerableForwarderImpl;

import de.renew.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.rmi.RemoteException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


class BindingSelectionFrame implements ListSelectionListener, TimeListener,
                                       RemoteTriggerable {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(BindingSelectionFrame.class);

    // Accesses to this field should use class synchronisation.
    private static BindingSelectionFrame instance = null;
    private JFrame frame;
    private JTextArea currentElement;
    private JList bindingList;
    private DefaultListModel bindingListModel;
    private JButton fireButton;
    private JButton updateButton;
    private JButton closeButton;
    private BindingAccessor[] bindings;
    private String[] fullBindingText;
    private TransitionInstanceAccessor transitionInstance;
    private CPNSimulation simulation;
    private TriggerableForwarder triggerableForwarder;

    //    private RenewMode mode;
    private boolean dirty = false;
    private boolean inSearch = false;
    private boolean wantClose = false;
    private Object updateSynchronizer = new Object();

    // indicates wheter the frame is wanted to be open. 
    // if not, the internal update shall not open it again!
    private static boolean frameOpened = false;

    private BindingSelectionFrame() {
        frame = new JFrame();

        currentElement = new JTextArea(5, 20);
        currentElement.setEditable(false);
        currentElement.setFont(Fontkit.getFont("Monospaced", Font.PLAIN, 11));
        JScrollPane currentElementPane = new JScrollPane(currentElement);
        currentElementPane.setMinimumSize(new Dimension(150, 50));
        currentElementPane.setPreferredSize(new Dimension(250, 100));

        bindingListModel = new DefaultListModel();
        bindingList = new JList(bindingListModel);
        bindingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bindingList.addListSelectionListener(this);
        bindingList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        int i = bindingList.getSelectedIndex();
                        fireBinding(i);
                    }
                }
            });
        bindingList.setVisibleRowCount(10);
        JScrollPane bindingListPane = new JScrollPane(bindingList);
        bindingListPane.setMinimumSize(new Dimension(100, 50));
        bindingListPane.setPreferredSize(new Dimension(150, 100));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              bindingListPane,
                                              currentElementPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.1);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        /*
         * Define the buttons and their ActionListeners
         */
        fireButton = new JButton(" Fire ");
        fireButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = bindingList.getSelectedIndex();
                    fireBinding(index);
                }
            });

        updateButton = new JButton(" Update ");
        updateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });

        closeButton = new JButton(" Close ");
        closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(fireButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(closeButton);
        frame.getContentPane().add(buttonPanel, "South");
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });
        frame.pack();


        // The binding frame will remain a listener forever.
        // It just does not evaluate the notifications, if it is closed.
        SearchQueue.insertTimeListener(this);

        try {
            triggerableForwarder = new TriggerableForwarderImpl(this);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                                          "A problem occurred: " + e + "\n"
                                          + "See the console for details.",
                                          "Binding selection frame",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    static synchronized void open(TransitionInstanceAccessor transitionInstance,
                                  CPNSimulation simulation) {
        if (instance == null) {
            instance = new BindingSelectionFrame();
        }

        frameOpened = true;
        instance.setup(transitionInstance, simulation);
    }

    static synchronized void close() {
        if (instance != null) {
            instance.instanceClose();
        }
    }

    private synchronized void instanceClose() {
        if (inSearch) {
            wantClose = true;
        } else {
            closeNow();
        }
    }

    private void closeNow() {
        // The forwarder can still be useful when the
        // BindingSelectionFrame instance is reopened.
        // But we have to deregister at the transition
        // instance accessor.
        assert triggerableForwarder != null : "Triggerable forwarder should have the same lifespan as the frame.";
        if (transitionInstance != null) {
            try {
                transitionInstance.forgetBindings(triggerableForwarder);
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(frame,
                                              "Could not disconnect from transition instance:\n"
                                              + e + "\n"
                                              + "See the console for details.",
                                              "Binding selection frame",
                                              JOptionPane.WARNING_MESSAGE);
            }
        }
        frame.setVisible(false);
        frameOpened = false;
        bindingListModel.clear();
        transitionInstance = null;
        wantClose = false;
    }

    public void valueChanged(ListSelectionEvent e) {
        String text = "";

        int index = bindingList.getSelectedIndex();
        if (index >= 0 && index < bindings.length) {
            text = fullBindingText[index];
        }
        currentElement.setText(text);
        currentElement.setCaretPosition(0);
    }

    /**
     * Called when the user clicks on the fireButton.
     * It fires the binding with the index (the selected one)
     * and updates the binding list.
     *
     * @param index - the index in bindingList to fire
     */
    private void fireBinding(int index) {
        if (index >= 0 && index < bindings.length) {
            simulation.getBreakpointManager().clearLog();
            try {
                bindings[index].execute(!simulation.isStrictlySequential());
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(frame,
                                              "A problem occurred: " + e + "\n"
                                              + "See the console for details.",
                                              "Binding selection frame",
                                              JOptionPane.ERROR_MESSAGE);
            }
            simulation.simulationRefresh();
            update();
        } else {
            fireButton.setEnabled(false);
        }
    }

    private void setup(TransitionInstanceAccessor transitionInstance,
                       CPNSimulation simulation) {
        this.transitionInstance = transitionInstance;
        wantClose = false;
        this.simulation = simulation;

        try {
            frame.setTitle(transitionInstance.asString()
                           + "'s possible bindings");
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(frame,
                                          "A problem occurred: " + e + "\n"
                                          + "See the console for details.",
                                          "Binding selection frame",
                                          JOptionPane.ERROR_MESSAGE);
        }

        update();
    }

    /**
     * This event is issued whenever the corresponding TransitionInstance
     * may have become enabled or disabled. The triggerable has to check
     * enabledness itself.
     */
    public void proposeSearch() {
        update();
    }

    /**
     * This event is issued whenever the global simulation time
     * changes. The triggerable has to check enabledness, because
     * it only keeps track of currently activated bindings.
     */
    public void timeAdvanced() {
        update();
    }

    /**
     * Updates the binding list thread-safely.
     * This is done in a separate thread, because of
     * a deadlock problem when executing synchronously.
     */
    public void update() {
        new AWTSynchronizedUpdate(new BindingListUpdater()).scheduleUpdate();
//        SimulationThreadPool.getCurrent().execute(new BindingListUpdater());
        //new BindingListUpdater().start();
    }

    private void setActiveBindings(BindingAccessor[] localBindings,
                                   String[] fullBindings, String[] shortBindings) {
        bindings = localBindings;
        fullBindingText = fullBindings;
        bindingListModel.clear();
        for (int i = 0; i < shortBindings.length; ++i) {
            bindingListModel.addElement(shortBindings[i]);
        }


        String text = "";

        if (bindings.length > 0) {
            bindingList.setSelectedIndex(0);
            text = fullBindingText[0];
        }
        currentElement.setText(text);
        currentElement.setCaretPosition(0);

        fireButton.setEnabled(bindings.length > 0);


    }

    /**
     * Updates the binding list thread-safely.
     */
    private class BindingListUpdater extends Thread {

        /**
         * Creates a new updater.
         */
        private BindingListUpdater() {
        }

        /**
         * Updates the binding list thread-safely.
         * This method is encapsulated in a separate thread, because of
         * a deadlock problem when executing synchronously.
         */
        public void run() {
            synchronized (updateSynchronizer) {
                if (inSearch) {
                    // In a different thread there is a search prozess running.
                    // Notify it about possible new bindings and forget about it.
                    dirty = true;
                    return;
                } else if (wantClose) {
                    closeNow();
                    return;
                } else {
                    // Request the start of a new search process.
                    dirty = false;
                    inSearch = true;
                }
            }


            // When I reach this point, I have acquired the inSearch flag.
            // I am supposed to do updates until the dirty flag remains
            // false.
            while (true) {
                // Was the window already closed?
                if (transitionInstance != null) {
                    update();
                }
                synchronized (updateSynchronizer) {
                    if (wantClose) {
                        // Close the frame and make sure that the search terminates.
                        closeNow();
                        dirty = false;
                    }
                    if (!dirty) {
                        // No more requests. Free the inSearch lock and
                        // finally exit from this method.
                        inSearch = false;
                        return;
                    } else {
                        // Start another round.
                        dirty = false;
                    }
                }
            }
        }

        /**
         * Does the AWT operations themselves.
         */
        public void update() {
            final BindingAccessor[] localBindings;
            try {
                localBindings = transitionInstance.findAllBindings(triggerableForwarder);
            } catch (RemoteException e) {
                logger.error(e.getMessage(), e);
                //Return if we cant find any bindings
                return;
            }
            final String[] fullBinding = new String[localBindings.length];
            final String[] shortBinding = new String[localBindings.length];
            for (int i = 0; i < localBindings.length; ++i) {
                try {
                    fullBinding[i] = localBindings[i].getDescription();
                } catch (RemoteException e) {
                    logger.error(e.getMessage(), e);
                    fullBinding[i] = e.toString();
                }

                shortBinding[i] = StringUtil.unspace(fullBinding[i]);
                if (shortBinding[i].length() > 50) {
                    shortBinding[i] = shortBinding[i].substring(0, 47) + "...";
                }
            }
            EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        setActiveBindings(localBindings, fullBinding,
                                          shortBinding);
                        if (frameOpened) {
                            frame.setVisible(true);
                        }
                    }
                });
        }
    }
}