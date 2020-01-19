package de.renew.refactoring.search.range;

import CH.ifa.draw.framework.Drawing;

import de.renew.gui.CPNApplication;
import de.renew.gui.GuiPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 * Provides static methods that return opened drawings from the gui.
 *
 * @author 2mfriedr
 */
public class GuiDrawings {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(GuiDrawings.class);

    /**
     * Should not be instantiated
     */
    private GuiDrawings() {
    }

    /**
     * Returns the drawings that are currently loaded in the gui and match
     * the specified filename extension.
     *
     * @param extension a list of filename extensions
     * @return a list of drawings
     */
    public static List<Drawing> guiDrawings(final List<String> extensions) {
        CPNApplication gui = GuiPlugin.getCurrent().getGui();
        Enumeration<Drawing> enumeration = gui.drawings();

        List<Drawing> drawings = new ArrayList<Drawing>();
        while (enumeration.hasMoreElements()) {
            Drawing drawing = enumeration.nextElement();
            if (extensions.contains(drawing.getDefaultExtension())) {
                drawings.add(drawing);
            }
        }
        return drawings;
    }

    /**
     * Returns the drawings that are currently loaded in the gui and match
     * the specified filename extension.
     *
     * @param extension the filename extension
     * @return a list of drawings
     */
    public static List<Drawing> guiDrawings(final String extension) {
        return guiDrawings(Collections.singletonList(extension));
    }
}