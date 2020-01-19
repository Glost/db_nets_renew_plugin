/*
 * Created on Oct 25, 2004
 *
 */
package de.renew.gui.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;


/**
 * Creates new instances of the registered Appenders, which
 * the user can add to the loggers.
 *
 * @author Sven Offermann
 *
 */
public class AppenderFactory {
    private static final Logger logger = Logger.getLogger(AppenderFactory.class);
    private static final String APPENDER_TYPE_PREFIX = "appender.type.";
    private static final String APPENDER_TYPE_CLASS = "class";
    private static final String APPENDER_TYPE_BUILDER = "builder";
    private static AppenderFactory factory = null;
    private Map<String, AppenderBuilder> builders = new Hashtable<String, AppenderBuilder>();

    private AppenderFactory() {
        Properties props = LoggingGuiPlugin.getCurrent().getProperties();

        Enumeration<Object> enumeration = props.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();

            if (key.startsWith(APPENDER_TYPE_PREFIX)) {
                if (key.length() > APPENDER_TYPE_PREFIX.length()) {
                    String typeName = key.substring(APPENDER_TYPE_PREFIX.length());

                    if (typeName.indexOf(".") >= 0) {
                        typeName = typeName.substring(0, typeName.indexOf("."));
                    }

                    if (!this.builders.containsKey(typeName)) {
                        String builderName = props.getProperty(APPENDER_TYPE_PREFIX
                                                               + typeName + "."
                                                               + APPENDER_TYPE_BUILDER);

                        AppenderBuilder builder = null;
                        if (builderName != null) {
                            try {
                                Class<?> clazz = getClass().getClassLoader()
                                                     .loadClass(builderName);

                                builder = (AppenderBuilder) clazz.newInstance();
                            } catch (Exception e) {
                                logger.error("Can't load instance of builder class for appender type "
                                             + typeName);
                                logger.error(e.getMessage(), e);
                            }
                        }

                        if (builder == null) {
                            String className = props.getProperty(APPENDER_TYPE_PREFIX
                                                                 + typeName
                                                                 + "."
                                                                 + APPENDER_TYPE_CLASS);

                            if (className != null) {
                                try {
                                    builder = new DefaultAppenderBuilder(className);
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        }

                        if ((builder != null)
                                    && (!this.builders.containsKey(typeName))) {
                            this.builders.put(typeName, builder);
                        }
                    }
                }
            }
        }
    }

    public static AppenderFactory getInstance() {
        if (factory == null) {
            factory = new AppenderFactory();
        }

        return factory;
    }

    public Appender createNewAppender(String appenderTypeName)
            throws InstantiationException {
        Appender appender = null;

        AppenderBuilder builder = builders.get(appenderTypeName);

        if (builder != null) {
            appender = builder.newInstance();
        }

        return appender;
    }

    public String[] getAllAppenderTypes() {
        return this.builders.keySet().toArray(new String[] {  });
    }

    private class DefaultAppenderBuilder implements AppenderBuilder {
        private final Class<?> clazz;

        protected DefaultAppenderBuilder(String appenderClassName)
                throws ClassNotFoundException {
            getClass();
            this.clazz = Class.forName(appenderClassName);
        }

        public Appender newInstance() throws InstantiationException {
            try {
                return (Appender) clazz.newInstance();
            } catch (InstantiationException e) {
                throw e;
            } catch (Exception e) {
                throw new InstantiationException();
            }
        }
    }
}