package de.renew.plugin.command;

import de.renew.plugin.IPlugin;
import de.renew.plugin.PluginManager;
import de.renew.plugin.PluginProperties;

import java.io.PrintStream;

import java.util.Properties;
import java.util.StringTokenizer;


/**
 * This command sets a system property.
 *
 * @author J&ouml;rn Schumacher
 */
public class SetPropertyCommand implements CLCommand {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(SetPropertyCommand.class);

    /**
     * Globally sets the value of the given property to the specified value.
     * The property and its value are specified via <code>args</code>,
     * error messages go to <code>response</code>.
     * @param args {@inheritDoc}
     * @param response {@inheritDoc}
     **/
    public void execute(String[] args, PrintStream response) {
        for (int i = 0; i < args.length; i++) {
            setProperty(args[i], response);
        }
    }

    protected void setProperty(String arg, PrintStream response) {
        StringTokenizer tokenizer = new StringTokenizer(arg, "=");
        String key;
        String value = null;
        if (!tokenizer.hasMoreTokens()) {
            response.println("Usage: set property=value [property=value ...]");
            response.println("       Don't use spaces around the = sign!");
            return;
        }
        key = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            response.println("SetPropertyCommand: no argument given for variable "
                             + arg + ", it becomes unset.");
        } else {
            while (tokenizer.hasMoreTokens()) {
                if (value == null) {
                    value = tokenizer.nextToken();
                } else {
                    value += "=" + tokenizer.nextToken();
                }
            }
        }

        // set the property in the user properties
        Properties userProps = PluginProperties.getUserProperties();
        if (value == null) {
            userProps.remove(key);

            // iterate through all plugins and remove their property
            for (IPlugin p : PluginManager.getInstance().getPlugins()) {
                PluginProperties props = p.getProperties();
                props.remove(key);
            }
        } else {
            userProps.setProperty(key, value);

            // iterate through all plugins and set their property
            for (IPlugin p : PluginManager.getInstance().getPlugins()) {
                PluginProperties props = p.getProperties();
                props.setProperty(key, value);
            }
        }
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getDescription()
     */
    public String getDescription() {
        return "sets a system property (syntax key=value)";
    }

    /**
     * @see de.renew.plugin.command.CLCommand#getArguments()
     */
    @Override
    public String getArguments() {
        return "propertyNames";
    }
}