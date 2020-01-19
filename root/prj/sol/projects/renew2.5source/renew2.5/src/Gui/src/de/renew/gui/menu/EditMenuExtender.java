/*
 * Created on 28.01.2004
 *
 */
package de.renew.gui.menu;

import CH.ifa.draw.application.DrawApplication;

import CH.ifa.draw.figures.SelectCommand;

import CH.ifa.draw.framework.ChildFigure;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureFilter;

import CH.ifa.draw.util.CommandMenu;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.InscribableFigure;
import de.renew.gui.NodeFigure;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * This class is used by the Gui plugin to add items to the Edit menu.
 *
 * @author J&ouml;rn Schumacher
 */
public class EditMenuExtender {
    public Collection<JMenuItem> createMenus() {
        Collection<JMenuItem> result = new Vector<JMenuItem>();

        JMenu selectMenu = createSelectMenu(SelectCommand.SELECT);
        result.add(selectMenu);
        JMenu addSelectMenu = createSelectMenu(SelectCommand.ADD);
        result.add(addSelectMenu);
        JMenu removeSelectMenu = createSelectMenu(SelectCommand.REMOVE);
        result.add(removeSelectMenu);
        JMenu restrictSelectMenu = createSelectMenu(SelectCommand.RESTRICT);
        result.add(restrictSelectMenu);

        return result;
    }

    protected JMenu createSelectMenu(int selectMode) {
        String selectMenuTitle;

        switch (selectMode) {
        case SelectCommand.SELECT:
            selectMenuTitle = "Select";
            break;
        case SelectCommand.ADD:
            selectMenuTitle = "Add To Selection";
            break;
        case SelectCommand.REMOVE:
            selectMenuTitle = "Remove From Selection";
            break;
        case SelectCommand.RESTRICT:
            selectMenuTitle = "Restrict Selection";
            break;
        default:
            selectMenuTitle = "??? Select mode " + selectMode + " ???";
        }

        CommandMenu selectMenu = DrawApplication.createCommandMenu(selectMenuTitle);

        CommandMenu nodeMenu = DrawApplication.createCommandMenu("Nodes");

        nodeMenu.add(new SelectCommand("All", NodeFigure.class, selectMode));
        nodeMenu.add(new SelectCommand("Transitions", TransitionFigure.class,
                                       selectMode));
        nodeMenu.add(new SelectCommand("Places", PlaceFigure.class, selectMode));
        selectMenu.add(nodeMenu);

        selectMenu.add(new SelectCommand("Arcs", ArcConnection.class, selectMode));
        selectMenu.add(new SelectCommand("Nodes and Arcs",
                                         InscribableFigure.class, selectMode));

        CommandMenu textChildMenu = DrawApplication.createCommandMenu("Text Children");

        textChildMenu.add(new SelectCommand("All", CPNTextFigure.class,
                                            InscribableFigure.class, selectMode));
        textChildMenu.add(new SelectCommand("of Transitions",
                                            CPNTextFigure.class,
                                            TransitionFigure.class, selectMode));
        textChildMenu.add(new SelectCommand("of Places", CPNTextFigure.class,
                                            PlaceFigure.class, selectMode));
        textChildMenu.add(new SelectCommand("of Arcs", CPNTextFigure.class,
                                            ArcConnection.class, selectMode));

        selectMenu.add(textChildMenu);

        CommandMenu inscrMenu = DrawApplication.createCommandMenu("Inscriptions");

        inscrMenu.add(createSelectCommand("All", InscribableFigure.class,
                                          CPNTextFigure.INSCRIPTION, selectMode));
        inscrMenu.add(createSelectCommand("of Transitions",
                                          TransitionFigure.class,
                                          CPNTextFigure.INSCRIPTION, selectMode));
        inscrMenu.add(createSelectCommand("of Places", PlaceFigure.class,
                                          CPNTextFigure.INSCRIPTION, selectMode));
        inscrMenu.add(createSelectCommand("of Arcs", ArcConnection.class,
                                          CPNTextFigure.INSCRIPTION, selectMode));
        selectMenu.add(inscrMenu);

        CommandMenu nameMenu = DrawApplication.createCommandMenu("Names");

        nameMenu.add(createSelectCommand("All", InscribableFigure.class,
                                         CPNTextFigure.NAME, selectMode));
        nameMenu.add(createSelectCommand("of Transitions",
                                         TransitionFigure.class,
                                         CPNTextFigure.NAME, selectMode));
        nameMenu.add(createSelectCommand("of Places", PlaceFigure.class,
                                         CPNTextFigure.NAME, selectMode));
        selectMenu.add(nameMenu);

        return selectMenu;
    }

    protected static SelectCommand createSelectCommand(String title,
                                                       final Class<?> parentClass,
                                                       final int type,
                                                       int selectMode) {
        return new SelectCommand(title,
                                 new FigureFilter() {
                public boolean isUsed(Figure fig) {
                    return fig instanceof CPNTextFigure
                           && parentClass.isInstance(((ChildFigure) fig).parent())
                           && ((CPNTextFigure) fig).getType() == type;
                }
            }, selectMode);
    }
}