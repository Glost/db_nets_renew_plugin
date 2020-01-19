/*
 * Created on 23.05.2003
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.ConnectionTool;
import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.Palette;

import de.renew.application.SimulatorPlugin;

import de.renew.plugin.PluginManager;
import de.renew.plugin.PropertyHelper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;


/**
 * This class is used when the Java Net Compiler is chosen.
 * <p>
 * It provides: <ul>
 * <li> a menu with an item to select the sequential compiler mode. </li>
 * <li> a palette with the inhibitor and clear arcs. This palette
 *      gets displayed only if the sequential compiler mode is
 *      activated. </li>
 * </ul></p>
 * @author J&ouml;rn Schumacher
 * @author Michael Duvigneau
 */
public class JavaGuiCreator implements FormalismGuiCreator {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(JavaGuiCreator.class);
    private boolean toolsVisible = false;
    private Palette sequentialTools = null;
    private JCheckBoxMenuItem sequentialItem;

    /*
     * @see de.renew.gui.FormalismPlugin.FormalismGuiCreator#createMenus()
     */
    public JMenuItem createMenu() {
        int mult = PropertyHelper.getIntProperty(getSimulatorPlugin()
                                                     .getProperties(),
                                                 SimulatorPlugin.MODE_PROP_NAME,
                                                 1);
        boolean isSequential = (mult == -1);
        sequentialItem = new JCheckBoxMenuItem("Show sequential-only arcs",
                                               isSequential);
        sequentialItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sequentialChanged();
                }
            });
        sequentialChanged();
        return sequentialItem;
    }

    public Palette createPalette() {
        return null;
    }

    public void formalismActivated() {
        // nothing to do.
    }

    public void formalismDeactivated() {
        hideSequentialPalette();
    }

    private void sequentialChanged() {
        // TODO: React to direct property changes (if someone
        // configures the simulation engine from outside)
        boolean isSequential = sequentialItem.isSelected();
        if (isSequential) {
            createSequentialPalette();
        } else {
            hideSequentialPalette();
        }
    }

    private void hideSequentialPalette() {
        if ((sequentialTools != null) && toolsVisible) {
            GuiPlugin guiPlugin = GuiPlugin.getCurrent();
            guiPlugin.getPaletteHolder().removePalette(sequentialTools);
            toolsVisible = false;
        }
    }

    private void createSequentialPalette() {
        GuiPlugin guiPlugin = GuiPlugin.getCurrent();
        PaletteHolder paletteHolder = guiPlugin.getPaletteHolder();
        if (sequentialTools == null) {
            DrawingEditor editor = guiPlugin.getDrawingEditor();
            Tool tool = new ConnectionTool(editor,
                                           InhibitorConnection.InhibitorArc);
            sequentialTools = new Palette("net elements for sequential mode");
            ToolButton button = paletteHolder.createToolButton(CPNApplication.CPNIMAGES
                                                               + "INHIB",
                                                               "InhibitorArc Tool",
                                                               tool);
            sequentialTools.add(button);

            tool = new ConnectionTool(editor,
                                      HollowDoubleArcConnection.HollowArc);
            button = paletteHolder.createToolButton(CPNApplication.CPNIMAGES
                                                    + "DHARC", "ClearArc Tool",
                                                    tool);
            sequentialTools.add(button);
        }
        if (!toolsVisible) {
            Properties props = getSimulatorPlugin().getProperties();
            if (!props.getProperty(SimulatorPlugin.MODE_PROP_NAME, "").trim()
                              .equals("-1")) {
                props.setProperty(SimulatorPlugin.MODE_PROP_NAME, "-1");
                if (logger.isInfoEnabled()) {
                    logger.info("For your convenience, the sequential simulation mode has been turned on, too.");
                    logger.info("In order to reset your simulation to concurrent simulation mode do: set "
                                + SimulatorPlugin.MODE_PROP_NAME + "=1");
                    logger.info("or uncheck the propterty in the \"Configure Simulation\" dialog.");
                }
            }

            paletteHolder.addPalette(sequentialTools);
            toolsVisible = true;
        }
    }

    private SimulatorPlugin getSimulatorPlugin() {
        return (SimulatorPlugin) PluginManager.getInstance()
                                              .getPluginsProviding("de.renew.simulator")
                                              .iterator().next();
    }

    /**
     * Returns a gui creator suitable for sequential-only formalisms. This
     * gui creator will always display the palette with sequential tools.
     **/
    public FormalismGuiCreator getSequentialJavaGuiCreator() {
        return new SequentialJavaGuiCreator();
    }

    private class SequentialJavaGuiCreator implements FormalismGuiCreator {
        public JMenuItem createMenu() {
            return null;
        }

        public Palette createPalette() {
            return null;
        }

        public void formalismActivated() {
            createSequentialPalette();
        }

        public void formalismDeactivated() {
            hideSequentialPalette();
        }
    }
}