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

/**
 * The DB-Nets Renew tool plugin main class.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class DBNetsPlugin extends PluginAdapter {

    /**
     * The DB-Nets Renew tool plugin main class's constructor.
     *
     * @param location The URL of the plugin file's location.
     * @throws PluginException If the error occured while loading the plugin.
     */
    public DBNetsPlugin(URL location) throws PluginException {
        super(location);
    }

    /**
     * The DB-Nets Renew tool plugin main class's constructor.
     *
     * @param props The plugin's properties object.
     */
    public DBNetsPlugin(PluginProperties props) {
        super(props);
    }

    @Override
    public void init() {
        GuiPlugin guiPlugin = GuiPlugin.getCurrent();

        createPalette(guiPlugin);

        FormalismPlugin.getCurrent().addCompilerFactory("DB-Net Compiler", new SingleJavaDBNetCompilerFactory());

        super.init();
    }

    /**
     * Creates the db-nets plugin's UI tools palette.
     *
     * @param guiPlugin The Renew GUI plugin's instance.
     */
    private void createPalette(GuiPlugin guiPlugin) {
        DrawingEditor drawingEditor = guiPlugin.getDrawingEditor();

        Palette palette = new DBNetsPalette(guiPlugin, drawingEditor);

        PaletteHolder paletteHolder = guiPlugin.getPaletteHolder();

        DBNetTransitionFigureCreationTool dbNetTransitionTool = new DBNetTransitionFigureCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/de/renew/dbnets/gui/images/DBN_TRANSITION",
                "DB-net transition tool",
                dbNetTransitionTool
        ));

        ViewPlaceFigureCreationTool viewPlaceTool = new ViewPlaceFigureCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/de/renew/dbnets/gui/images/VIEW_PLACE",
                "View place tool",
                viewPlaceTool
        ));

        ReadArcConnectionCreationTool readArcTool = new ReadArcConnectionCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/de/renew/dbnets/gui/images/READ_ARC",
                "Read arc tool",
                readArcTool
        ));

        RollbackArcConnectionCreationTool rollbackArcTool = new RollbackArcConnectionCreationTool(drawingEditor);

        palette.add(paletteHolder.createToolButton(
                "/de/renew/dbnets/gui/images/ROLLBACK_ARC",
                "Rollback arc tool",
                rollbackArcTool
        ));

        paletteHolder.addPalette(palette);
    }
}
