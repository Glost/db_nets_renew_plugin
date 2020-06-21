package de.renew.dbnets.gui;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.framework.Tool;
import CH.ifa.draw.standard.NullDrawingEditor;
import CH.ifa.draw.standard.ToolButton;
import CH.ifa.draw.util.Palette;
import de.renew.dbnets.gui.tool.DrawingEditorSettable;
import de.renew.gui.GuiPlugin;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * The db-nets plugin's UI tools palette.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class DBNetsPalette extends Palette {

    /**
     * The db-nets plugin's UI tools.
     */
    private final Collection<Tool> tools = new ArrayList<>();

    /**
     * The Renew GUI plugin's instance.
     */
    private final GuiPlugin guiPlugin;

    /**
     * The drawing editor's instance.
     */
    private DrawingEditor drawingEditor;

    /**
     * The db-nets plugin's UI tools palette's constructor.
     *
     * @param guiPlugin The Renew GUI plugin's instance.
     * @param drawingEditor The drawing editor's instance.
     */
    public DBNetsPalette(GuiPlugin guiPlugin, DrawingEditor drawingEditor) {
        super(DBNetsPalette.class.getSimpleName());
        this.guiPlugin = guiPlugin;
        this.drawingEditor = drawingEditor;
    }

    /**
     * Returns the toolbar.
     *
     * @return The toolbar.
     */
    @Override
    public Component getComponent() {
        registerDrawingEditor();
        return super.getComponent();
    }

    /**
     * Adds the db-nets plugin's UI tool to the palette.
     *
     * @param c The db-nets plugin's UI tool.
     * @return The tool's component instance.
     */
    @Override
    public Component add(ToolButton c) {
        tools.add(c.tool());
        return super.add(c);
    }

    /**
     * Registers the drawing editor in the all the created db-nets plugin's UI tools.
     */
    private void registerDrawingEditor() {
        DrawingEditor initializedDrawingEditor;

        if (drawingEditor instanceof NullDrawingEditor &&
                !((initializedDrawingEditor = Optional.ofNullable(guiPlugin.getDrawingEditor())
                        .orElse(NullDrawingEditor.INSTANCE)
                ) instanceof NullDrawingEditor)) {
            drawingEditor = initializedDrawingEditor;
            tools.forEach(tool -> ((DrawingEditorSettable) tool).setDrawingEditor(drawingEditor));
        }
    }
}
