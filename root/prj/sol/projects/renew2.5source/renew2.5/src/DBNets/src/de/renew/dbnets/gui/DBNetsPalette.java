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

public class DBNetsPalette extends Palette {

    private final Collection<Tool> tools = new ArrayList<>();

    private final GuiPlugin guiPlugin;

    private DrawingEditor drawingEditor;

    public DBNetsPalette(String title, GuiPlugin guiPlugin, DrawingEditor drawingEditor) {
        super(title);
        this.guiPlugin = guiPlugin;
        this.drawingEditor = drawingEditor;
    }

    @Override
    public Component getComponent() {
        registerDrawingEditor();
        return super.getComponent();
    }

    @Override
    public Component add(ToolButton c) {
        tools.add(c.tool());
        return super.add(c);
    }

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
