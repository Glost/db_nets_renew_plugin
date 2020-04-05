package de.renew.dbnets;

import CH.ifa.draw.framework.DrawingEditor;
import CH.ifa.draw.util.Palette;
import de.renew.dbnets.gui.tool.DBNetTransitionCreationTool;
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

        super.init();
    }

    // TODO: implement palette creating.
    private void createPalette(GuiPlugin guiPlugin) {
        Palette palette = new Palette("DBNetsPalette");

        DrawingEditor drawingEditor = guiPlugin.getDrawingEditor();
        DBNetTransitionCreationTool tool = new DBNetTransitionCreationTool(drawingEditor);

        PaletteHolder paletteHolder = guiPlugin.getPaletteHolder();

        palette.add(paletteHolder.createToolButton("/CH/ifa/draw/images/TEXT", "DB-net transition tool", tool));

        paletteHolder.addPalette(palette);
    }
}
