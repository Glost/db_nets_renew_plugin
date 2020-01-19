/*
 * Created on 28.01.2004
 *
 */
package de.renew.gui.menu;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.figures.ArrowTip;

import CH.ifa.draw.standard.ChangeAttributeCommand;

import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.CommandMenuItem;

import de.renew.gui.AssocArrowTip;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.CompositionArrowTip;
import de.renew.gui.DoubleArrowTip;
import de.renew.gui.IsaArrowTip;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


/**
 * This class creates the menu extensions for the Attributes menu.
 * <ul>
 * <li><em>Arrow Shape</em> lets you choose double tipped arrows </li>
 * <li><em>Text Type</em> lets you change the type of a text field (label, inscription, name)</li>
 * </ul>
 *
 * @author J&ouml;rn Schumacher
 */
public class AttributesMenuExtender {
    public Collection<JMenuItem> createMenus() {
        Collection<JMenuItem> result = new Vector<JMenuItem>();
        result.add(MenuManager.createSeparator("de.renew.gui.align.sep"));

        /*
         * TODO: JSC: Move to CH
         */
        CommandMenu arrowTipMenu = DrawApplication.createCommandMenu("Arrow Shape");
        arrowTipMenu.add(new CommandMenuItem(new ChangeAttributeCommand("normal",
                                                                        "ArrowTip",
                                                                        ArrowTip.class
                                                                        .getName())));
        arrowTipMenu.add(new CommandMenuItem(new ChangeAttributeCommand("double",
                                                                        "ArrowTip",
                                                                        DoubleArrowTip.class
                                                                        .getName())));
        arrowTipMenu.add(new CommandMenuItem(new ChangeAttributeCommand("lines",
                                                                        "ArrowTip",
                                                                        AssocArrowTip.class
                                                                        .getName())));
        arrowTipMenu.add(new CommandMenuItem(new ChangeAttributeCommand("triangle",
                                                                        "ArrowTip",
                                                                        IsaArrowTip.class
                                                                        .getName())));
        arrowTipMenu.add(new CommandMenuItem(new ChangeAttributeCommand("diamond",
                                                                        "ArrowTip",
                                                                        CompositionArrowTip.class
                                                                        .getName())));

        result.add(arrowTipMenu);
        CommandMenu textTypeMenu = DrawApplication.createCommandMenu("Text Type");

        textTypeMenu.add(new CommandMenuItem(new ChangeAttributeCommand("Label",
                                                                        "TextType",
                                                                        new Integer(CPNTextFigure.LABEL))));
        textTypeMenu.add(new CommandMenuItem(new ChangeAttributeCommand("Inscription",
                                                                        "TextType",
                                                                        new Integer(CPNTextFigure.INSCRIPTION))));
        textTypeMenu.add(new CommandMenuItem(new ChangeAttributeCommand("Name",
                                                                        "TextType",
                                                                        new Integer(CPNTextFigure.NAME))));
        result.add(textTypeMenu);
        return result;
    }
}