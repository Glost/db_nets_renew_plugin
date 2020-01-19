/**
 * The logging plug-in (comprising the package
 * <code>de.renew.gui.logging</code>) provides simulation trace windows and
 * related configuration facilities in the Renew GUI.
 *
 * <h3>Functionality overview:</h3>
 * <p>
 * To present the simulation trace graphically, the logging plug-in
 * attaches to the Log4j framework, receives simulation-related events,
 * groups them into simulation steps, presents them as a table in the
 * graphical user interface, and allows the user to navigate directly
 * to the net elements that caused the events.
 * Multiple simulation trace views can be defined that present only
 * simulation steps affecting some specific net or net element (place or
 * transition).
 * The Renew simulation configuration dialog is extended by a logging
 * configuration tab that provides facilites to create, configure and
 * delete these views as well as other simulation-related Log4j appenders.
 * The usage of the simulation trace windows and the configuration tab is
 * explained in the Renew User Guide.
 * </p>
 *
 * <h3>Internal architecture:</h3>
 * <dl>
 * <dt>Log4j integration:</dt>
 * <dd>
 *   The simulator produces Log4j events with the message type
 *   {@link de.renew.engine.common.SimulatorEvent} that wrap instances of
 *   the {@link de.renew.engine.events.SimulationEvent} type hierarchy (see
 *   package {@link de.renew.engine.events}).
 *   The Log4j message events are targeted to logger categories following
 *   the naming scheme <code>simulation.netname.elementname</code>.
 *   The user can define appenders for logger categories by his choice.
 *   By default, only the global logger category <code>simulation</code> is
 *   defined.
 *   To collect simulation events of a logger category for the graphical
 *   simulation trace, a {@link de.renew.gui.logging.GuiAppender} instance
 *   for the respective category name needs to be registered with the Log4j
 *   framework.
 * </dd>
 *
 * <dt>Event collection:</dt>
 * <dd>
 *   The {@link de.renew.gui.logging.MainRepositoryManager} manages
 *   repositories per simulation run so that old repositories can still be
 *   inspected after a simulation run has been terminated.
 *   A {@link de.renew.gui.logging.MainRepository} collects simulation
 *   events and merges them to {@link de.renew.gui.logging.StepTrace}
 *   objects (multiple events form one step).
 *   The {@link de.renew.gui.logging.MainRepository} is not accessed
 *   directly, instead each {@link de.renew.gui.logging.GuiAppender}
 *   determines one or more {@link de.renew.gui.logging.LoggerRepository}
 *   instances that are interested in events belonging to its logger
 *   category.
 *   Each {@link de.renew.gui.logging.LoggerRepository} presents a view
 *   with limited capacity in the number of steps (not events!).
 *   The capacity is configured via the
 *   {@link de.renew.gui.logging.GuiAppender#setPufferSize} method and
 *   passed to related repositories on each event update.
 *   When a {@link de.renew.gui.logging.LoggerRepository} wants to discard
 *   a step, it requests the removal from the
 *   {@link de.renew.gui.logging.MainRepository}.  Other repositories can
 *   veto if they still need the step.
 * </dd>
 *
 * <dt>Event display:</dt>
 * <dd>
 *  A {@link de.renew.gui.logging.LoggerTableModel} presents simulation
 *  steps in a single-colum table.
 *  Each cell is again a {@link javax.swing.JTable} populated with a
 *  {@link de.renew.gui.logging.StepTableModel} that presents the
 *  simulation events forming the step.
 *  Both models listen to changes in their associated
 *  {@link de.renew.gui.logging.LoggerRepository} or
 *  {@link de.renew.gui.logging.StepTrace}, respectively.
 *  After a model integrated a change event into its data, the
 *  corresponding {@link javax.swing.JTable} instance is informed so that
 *  it displays the change.
 *  The {@link de.renew.gui.logging.LoggerRepository} sends a removal
 *  notice if it discards a step due to capacity limits.
 * </dd>
 *
 * <dt>Threading:</dt>
 * <dd><em>(To be implemented)</em></dd>
 * <dd>
 *   Since the Log4j framework does not decouple threads, the
 *   {@link de.renew.gui.logging.GuiAppender#doAppend} method is usually
 *   called within the simulation thread that fires the binding that
 *   produced the event.
 *   Since Log4j requires quick event handling in appenders and the
 *   manipulation of Swing components is allowed only in the AWT event
 *   thread, we need to cut the listener update chain somewhere.
 *   In the calling simulation thread, we just sort the event into the
 *   respective {@link de.renew.gui.logging.LoggerRepository} and
 *   {@link de.renew.gui.logging.StepTrace} collections.
 *   The update notifications from these collections to the respective
 *   {@link de.renew.gui.logging.LoggerTableModel} and
 *   {@link de.renew.gui.logging.StepTableModel} instances are decoupled.
 *   But the simulator can produce events in quick succession so that the
 *   update requests flood the AWT event thread which leads to an
 *   unbearable responsiveness of the user interface.
 *   Therefore, we not only buffer the update notifications, but also
 *   discard them when they become outdated.
 * </dd>
 *
 * @author Sven Offermann (original code)
 * @author Michael Duvigneau (documentation)
 * @since Renew 2.1
 **/
package de.renew.gui.logging;

