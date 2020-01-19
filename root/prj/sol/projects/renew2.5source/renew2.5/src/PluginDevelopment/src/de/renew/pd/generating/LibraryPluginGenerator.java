package de.renew.pd.generating;

import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;

import java.util.Properties;


public class LibraryPluginGenerator extends StandardPluginGenerator {
    private static String LIBRARY_TEMPLATE_FOLDER = "de/renew/pd/templates/library/";
    private static final String LIB_PATH = "lib";

    public LibraryPluginGenerator() {
        super();
        this.templateFolder = LIBRARY_TEMPLATE_FOLDER;
    }

    @Override
    protected void createFilesFromTemplates(String rootDir, Properties props) {
        try {
            Context context = getContextFromProperties(props);
            context.put("generic-description",
                        "This is the generated desctiption for this plugin.");
            context.put("generic-name",
                        props.getProperty("appName").toLowerCase());
            context.put("smallAppName",
                        props.getProperty("appName").toLowerCase());

            // copy the build.xml file
            createFile(ve, context, rootDir, "", "build.xml", null);

            // copy the plugin.cfg file
            createFile(ve, context, rootDir, "etc", "plugin.cfg", null);
        } catch (ResourceNotFoundException e) {
            logger.error(e.getMessage());
            logger.debug(e.getStackTrace());
        } catch (ParseErrorException e) {
            logger.error(e.getMessage());
            logger.debug(e.getStackTrace());
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug(e.getStackTrace());
        }
    }

    @Override
    protected void createFolders(String rootDir, String appName) {
        File directory = new File(rootDir + File.separator + appName
                                  + File.separator + LIB_PATH);
        directory.mkdirs();


    }
}