/*
 * Created on 06.06.2003
 *
 */
package de.renew.gui;

import CH.ifa.draw.framework.Tool;

import CH.ifa.draw.standard.ToolButton;

import CH.ifa.draw.util.Palette;

import javax.swing.Icon;


/**
 * @author 6schumac
 *
 */
public interface PaletteHolder {

    /**
     * add the given Palette
     * @param palette the Palette to add
     */
    public void addPalette(Palette palette);

    /**
     * remove the gicen Palette
     * @param palette the Palette to remove
     */
    public void removePalette(Palette palette);

    /**
     * create a button for with the given icons, name and tool
     * @param icon the icon for the button
     * @param selectedIcon the icon for the pressed button
     * @param toolName the name of the tool
     * @param tool the tool to be triggered with this button
     */
    public ToolButton createToolButton(Icon icon, Icon selectedIcon,
                                       String toolName, Tool tool);

    /**
     * try to create a button withe the given name and tool, loading the icons from the given path
     * @param path the path where to look for the icon files
     * @param toolName the name of this tool
     * @param tool the Tool to trigger
     */
    public ToolButton createToolButton(String path, String toolName, Tool tool);
}