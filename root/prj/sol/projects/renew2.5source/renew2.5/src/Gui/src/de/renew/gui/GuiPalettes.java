/*
 * Created on 27.01.2004
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.Palette;
import CH.ifa.draw.util.PaletteListener;

import java.awt.Container;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;


/**
 * This class represents the palettes provided by the Gui plugin.
 * It is used to receive delegated PaletteHolder calls.
 *
 * @author J&ouml;rn Schumacher
 */
public class GuiPalettes implements PaletteHolder {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(GuiPalettes.class);

    /**
     * A list of all palettes that have been registered.
     **/
    private Vector<Palette> _palettes = new Vector<Palette>();
    private GuiPlugin plugin;

    /**
     * The PaletteListener that receives Palette Events from ToolButtons and such.
     * If a Gui is currently open, it delegates the event there.
     **/
    GuiEventDelegator _eventDelegator = new GuiEventDelegator();

    public GuiPalettes(GuiPlugin gui) {
        plugin = gui;
    }

    void notifyGuiOpen() {
        JPanel toolPanel = plugin.getGui().getToolsPanel();
        Iterator<Palette> palettes = _palettes.iterator();
        while (palettes.hasNext()) {
            Palette toAdd = palettes.next();
            toolPanel.add(toAdd.getComponent());
        }
        plugin.getGuiFrame().pack();
    }

    /*
     * @see de.renew.gui.PaletteHolder#addPalette(CH.ifa.draw.util.Palette)
     */
    public void addPalette(Palette toAdd) {
        logger.debug("-> GuiPlugin: registering palette " + toAdd);
        _palettes.add(toAdd);
        if (plugin.isGuiPresent()) {
            JPanel toolPanel = plugin.getGui().getToolsPanel();
            toolPanel.add(toAdd.getComponent());
            plugin.getGuiFrame().pack();
            toolPanel.repaint();
        }
    }

    /*
     * @see de.renew.gui.PaletteHolder#removePalette(CH.ifa.draw.util.Palette)
     */
    public void removePalette(Palette toRemove) {
        logger.debug("-> GuiPlugin: removing palette " + toRemove);
        _palettes.remove(toRemove);
        if (plugin.isGuiPresent()) {
            // if the user detached the toolbar from the frame,
            // we need to close the containing Dialog explicitly
            // or if it is closed, swing will put it into the frame again
            Container c = toRemove.getComponent().getParent();
            while (c != null) {
                if (c instanceof JDialog) {
                    ((JDialog) c).dispose();
                    break;
                }
                c = c.getParent();
            }
            JPanel toolPanel = plugin.getGui().getToolsPanel();
            toolPanel.remove(toRemove.getComponent());
            toolPanel.repaint();
        }
    }

    /*
     * @see de.renew.gui.PaletteHolder#createToolButton(javax.swing.Icon, javax.swing.Icon, java.lang.String, CH.ifa.draw.framework.Tool)
     */
    public ToolButton createToolButton(Icon icon, Icon selectedIcon,
                                       String toolName, Tool tool) {
        // logger.debug("creating a toolbutton with icon " + iconName + ", toolname " + toolName);
        logger.debug("the delegator is " + _eventDelegator);
        return new ToolButton(_eventDelegator, icon, selectedIcon, toolName,
                              tool);
    }

    /*
     * @see de.renew.gui.PaletteHolder#createToolButton(java.lang.String, java.lang.String, CH.ifa.draw.framework.Tool)
     */
    public ToolButton createToolButton(String path, String toolName, Tool tool) {
        return new ToolButton(_eventDelegator, path, toolName, tool);
    }

    /**
     * The class that delegates the PaletteListener events to the
     * presently open Gui, if any
     * @author Joern Schumacher
     */
    private class GuiEventDelegator implements PaletteListener {
        public void paletteUserOver(ToolButton button, boolean inside) {
            CPNApplication gui = GuiPlugin.getCurrent().getGui();
            if (gui != null) {
                gui.paletteUserOver(button, inside);
            }
        }

        public void paletteUserSelected(ToolButton button, boolean inside) {
            CPNApplication gui = GuiPlugin.getCurrent().getGui();
            if (gui != null) {
                gui.paletteUserSelected(button, inside);
            }
        }
    }
}