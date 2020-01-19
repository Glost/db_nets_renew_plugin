package de.renew.plugin;

import de.renew.plugin.command.CLCommand;

import java.io.PrintStream;

import java.net.URL;
import java.net.URLClassLoader;


/**
 * This ClassLoader is responsible for loading the plugin classes.
 * It allows adding classpath items dynamically.
 * It provides a Singleton access interface to ensure that only one
 * PluginClassLoader is in use.
 */
public class PluginClassLoader extends URLClassLoader {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(PluginClassLoader.class);

    public PluginClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    /**
     * Add the given URL to the list where classes are sought for.
     */
    public void addURL(URL url) {
        logger.debug(this + " adding URL " + url);
        super.addURL(url);
    }

    class PackageCountCommand implements CLCommand {
        /*
         * @see de.renew.plugin.command.CLCommand#execute(java.lang.String[])
         */
        public void execute(String[] args, PrintStream response) {
            String prefix = "";
            if (args.length != 0) {
                prefix = args[0];
            }
            Package[] packages = Package.getPackages();
            int count = 0;
            for (int i = 0; i < packages.length; i++) {
                if (packages[i].getName().startsWith(prefix)) {
                    response.println(packages[i]);
                    count++;
                }
            }
            response.println(count + " packages found.");
        }

        /*
         * @see de.renew.plugin.command.CLCommand#getDescription()
         */
        public String getDescription() {
            return "prints the number of packages in the class loader";
        }

        /**
         * @see de.renew.plugin.command.CLCommand#getArguments()
         */
        @Override
        public String getArguments() {
            return null;
        }
    }
}