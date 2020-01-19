/*
 * Created on 19.08.2004
 */
package de.renew.gui.logging;

import CH.ifa.draw.framework.DrawingView;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureWithID;

import de.renew.application.SimulatorPlugin;

import de.renew.engine.events.NetEvent;
import de.renew.engine.events.PlaceEvent;
import de.renew.engine.events.SimulationEvent;
import de.renew.engine.events.TransitionEvent;

import de.renew.gui.CPNApplication;
import de.renew.gui.CPNInstanceDrawing;
import de.renew.gui.GuiPlugin;

import de.renew.net.NetElementID;

import de.renew.remote.NetInstanceAccessor;
import de.renew.remote.PlaceInstanceAccessor;
import de.renew.remote.RemotePlugin;
import de.renew.remote.TransitionInstanceAccessor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


/**
 * A table model for one defined logger. It presents a single column, whose
 * cells are again {@link JTable}s presenting {@link StepTableModel}s.
 * <p>
 * The name of the logger to present is configured at construction time. The
 * table model automatically connects itself to the respective
 * {@link LoggerRepository}.  The connection is decoupled using a
 * {@link RepositoryChangeBuffer} instance.
 * </p>
 * <p>
 * The cells in this table are flagged as editable although they do not really
 * react to modification requests. The editable flag is necessary to allow a
 * popup menu for step trace entries to show up.
 * </p>
 *
 * @author Sven Offermann (code)
 * @author Michael Duvigneau (documentation)
 */
