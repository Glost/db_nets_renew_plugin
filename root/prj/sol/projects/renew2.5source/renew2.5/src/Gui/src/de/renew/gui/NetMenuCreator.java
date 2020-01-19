/*
 * Created on 28.01.2004
 *
 */
package de.renew.gui;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager.SeparatorFactory;

import CH.ifa.draw.standard.ChangeAttributeCommand;

import CH.ifa.draw.util.Command;
import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.CommandMenuItem;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenuItem;


/**
 * This class creates the Net menu for the Gui plugin.<br>
 * The Net menu contains entries which offer editing functionality
 * for drawings representing Renew nets.
 *
 * @author J&ouml;rn Schumacher
 */
public class NetMenuCreator {
    public Collection<JMenuItem> createMenus(BreakpointManager bpm) {
        SeparatorFactory sepFac = new SeparatorFactory("de.renew.gui.net");
        Vector<JMenuItem> result = new Vector<JMenuItem>();
        result.add(new CommandMenuItem(new SplitCommand("Split transition/place")));
        result.add(new CommandMenuItem(new CoarseCommand("Coarsen subnet")));
        result.add(sepFac.createSeparator());

        CommandMenu traceMenu = DrawApplication.createCommandMenu("Trace");

        traceMenu.add(new ChangeAttributeCommand("on", "TraceMode", Boolean.TRUE));
        traceMenu.add(new ChangeAttributeCommand("off", "TraceMode",
                                                 Boolean.FALSE));
        result.add(traceMenu);

        CommandMenu markingMenu = DrawApplication.createCommandMenu("Marking");

        markingMenu.add(new ChangeAttributeCommand("highlight only",
                                                   "MarkingAppearance",
                                                   new Integer(PlaceFigure.HIGHLIGHT)));
        markingMenu.add(new ChangeAttributeCommand("Cardinality",
                                                   "MarkingAppearance",
                                                   new Integer(PlaceFigure.CARDINALITY)));
        markingMenu.add(new ChangeAttributeCommand("Tokens",
                                                   "MarkingAppearance",
                                                   new Integer(PlaceFigure.TOKENS)));
        markingMenu.add(new ChangeAttributeCommand("expanded Tokens",
                                                   "MarkingAppearance",
                                                   new Integer(PlaceFigure.EXPANDED_TOKENS)));
        result.add(markingMenu);
        result.add(bpm.getNetMenu());
        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new SetIconFigureCommand("Set Selection as Icon")));
        result.add(new CommandMenuItem(new HighlightAssociateCommand("Associate Highlight")));
        result.add(new CommandMenuItem(new HighlightSelectCommand("Select Highlight(s)")));
        result.add(new CommandMenuItem(new HighlightUnassociateCommand("Unassociate Highlight")));
        result.add(sepFac.createSeparator());

        result.add(new CommandMenuItem(new Command("Syntax Check") {
                @Override
                public void execute() {
                    CPNApplication ca = GuiPlugin.getCurrent().getGui();
                    ca.syntaxCheck();
                }
            }, KeyEvent.VK_S,
                                       Toolkit.getDefaultToolkit()
                                              .getMenuShortcutKeyMask()
                                       + KeyEvent.ALT_DOWN_MASK));

//        result.add(DrawApplication.createMenuItem("Syntax check",
//                                                  KeyEvent.VK_5,
//                                                  new ActionListener() {
//                public void actionPerformed(ActionEvent event) {
//                    CPNApplication ca = GuiPlugin.getCurrent().getGui();
//                    ca.syntaxCheck();
//                }
//            }));
        result.add(new CommandMenuItem(new LayoutCheckCommand("Layout check"),
                                       KeyEvent.VK_L,
                                       Toolkit.getDefaultToolkit()
                                              .getMenuShortcutKeyMask()
                                       + KeyEvent.ALT_DOWN_MASK));


        //Excluded because the results are not reliable.
        //result.add(new CommandMenuItem(new TypeCheckCommand("Type Check")));
        return result;
    }
}