package de.renew.dbnets;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.util.Palette;
import de.renew.dbnets.gui.DBNetsPalette;
import de.renew.dbnets.gui.tool.DBNetTransitionFigureCreationTool;
import de.renew.dbnets.gui.tool.ReadArcConnectionCreationTool;
import de.renew.dbnets.gui.tool.RollbackArcConnectionCreationTool;
import de.renew.dbnets.gui.tool.ViewPlaceFigureCreationTool;
import de.renew.dbnets.shadow.SingleJavaDBNetCompilerFactory;
import de.renew.formalism.FormalismPlugin;
import de.renew.gui.GuiPlugin;
import de.renew.gui.PaletteHolder;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginProperties;

import java.net.URL;

public class DBNets extends PluginAdapter {

    public DBNets(URL location) throws PluginException {
        super(location);
    }

    public DBNets(PluginProperties props) {
        super(props);
    }

    @Override
    public void init() {
        logger.info("Initializing DB-Nets Plugin..."); // TODO: remove this line, it is only for the debugging purposes.

        GuiPlugin guiPlugin = GuiPlugin.getCurrent();

        createPalette(guiPlugin);

        FormalismPlugin.getCurrent().addCompilerFactory("DB-Net Compiler", new SingleJavaDBNetCompilerFactory());

        super.init();
    }

    // TODO: implement palette creating.
    private void createPalette(GuiPlugin guiPlugin) {
        DrawingEditor drawingEditor = guiPlugin.getDrawingEditor();

        Palette palette = new DBNetsPalette("DBNetsPalette", guiPlugin, drawingEditor);

        PaletteHolder paletteHolder = guiPlugin.getPaletteHolder();

        DBNetTransitionFigureCreationTool dbNetTransitionTool = new DBNetTransitionFigureCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/CH/ifa/draw/images/TEXT",
                "DB-net transition tool",
                dbNetTransitionTool
        ));

        ViewPlaceFigureCreationTool viewPlaceTool = new ViewPlaceFigureCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/CH/ifa/draw/images/TEXT",
                "View place tool",
                viewPlaceTool
        ));

        ReadArcConnectionCreationTool readArcTool = new ReadArcConnectionCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/CH/ifa/draw/images/TEXT",
                "Read arc tool",
                readArcTool
        ));

        RollbackArcConnectionCreationTool rollbackArcTool = new RollbackArcConnectionCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/CH/ifa/draw/images/TEXT",
                "Rollback arc tool",
                rollbackArcTool
        ));

        paletteHolder.addPalette(palette);
    }
}
