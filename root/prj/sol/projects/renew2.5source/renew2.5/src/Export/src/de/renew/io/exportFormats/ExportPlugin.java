/**
 *
 */
package de.renew.io.exportFormats;

import org.freehep.graphicsio.PageConstants;

import CH.ifa.draw.DrawPlugin;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginAdapter;
import de.renew.plugin.PluginException;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.net.URL;

import java.util.Iterator;


/**
 * @author Benjamin Schleinzer
 *
 */
public class ExportPlugin extends PluginAdapter {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ExportPlugin.class);
    public static String NO_EMEDED_FONTS_PROPERTY = "de.renew.io.exportFormats.svg-no-embeded-fonts";
    private EPSExportFormat epsExport;
    private PDFExportFormat pdfExport;
    private SVGExportFormat svgExport;
    private PNGExportFormat pngExport;
    public static final String PAGE_SIZE_PROP_NAME = "de.renew.io.export.pageSize";
    public static final String PAGE_ORIENTATION_PROP_NAME = "de.renew.io.export.pageOrientation";
    public static final String PAGE_MARGINS_PROP_NAME = "de.renew.io.export.pageMargins";
    public static final String EPS_FONT_HANDLING_PROP_NAME = "de.renew.io.export.epsFontHandling";
    public static final String EPS_FONT_HANDLING_EMBED = "embed";
    public static final String EPS_FONT_HANDLING_NONE = "none";
    public static final String EPS_FONT_HANDLING_SHAPES = "shapes";
    public static final String[] EPS_FONT_HANDLING_PROP_VALUES = new String[] { EPS_FONT_HANDLING_EMBED, EPS_FONT_HANDLING_NONE, EPS_FONT_HANDLING_SHAPES };
    private static final String EPS_TRANSPARENCY_PROP_NAME = "de.renew.io.export.eps-transparency";
    public static final String BOUNDING_BOX_PAGE_SIZE = "BoundingBox";

    /**
     * @param location
     * @throws PluginException
     */
    public ExportPlugin(URL location) throws PluginException {
        super(location);
    }

    /**
     * @param props
     */
    public ExportPlugin(PluginProperties props) {
        super(props);
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.PluginAdapter#cleanup()
     */
    public boolean cleanup() {
        DrawPlugin current = DrawPlugin.getCurrent();
        if (current == null) {
            return true;
        }
        current.getExportHolder().removeExportFormat(epsExport);
        current.getExportHolder().removeExportFormat(pdfExport);
        current.getExportHolder().removeExportFormat(svgExport);
        current.getExportHolder().removeExportFormat(pngExport);
        PluginManager.getInstance().removeCLCommand("ex");
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.renew.plugin.PluginAdapter#getAlias()
     */
    public String getAlias() {
        return "export";
    }

    /* (non-Javadoc)
     * @see de.renew.plugin.PluginAdapter#init()
     */
    public void init() {
        DrawPlugin current = DrawPlugin.getCurrent();
        if (current == null) {
            return;
        }
        epsExport = new EPSExportFormat();
        pdfExport = new PDFExportFormat();
        svgExport = new SVGExportFormat();
        pngExport = new PNGExportFormat();
        current.getExportHolder().addExportFormat(epsExport);
        current.getExportHolder().addExportFormat(pdfExport);
        current.getExportHolder().addExportFormat(svgExport);
        current.getExportHolder().addExportFormat(pngExport);
        PluginManager.getInstance().addCLCommand("ex", new ExportClCommand());
    }

    public static ExportPlugin getCurrent() {
        // Iterator it = PluginManager.getInstance().getPlugins().iterator();
        Iterator<IPlugin> it = PluginManager.getInstance()
                                            .getPluginsProviding("de.renew.io.export")
                                            .iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof ExportPlugin) {
                return (ExportPlugin) o;
            }
        }
        return null;
    }

    public String getPageSize() {
        String result = ExportPlugin.BOUNDING_BOX_PAGE_SIZE;
        String userValue = getProperties()
                               .getProperty(ExportPlugin.PAGE_SIZE_PROP_NAME,
                                            PageConstants.A4);
        logger.debug("Page Size user = " + userValue);
        for (String s : PageConstants.getSizeList()) {
            if (s.equalsIgnoreCase(userValue)) {
                result = s;
                getProperties().setProperty(ExportPlugin.PAGE_SIZE_PROP_NAME, s);
                break;
            }
        }
        if (userValue.equalsIgnoreCase(ExportPlugin.BOUNDING_BOX_PAGE_SIZE)) {
            result = ExportPlugin.BOUNDING_BOX_PAGE_SIZE;
            getProperties()
                .setProperty(ExportPlugin.PAGE_SIZE_PROP_NAME,
                             ExportPlugin.BOUNDING_BOX_PAGE_SIZE);
        }
        logger.debug("Page Size = " + result);
        return result;
    }

    public String getPageOrientation() {
        String result = PageConstants.PORTRAIT;
        String userValue = getProperties()
                               .getProperty(ExportPlugin.PAGE_ORIENTATION_PROP_NAME,
                                            PageConstants.PORTRAIT);
        logger.debug("Page orientation user = " + userValue);
        if (userValue.equalsIgnoreCase(PageConstants.PORTRAIT)) {
            result = PageConstants.PORTRAIT;
            getProperties()
                .setProperty(ExportPlugin.PAGE_ORIENTATION_PROP_NAME, result);
        } else if (userValue.equalsIgnoreCase(PageConstants.LANDSCAPE)) {
            result = PageConstants.LANDSCAPE;
            getProperties()
                .setProperty(ExportPlugin.PAGE_ORIENTATION_PROP_NAME, result);
        }
        logger.debug("Page orientation = " + result);
        return result;
    }

    public String getEpsFontHandling() {
        String result = EPS_FONT_HANDLING_SHAPES;
        String userValue = getProperties()
                               .getProperty(EPS_FONT_HANDLING_PROP_NAME,
                                            EPS_FONT_HANDLING_SHAPES);
        logger.debug("EPS font handling user = " + userValue);
        for (String s : EPS_FONT_HANDLING_PROP_VALUES) {
            if (s.equalsIgnoreCase(userValue)) {
                result = s;
                getProperties().setProperty(EPS_FONT_HANDLING_PROP_NAME, result);
                break;
            }
        }
        logger.debug("EPS font handling = " + result);
        return result;
    }

    public boolean getEpsTransparency() {
        boolean result = getProperties()
                             .getBoolProperty(EPS_TRANSPARENCY_PROP_NAME);
        logger.debug("EPS tansparency user = " + result);
        return result;
    }
}