public class LoggerTableModel extends TableModel
        implements RepositoryChangeListener {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                          .getLogger(LoggerTableModel.class);

    /**
     * whether to immediately integrate and propagate change notifications
     */
    private boolean permanentUpdate = true;

    /**
     * stores references to listeners observing this table model
     */
    private Set<RepositoryChangeListener> listeners = new HashSet<RepositoryChangeListener>();

    /**
     * the StepTraceRepository represented by this table model.
     */
    private StepTraceRepository repository;

    /**
     * stores the stepTraces in the same order like in the Repository. If a
     * stepTrace is removed from the appender, this information is used to
     * determine the row index of the removed StepTrace.
     */
    private List<StepTrace> traces = new ArrayList<StepTrace>();

    /**
     * the buffer that merges repository events and synchronizes this table
     * model with the AWT event queue.
     */
    private RepositoryChangeBuffer eventBuffer = new RepositoryChangeBuffer();

    /**
     * keeps references to visible step table models so that we can disconnect
     * them on removal.
     **/
    private Map<StepTrace, StepTableModel> stepModels = new HashMap<StepTrace, StepTableModel>();

    /**
     * Creates a new table model to display the logged messages of the given
     * logger name. The table model automatically registers at the respective
     * {@link StepTraceRepository} as change listener and initially retrieves
     * all current entries.
     *
     * @param loggerName the GuiAppender represented by this table model
     */
    public LoggerTableModel(String loggerName) {
        super(true);

        this.repository = MainRepositoryManager.getInstance()
                                               .getCurrentRepository(loggerName);
        this.eventBuffer.addStepTraceChangeListener(this);

        if (repository != null) {
            repository.addRepositoryChangeListener(eventBuffer);

            StepTrace[] stepTraces = repository.getAllStepTraces();
            for (int x = 0; x < stepTraces.length; x++) {
                addStepTrace(stepTraces[x]);
            }
        }
    }

    /**
     * Configure whether this table model should keep itself permanently
     * up-to-date. If <code>true</code>, changes received via the
     * {@link RepositoryChangeListener} interface will be integrated in the
     * model data and propagated to listeners. If <code>false</code>, such events
     * will be discarded (with the exception of removal of step traces).
     *
     * @param update whether to enable the update facilities
     **/
    public void setPermanentUpdate(boolean update) {
        this.permanentUpdate = update;
    }

    /**
     * Internally add the given {@link StepTrace} to the model. Steps without an
     * actual step count component are ignored.
     * <p>
     * When the step is added, we create a new {@link JTable} and the associated
     * {@link StepTableModel} for this table model. We also observe the nested
     * model for changes so we can propagate them as updates of our cell.
     * Afterwards, we inform all listeners (including the {@link JTable}
     * component related to this model) about the change.
     * </p>
     *
     * @param stepTrace the step trace to add
     */
    private void addStepTrace(StepTrace stepTrace) {
        if (stepTrace.getStepIdentifier().getComponents().length > 0) {
            JPanel panel = new JPanel(new GridLayout(1, 1));
            StepTableModel stepModel = new StepTableModel(stepTrace, eventBuffer);
            JTable tb = new JTable(stepModel);

            ErrorLevelTableRenderer elr = new ErrorLevelTableRenderer();
            tb.setDefaultRenderer(Object.class, elr);

            stepModel.addTableModelListener(new TableModelListenerImpl(stepTrace));

            // create popupMenu
            tb.addMouseListener(new PopupMenuMouseListener());
            panel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.RAISED), stepTrace.toString()));
            panel.add(tb);

            addRow(new Object[] { panel });
            this.traces.add(stepTrace);
            this.stepModels.put(stepTrace, stepModel);

            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
            fireTableCellUpdated(getRowCount() - 1, 0);
        }
    }

    private void removeStepTrace(StepTrace stepTrace) {
        if (stepTrace.getStepIdentifier().getComponents().length > 0) {
            int row = this.traces.indexOf(stepTrace);
            if (row != -1) {
                removeRow(row);
                this.traces.remove(row);

                if (this.permanentUpdate) {
                    this.fireTableRowsDeleted(row, row);
                }
                StepTableModel stepModel = stepModels.get(stepTrace);
                if (stepModel != null) {
                    stepModel.dispose();
                    stepModels.remove(stepTrace);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If permanent updates are enabled, the model is updated and the change is propagated to listeners.
     * </p>
     * @see #setPermanentUpdate(boolean)
     **/
    @Override
    public void stepTraceChanged(StepTrace stepTrace) {
        if (permanentUpdate == true) {
            fireStepTraceChanged(stepTrace);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If permanent updates are enabled, the model is updated and the change is propagated to listeners.
     * </p>
     * @see #setPermanentUpdate(boolean)
     **/
    @Override
    public void stepTraceAdded(StepTraceRepository repository,
                               StepTrace stepTrace) {
        if (permanentUpdate == true && eventBuffer != null) {
            addStepTrace(stepTrace);
            fireStepTraceAdded(repository, stepTrace);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If permanent updates are enabled, the model is updated and the change is propagated to listeners.
     * </p>
     * @see #setPermanentUpdate(boolean)
     **/
    @Override
    public void stepTraceRemoved(StepTraceRepository repository,
                                 StepTrace stepTrace) {
        if (permanentUpdate == true) {
            removeStepTrace(stepTrace);
            fireStepTraceRemoved(repository, stepTrace);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This {@link LoggerTableModel} never expects such a request because the
     * {@link RepositoryChangeBuffer} should discard it.
     * </p>
     **/
    @Override
    public void stepTraceRemoveRequest(StepTraceRemoveRequest request) {
        assert false : "A stepTraceRemoveRequest should never reach a LoggerTableModel.";
    }

    /**
     * Enquire the current row index of the given step trace.
     *
     * @param stepTrace the step trace to look up
     * @return the index of the first row containing the given step trace in the
     *         table model. Returns -1 if the table does not contain the given
     *         step trace.
     */
    public int getIndexOf(StepTrace stepTrace) {
        return this.traces.indexOf(stepTrace);
    }

    /**
     * Registers the given <code>listener</code> for notifications about changes
     * to the step traces contained in this model (like additions, removals,
     * etc.).
     *
     * @param listener the observer of step trace changes
     * @deprecated It seems that no one uses this interface. We should remove
     *             it.
     */
    public void addRepositoryChangeListener(RepositoryChangeListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Propagate a change in the given step trace to all registered
     * {@link RepositoryChangeListener}s.
     *
     * @param stepTrace the step trace to which a new message was added
     * @deprecated It seems that no one uses this interface. We should remove
     *             it.
     */
    public void fireStepTraceChanged(StepTrace stepTrace) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceChanged(stepTrace);
        }
    }

    /**
     * Propagate the addition of the given step trace to all registered
     * {@link RepositoryChangeListener}s.
     *
     * @param repository the repository to which the created step trace belongs
     * @param stepTrace the new created step trace
     * @deprecated It seems that no one uses this facility. We should remove
     *             it.
     **/
    public void fireStepTraceAdded(StepTraceRepository repository,
                                   StepTrace stepTrace) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceAdded(repository, stepTrace);
        }
    }

    /**
     * Propagate the removal of the given step trace to all registered
     * {@link RepositoryChangeListener}s.
     *
     * @param repository the repository to which the removed step trace belonged
     * @param stepTrace the removed step trace
     * @deprecated It seems that no one uses this facility. We should remove
     *             it.
     **/
    public void fireStepTraceRemoved(StepTraceRepository repository,
                                     StepTrace stepTrace) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceRemoved(repository, stepTrace);
        }
    }

    /**
     * Propagate the request to remove the given step trace to all registered
     * {@link RepositoryChangeListener}s.
     *
     * @param request the StepTraceRemovalRequest with the StepTrace to remove
     * @deprecated It seems that no one uses this facility. We should remove
     *             it.
     **/
    public void fireStepTraceRemoveRequest(StepTraceRemoveRequest request) {
        RepositoryChangeListener[] l = this.listeners.toArray(new RepositoryChangeListener[] {  });
        for (int x = 0; x < l.length; x++) {
            l[x].stepTraceRemoveRequest(request);
        }
    }

    // --------------------------------------------------------------------
    /**
     * This class implements the context menu functionality for step traces. It
     * uses data from nested {@link StepTableModel}s to determine which menu
     * entries to display.
     *
     * @author Sven Offermann (code)
     * @author Michael Duvigneau (documentation)
     **/
    private class PopupMenuMouseListener extends MouseAdapter {

        /**
         * {@inheritDoc}
         * <p>
         * If the event is a popup trigger for a JTable (usually a nested step
         * table), open the context menu for the underlying table entry. Other
         * events are ignored.
         * </p>
         *
         * @param e the event to handle
         **/
        @Override
        public void mousePressed(MouseEvent e) {
            Component c = e.getComponent();
            if ((c instanceof JTable) && (e.isPopupTrigger())) {
                openPopup((JTable) c, e);
            }
        }

        /**
         * {@inheritDoc}
         * <p>
         * If the event is a popup trigger for a JTable (usually a nested step
         * table), open the context menu for the underlying table entry. Other
         * events are ignored.
         * </p>
         *
         * @param e the event to handle
         **/
        @Override
        public void mouseReleased(MouseEvent e) {
            Component c = e.getComponent();
            if ((c instanceof JTable) && (e.isPopupTrigger())) {
                openPopup((JTable) c, e);
            }
        }

        /**
         * {@inheritDoc}
         * <p>
         * If the event is double click on a JTable (usually a nested step
         * table), open a detail view for the underlying table entry. Other
         * events are ignored.
         * </p>
         *
         * @param e the event to handle
         **/
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                Component c = e.getComponent();
                if (c instanceof JTable) {
                    // open a new window with a detailed view of the simulation
                    // event log message
                    openDetailView((JTable) c, e);
                }
            }
        }

        /**
         * Construct and display the context menu for the current table row. It
         * is assumed that the given JTable hosts a {@link StepTableModel}. To
         * determine the table row, the current mouse position is evaluated
         * relative to the nested table.
         *
         * @param table  the nested table displaying the step events.
         * @param e  the mouse event that led to context menu creation.
         */
        private void openPopup(JTable table, MouseEvent e) {
            Point p = e.getPoint();
            int row = table.rowAtPoint(p);
            ListSelectionModel selectionModel = table.getSelectionModel();
            selectionModel.setSelectionInterval(row, row);
            StepTableModel model = (StepTableModel) table.getModel();

            SimulationEvent simEvent = (SimulationEvent) model.getValueAt(row, 0);

            JPopupMenu popup = new JPopupMenu();
            if (SimulatorPlugin.getCurrent().isSimulationActive()) {
                RemotePlugin remote = RemotePlugin.getInstance();
                JMenuItem item1 = new JMenuItem("show net pattern");
                if (!(simEvent instanceof NetEvent)) {
                    item1.setEnabled(false);
                } else {
                    item1.addActionListener(new OpenNetPatternAction(remote
                        .wrapInstance(((NetEvent) simEvent).getNetInstance())));
                }
                popup.add(item1);

                JMenuItem item2 = new JMenuItem("show net instance");
                if (!(simEvent instanceof NetEvent)) {
                    item2.setEnabled(false);
                } else {
                    item2.addActionListener(new OpenNetInstanceAction(remote
                        .wrapInstance(((NetEvent) simEvent).getNetInstance())));
                }
                popup.add(item2);

                popup.addSeparator();

                JMenuItem item3 = new JMenuItem("show net pattern element");
                if ((simEvent instanceof PlaceEvent)
                            || (simEvent instanceof TransitionEvent)) {
                    if (simEvent instanceof PlaceEvent) {
                        item3.addActionListener(new OpenNetPatternElementAction(remote
                                                                                .wrapInstance(((PlaceEvent) simEvent)
                                                                                              .getPlaceInstance())));
                    } else {
                        item3.addActionListener(new OpenNetPatternElementAction(remote
                                                                                .wrapInstance(((TransitionEvent) simEvent)
                                                                                              .getTransitionInstance())));
                    }
                } else {
                    item3.setEnabled(false);
                }
                popup.add(item3);

                JMenuItem item4 = new JMenuItem("show net instance element");
                if ((simEvent instanceof PlaceEvent)
                            || (simEvent instanceof TransitionEvent)) {
                    if (simEvent instanceof PlaceEvent) {
                        item4.addActionListener(new OpenNetInstanceElementAction(remote
                                                                                 .wrapInstance(((PlaceEvent) simEvent)
                                                                                               .getPlaceInstance())));
                    } else {
                        item4.addActionListener(new OpenNetInstanceElementAction(remote
                                                                                 .wrapInstance(((TransitionEvent) simEvent)
                                                                                               .getTransitionInstance())));
                    }
                } else {
                    item4.setEnabled(false);
                }
                popup.add(item4);
            } else {
                JMenuItem item5 = new JMenuItem("Simulation terminated");
                item5.setEnabled(false);
                popup.add(item5);
            }

            popup.show(table, e.getX(), e.getY());
        }

        /**
         * Construct and display the detail view for the current table row. It
         * is assumed that the given JTable hosts a {@link StepTableModel}. To
         * determine the table row, the current mouse position is evaluated
         * relative to the nested table.

         * @param table  the nested table displaying the step events.
         * @param e  the mouse event that led to detail view display.
         */
        private void openDetailView(JTable table, MouseEvent e) {
            Point p = e.getPoint();
            int row = table.rowAtPoint(p);
            StepTableModel model = (StepTableModel) table.getModel();
            SimulationEvent simEvent = (SimulationEvent) model.getValueAt(row, 0);

            JFrame detailFrame = new JFrame();
            detailFrame.setSize(600, 300);
            detailFrame.getContentPane().setLayout(new BorderLayout());
            JTextArea textArea = new JTextArea(simEvent.toString());
            JScrollPane scrollPane = new JScrollPane(textArea);
            detailFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            detailFrame.setVisible(true);
        }

        // Inner action classes.
        /**
         * The action to open a net instance drawing with token game in the
         * editor that displays the given {@link NetInstanceAccessor}.
         **/
        private class OpenNetInstanceAction implements ActionListener {
            private NetInstanceAccessor netInstance;

            public OpenNetInstanceAction(NetInstanceAccessor instance) {
                netInstance = instance;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                GuiPlugin.getCurrent().getGui().openInstanceDrawing(netInstance);
            }
        }

        /**
         * The action to open a net template drawing in the editor that displays
         * the net underlying the given {@link NetInstanceAccessor}.
         **/
        private class OpenNetPatternAction implements ActionListener {
            private NetInstanceAccessor netInstance;

            public OpenNetPatternAction(NetInstanceAccessor instance) {
                netInstance = instance;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    GuiPlugin.getCurrent().getGui()
                             .openNetPatternDrawing(netInstance.getNet()
                                                               .getName());
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * The action to open a net template drawing in the editor that displays
         * the net containing the place or transition underlying the given
         * {@link PlaceInstanceAccessor} or {@link TransitionInstanceAccessor}.
         * The net element will be selected.
         **/
        private class OpenNetPatternElementAction implements ActionListener {
            private Object elementInstance;

            public OpenNetPatternElementAction(PlaceInstanceAccessor instance) {
                elementInstance = instance;
            }

            public OpenNetPatternElementAction(TransitionInstanceAccessor instance) {
                elementInstance = instance;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    NetInstanceAccessor netInstance = null;
                    NetElementID elementID = null;
                    if (elementInstance instanceof PlaceInstanceAccessor) {
                        PlaceInstanceAccessor placeInstance = (PlaceInstanceAccessor) elementInstance;
                        netInstance = placeInstance.getNetInstance();
                        elementID = placeInstance.getID();
                    } else if (elementInstance instanceof TransitionInstanceAccessor) {
                        TransitionInstanceAccessor transitionInstance = (TransitionInstanceAccessor) elementInstance;
                        netInstance = transitionInstance.getNetInstance();
                        elementID = transitionInstance.getID();
                    }
                    int figureID = FigureWithID.NOID;
                    if (elementID != null) {
                        figureID = elementID.getFigureID();
                    }
                    GuiPlugin guiPlugin = GuiPlugin.getCurrent();
                    CPNApplication gui = guiPlugin.getGui();

                    // NOTICEnull
                    if (netInstance != null) {
                        String netName = netInstance.getNet().getName();
                        gui.openNetPatternDrawing(netName, figureID);
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * The action to open a net instance drawing with token game in the
         * editor that displays the net containing the place or transition
         * underlying the given {@link PlaceInstanceAccessor} or
         * {@link TransitionInstanceAccessor}. The net element will be selected.
         **/
        private class OpenNetInstanceElementAction implements ActionListener {
            private Object elementInstance;

            public OpenNetInstanceElementAction(PlaceInstanceAccessor instance) {
                elementInstance = instance;
            }

            public OpenNetInstanceElementAction(TransitionInstanceAccessor instance) {
                elementInstance = instance;
            }

            public void actionPerformed(ActionEvent e) {
                try {
                    NetInstanceAccessor netInstance = null;
                    NetElementID elementID = null;
                    if (elementInstance instanceof PlaceInstanceAccessor) {
                        PlaceInstanceAccessor placeInstance = (PlaceInstanceAccessor) elementInstance;
                        netInstance = placeInstance.getNetInstance();
                        elementID = placeInstance.getID();
                    } else if (elementInstance instanceof TransitionInstanceAccessor) {
                        TransitionInstanceAccessor transitionInstance = (TransitionInstanceAccessor) elementInstance;
                        netInstance = transitionInstance.getNetInstance();
                        elementID = transitionInstance.getID();
                    }
                    int figureID = FigureWithID.NOID;
                    if (elementID != null) {
                        figureID = elementID.getFigureID();
                    }
                    GuiPlugin guiPlugin = GuiPlugin.getCurrent();
                    CPNApplication gui = guiPlugin.getGui();
                    gui.openInstanceDrawing(netInstance);
                    CPNInstanceDrawing instDwg = CPNInstanceDrawing
                                                     .getInstanceDrawing(netInstance);
                    if ((instDwg != null) && (figureID != FigureWithID.NOID)) {
                        Figure figure = instDwg.getInstanceFigureOfFigureWithID(figureID);
                        DrawingView view = gui.getView(instDwg);
                        if (figure != null && view != null) {
                            view.clearSelection();
                            view.addToSelection(figure);
                            view.repairDamage();
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }


    /**
     * Objects of this class listen to change notifications of nested step trace
     * tables and propagate some of them as cell updates for the
     * {@link LoggerTableModel}.
     *
     * @author Sven Offermann (code)
     * @author Michael Duvigneau (documentation)
     */
    private class TableModelListenerImpl implements TableModelListener {
        private StepTrace stepTrace;

        /**
         * Create a listener for a nested step trace table. The given
         * <code>stepTrace</code> is not used to filter events, it merely serves
         * as pointer to the table row that displays the nested table.
         *
         * @param stepTrace the step trace belonging to the table cell to update
         *            in case of changes.
         **/
        public TableModelListenerImpl(StepTrace stepTrace) {
            this.stepTrace = stepTrace;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Forwards some events to the {@link LoggerTableModel} as update
         * notifications for the cell displaying the <code>stepTrace</code>
         * defined at listener construction time. Only
         * {@link TableModelEvent#INSERT} and {@link TableModelEvent#UPDATE}
         * events are forwarded.
         * </p>
         * @param e the event that occurred in the nested step trace table
         **/
        @Override
        public void tableChanged(TableModelEvent e) {
            if ((e.getType() == TableModelEvent.INSERT)
                        || (e.getType() == TableModelEvent.UPDATE)) {
                fireTableCellUpdated(getIndexOf(this.stepTrace), 0);
            }
        }
    }

    /**
     * Removes all registered {@link RepositoryChangeListener}s, all {@link StepTrace}s
     * and the {@link RepositoryChangeBuffer}. Please use this method if the logging shall be stopped.
     */
    protected void removeAll() {
        listeners.removeAll(listeners);
        traces.removeAll(traces);
        eventBuffer.stopBuffer();
        eventBuffer = null;
    }
}