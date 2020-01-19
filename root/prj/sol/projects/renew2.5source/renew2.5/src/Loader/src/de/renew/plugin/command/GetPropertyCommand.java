package de.renew.plugin.command;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;


/**
 * This command is used to query a user property value as returned
 * by the PluginProperties.getUserProperties() static method.
 *
 * @author J&ouml;rn Schumacher
 */
public class GetPropertyCommand implements CLCommand {

    /**
     * Prints the value of a property as it is known to any plugin.
     * The property is specified via <code>args</code>, the output goes to
     * <code>response</code>.
     * @param args {@inheritDoc}
     * @param response {@inheritDoc}
     **/
    public void execute(String[] args, PrintStream response) {
        if (args.length > 0) {
            if ("-a".equals(args[0])) {
                showAllPropertyNames(response);
            } else if ("-A".equals(args[0])) {
                showAllProperties(response);
            } else {
                for (int i = 0; i < args.length; i++) {
                    getProperty(args[i], response);
                }
            }
        }
    }

    private void showAllPropertyNames(PrintStream response) {
        ArrayList<String> propertynames = getPropertyNames();
        for (String s : propertynames) {
            response.append(s + "\n");
        }
    }

    private void showAllProperties(PrintStream response) {
        ArrayList<String> propertynames = getPropertyNames();
        for (String s : propertynames) {
            String[] args = { s };
            execute(args, response);
        }
    }

    /**
     * @return
     */
    static public ArrayList<String> getPropertyNames() {
        ArrayList<String> propertynames = new ArrayList<String>();
        for (IPlugin p : PluginManager.getInstance().getPlugins()) {
            PluginProperties props = p.getProperties();
            Enumeration<Object> en = props.keys();
            while (en.hasMoreElements()) {
                String s = (String) en.nextElement();
                if (!propertynames.contains(s)) {
                    propertynames.add(s);
                }
            }
        }
        Collections.sort(propertynames);
        return propertynames;
    }

    protected void getProperty(String arg, PrintStream response) {
        String globalValue = PluginProperties.getUserProperties()
                                             .getProperty(arg);
        if (globalValue != null) {
            response.println(arg + " = " + globalValue
                             + " (global value of property)");
        }
        boolean noMatches = true;
        for (IPlugin p : PluginManager.getInstance().getPlugins()) {
            PluginProperties props = p.getProperties();
            String localValue = props.getProperty(arg);
            if (props.isKnownProperty(arg)) {
                if (localValue == null) {
                    response.println("Property " + arg + " is known to plugin "
                                     + p.getName() + ", but not set.");
                } else {
                    response.println(arg + " = " + localValue
                                     + " (known property in plugin "
                                     + p.getName() + ")");
                }
                noMatches = false;
            } else if (!(localValue == globalValue
                               || (localValue != null
                                          && localValue.equals(globalValue)))) {
                if (localValue == null) {
                    response.println("Property " + arg
                                     + " is not set locally in plugin "
                                     + p.getName() + ".");
                } else {
                    response.println(arg + " = " + localValue
                                     + " (local value in plugin " + p.getName()
                                     + ")");
                }
                noMatches = false;
            }
        }
        if (globalValue == null && noMatches) {
            response.println("Property " + arg + " is not set.");
        }
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return "Returns the value of the given PluginProperty. "
               + "Options: [-a| -A] for a listing of all known property names / properties.";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "propertyNames";
    }
}