package de.renew.plugin.command;

import de.renew.plugin.CollectionLister;
import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.PrintStream;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * This class shows the properties of a specified plugin.
 *
 * @author J&ouml;rn Schumacher
 */
public class InfoCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(InfoCommand.class);

    /**
     * Prints all properties of a plugin. The plugin is specified via
     * <code>args</code>, the output goes to <code>response</code>.
     * @param args {@inheritDoc}
     * @param response {@inheritDoc}
     **/
    public void execute(String[] args, PrintStream response) {
        // get Plugin name from input
        String pluginName = CollectionLister.toString(args, " ");
        if (args.length > 0) {
            if ("-c".equals(args[0]) || "c".equals(args[0])
                        || "--comments".equals(args[0])
                        || "comments".equals(args[0])) {
                response.println("Please use list command instead!");
            }
        }
        IPlugin found = PluginManager.getInstance().getPluginByName(pluginName);
        if (found == null) {
            logger.debug("InfoCommand: did not find Plugin with name "
                         + pluginName);
            return;
        }
        try {
            PluginProperties props = found.getProperties();
            response.println(renderProperties(props));
        } catch (ArrayIndexOutOfBoundsException e) {
        } catch (NumberFormatException e) {
            response.println("cleanup canceled.");
        } catch (Exception e) {
            response.println("cleanup canceled: " + e + "; " + e.getMessage());
            logger.error(e.getMessage(), e);
        }
    }

    public static String renderProperties(PluginProperties props) {
        StringBuffer result = new StringBuffer();
        result.append("Properties of plugin ");
        result.append(props.getName());
        result.append("\n\tloaded from ");
        result.append(props.getURL());
        result.append("\n\t");
        String mainClass = props.getMainClass();
        if ("".equals(mainClass)) {
            result.append("no main class");
        } else {
            result.append("main class is ");
            result.append(mainClass);
        }
        result.append("\n\tprovides ");
        result.append(CollectionLister.toString(props.getProvisions()));
        result.append("\n\trequires ");
        result.append(CollectionLister.toString(props.getRequirements()));
        result.append("\n\tversion information \n\t\t");
        result.append(props.getVersion());
        result.append("\n\t\t");
        result.append(props.getVersionDate());
        result.append("\n\t\t");
        result.append(props.getVersionText());
        result.append("\n");
        List<String> pNames = new Vector<String>();
        pNames.addAll(props.getKeys());
        StringBuffer other = new StringBuffer();
        Collections.sort(pNames);
        Iterator<String> propNames = pNames.iterator();
        while (propNames.hasNext()) {
            String currentProp = propNames.next();
            if (props.isKnownProperty(currentProp)) {
                other.append("\t");
                other.append(currentProp);
                other.append("=");
                other.append(props.getProperty(currentProp));
                other.append("\n");
            }
        }
        if (other.length() > 0) {
            result.append("Other properties:\n");
            result.append(other);
        }

        pNames = new Vector<String>(props.getUnsetProperties());
        StringBuffer unset = new StringBuffer();
        Collections.sort(pNames);
        propNames = pNames.iterator();
        while (propNames.hasNext()) {
            String currentProp = propNames.next();
            if (props.isKnownProperty(currentProp)) {
                unset.append("\t");
                unset.append(currentProp);
                unset.append("=");
                unset.append(props.getProperty(currentProp));
                unset.append("\n");
            }
        }
        if (unset.length() > 0) {
            result.append("Unset properties:\n");
            result.append(unset);
        }


        return result.toString();
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return "view properties of a loaded plugin.";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "pluginNames";
    }
}