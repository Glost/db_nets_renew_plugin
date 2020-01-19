/*
 * Created on 28.01.2004
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.application.DrawApplication;
import CH.ifa.draw.application.MenuManager;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.RoundRectangleFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.AlphaChangeCommand;

import CH.ifa.draw.util.ColorMap;
import CH.ifa.draw.util.CommandMenu;
import CH.ifa.draw.util.ExtendedFont;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class AttributesMenuCreator {
    private static final int MAX_FONT_MENU_ENTRIES = 30;

    public Collection<JMenuItem> createMenus() {
        Vector<JMenuItem> result = new Vector<JMenuItem>();
        result.add(createColorMenu("Fill Color", "FillColor"));
        result.add(createTransparencyMenu("Fill Opacity", "FillColor"));
        result.add(createColorMenu("Pen Color", "FrameColor"));
        result.add(createTransparencyMenu("Pen Opacity", "FrameColor"));
        result.add(createVisibilityMenu());
        result.add(createArrowMenu());
        result.add(createLineStyleMenu());
        result.add(createLineShapeMenu());
        result.add(createLineWidthMenu());
        result.add(createArcScaleMenu());
        result.add((new MenuManager.SeparatorFactory("ch.ifa.draw"))
            .createSeparator());

        result.add(createFontMenu());
        result.add(createFontSizeMenu());
        result.add(createFontStyleMenu());
        result.add(createTextAlignmentMenu());
        result.add(createColorMenu("Text Color", "TextColor"));
        result.add(createTransparencyMenu("Text Opacity", "TextColor"));
        return result;
    }

    protected JMenu createColorMenu(String title, String attribute) {
        CommandMenu menu = DrawApplication.createCommandMenu(title);
        for (int i = 0; i < ColorMap.size(); i++) {
            menu.add(new ChangeAttributeCommand(ColorMap.name(i), attribute,
                                                ColorMap.color(i)));
        }
        menu.add(new ChooseColorCommand(title, "other...", attribute,
                                        Color.class));
        return menu;
    }

    protected JMenu createTransparencyMenu(String title, String attribute) {
        CommandMenu menu = DrawApplication.createCommandMenu(title);

        for (int i = 10; i >= 0; i--) {
            menu.add(new AlphaChangeCommand(i + "0%", attribute,
                                            (int) Math.round(i * 25.5)));
        }
        return menu;
    }

    /**
     * Creates the visibility menu.
     */
    protected JMenu createVisibilityMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Visibility");
        menu.add(new ChangeAttributeCommand("visible", "Visibility",
                                            Boolean.TRUE));
        menu.add(new ChangeAttributeCommand("invisible", "Visibility",
                                            Boolean.FALSE));
        return menu;
    }

    /**
     * Creates the arrows menu.
     */
    protected JMenu createArrowMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Arrow");
        menu.add(new ChangeAttributeCommand("none", "ArrowMode",
                                            new Integer(PolyLineFigure.ARROW_TIP_NONE)));
        menu.add(new ChangeAttributeCommand("at Start", "ArrowMode",
                                            new Integer(PolyLineFigure.ARROW_TIP_START)));
        menu.add(new ChangeAttributeCommand("at End", "ArrowMode",
                                            new Integer(PolyLineFigure.ARROW_TIP_END)));
        menu.add(new ChangeAttributeCommand("at Both", "ArrowMode",
                                            new Integer(PolyLineFigure.ARROW_TIP_BOTH)));
        return menu;
    }

    /**
     * Creates the line style menu.
     */
    protected JMenu createLineStyleMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Line Style");
        menu.add(new ChangeAttributeCommand("normal", "LineStyle",
                                            AttributeFigure.LINE_STYLE_NORMAL));
        menu.add(new ChangeAttributeCommand("dotted", "LineStyle",
                                            AttributeFigure.LINE_STYLE_DOTTED));
        menu.add(new ChangeAttributeCommand("dashed", "LineStyle",
                                            AttributeFigure.LINE_STYLE_DASHED));
        menu.add(new ChangeAttributeCommand("medium dashed", "LineStyle",
                                            AttributeFigure.LINE_STYLE_MEDIUM_DASHED));
        menu.add(new ChangeAttributeCommand("long dashed", "LineStyle",
                                            AttributeFigure.LINE_STYLE_LONG_DASHED));
        menu.add(new ChangeAttributeCommand("dash-dotted", "LineStyle",
                                            AttributeFigure.LINE_STYLE_DASH_DOTTED));
        menu.add(new QueryAttributeCommand("Line style (sequence of dash/gap lengths)",
                                           "other...", "LineStyle", String.class));
        return menu;
    }

    /**
     * Creates the line shape menu.
     */
    protected JMenu createLineShapeMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Line Shape");
        menu.add(new ChangeAttributeCommand("straight", "LineShape",
                                            new Integer(PolyLineFigure.LINE_SHAPE)));
        menu.add(createBSplineMenu());
        return menu;
    }

    protected JMenu createLineWidthMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Line Width");
        for (int i = 1; i <= 10; i++) {
            menu.add(new SetLineWidthCommand(i + "", i));
        }
        return menu;
    }

    /**
     * Creates the B-Spline submenu
     */
    protected JMenu createBSplineMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("B-Spline");
        menu.add(new SplineAttributeCommand("standard", "standard", 0));
        menu.add(createBSplineSegmentsMenu());
        menu.add(createBSplineDegreeMenu());
        return menu;
    }

    protected JMenu createBSplineSegmentsMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Segments");
        for (int i = 5; i < 46; i += 5) {
            menu.add(new SplineAttributeCommand(Integer.toString(i),
                                                "BSplineSegments", i));
        }
        return menu;
    }

    protected JMenu createBSplineDegreeMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Degree");
        for (int i = 2; i < 10; i++) {
            menu.add(new SplineAttributeCommand(Integer.toString(i),
                                                "BSplineDegree", i));
        }
        return menu;
    }

    protected JMenu createArcScaleMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Round corners");
        menu.add(new ChangeAttributeCommand("scale with size",
                                            RoundRectangleFigure.ARC_SCALE_ATTR,
                                            Boolean.TRUE));
        menu.add(new ChangeAttributeCommand("fixed radius",
                                            RoundRectangleFigure.ARC_SCALE_ATTR,
                                            Boolean.FALSE));
        return menu;
    }

    /**
     * Creates the fonts menus. It installs all available fonts supported by the
     * toolkit implementation.
     */
    protected JMenu createFontMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Font");
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                            .getAvailableFontFamilyNames();
        for (int i = 0; (i < fonts.length) && (i < MAX_FONT_MENU_ENTRIES);
                     i++) {
            menu.add(new ChangeAttributeCommand(fonts[i], "FontName", fonts[i]));
        }
        menu.add(new ChooseFontCommand("Font Name", "other...", "FontName",
                                       String.class));
        return menu;
    }

    /**
     * Creates the font style menu with entries (Plain, Italic, Bold).
     */
    protected JMenu createFontStyleMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Font Style");
        menu.add(new ChangeAttributeCommand("Plain", "FontStyle",
                                            new Integer(Font.PLAIN)));
        menu.add(new ChangeAttributeCommand("Italic", "FontStyle",
                                            new Integer(Font.ITALIC)));
        menu.add(new ChangeAttributeCommand("Bold", "FontStyle",
                                            new Integer(Font.BOLD)));
        menu.add(new ChangeAttributeCommand("Underlined", "FontStyle",
                                            new Integer(ExtendedFont.UNDERLINED)));
        return menu;
    }

    /**
     * Creates the font size menu.
     */
    protected JMenu createFontSizeMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Font Size");
        int[] sizes = { 9, 10, 11, 12, 14, 18, 24, 36, 48, 72 };
        for (int i = 0; i < sizes.length; i++) {
            menu.add(new ChangeAttributeCommand(Integer.toString(sizes[i]),
                                                "FontSize",
                                                new Integer(sizes[i])));
        }
        menu.add(new QueryAttributeCommand("Font Size", "other...", "FontSize",
                                           Integer.class));
        return menu;
    }

    protected JMenu createTextAlignmentMenu() {
        CommandMenu menu = DrawApplication.createCommandMenu("Text Alignment");
        menu.add(new ChangeAttributeCommand("Left", TextFigure.ALIGN_ATTR,
                                            new Integer(TextFigure.LEFT)));
        menu.add(new ChangeAttributeCommand("Center", TextFigure.ALIGN_ATTR,
                                            new Integer(TextFigure.CENTER)));
        menu.add(new ChangeAttributeCommand("Right", TextFigure.ALIGN_ATTR,
                                            new Integer(TextFigure.RIGHT)));
        return menu;
    }
}