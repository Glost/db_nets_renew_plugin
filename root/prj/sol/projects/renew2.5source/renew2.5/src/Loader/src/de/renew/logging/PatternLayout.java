package de.renew.logging;

import org.apache.log4j.Level;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;


/**
 * A customized log4j pattern layout.
 *
 * @author Konstantin Simon Maria Möllers
 * @since Renew 2.5
 */
@SuppressWarnings("unused")
public class PatternLayout extends org.apache.log4j.PatternLayout {

    /**
     * Format for date output
     */
    private static final SimpleDateFormat DATE_FORMAT;

    /**
     * ANSI-Format for the level
     */
    private static final HashMap<Level, CliColor> LEVELS;

    static {
        LEVELS = new HashMap<Level, CliColor>();
        LEVELS.put(Level.TRACE, CliColor.BLUE);
        LEVELS.put(Level.DEBUG, CliColor.CYAN);
        LEVELS.put(Level.INFO, CliColor.GREEN);
        LEVELS.put(Level.WARN, CliColor.YELLOW);
        LEVELS.put(Level.ERROR, CliColor.RED);
        LEVELS.put(Level.FATAL, CliColor.RED);
        DATE_FORMAT = new SimpleDateFormat("HH:mm:ss,SSS");
    }


    /**
     * Pads a string until it has the desired length. The space
     * is filled with spaces.
     *
     * @param str The string to pad.
     * @param length The desired length of the string.
     * @return A padded string.
     */
    public static String leftPad(String str, int length) {
        if (str.length() > length) {
            return str.substring(0, length);
        }

        if (str.length() < length) {
            StringBuilder stringBuilder = new StringBuilder(str);
            for (int i = str.length(); i < length; ++i) {
                stringBuilder.append(' ');
            }
            return stringBuilder.toString();
        }

        return str;
    }

    @Override
    public String format(LoggingEvent event) {
        return String.format("%s %s %s %s\n", DATE_FORMAT.format(new Date()),
                             formatLevel(event), formatLocationInfo(event),
                             formatMessage(event));
    }

    /**
     * @param event The event that gets formatted
     * @return A string representing the log level.
     */
    private String formatLevel(LoggingEvent event) {
        Level level = event.getLevel();
        String levelName = leftPad(level.toString(), 5);
        CliColor ansiConfig = LEVELS.get(level);

        if (ansiConfig == null) {
            return CliColor.color(levelName, CliColor.BOLD);
        }

        return CliColor.color(levelName, CliColor.BOLD, ansiConfig);
    }

    /**
     * @param event The event that gets formatted
     * @return A formatted location info.
     */
    private String formatLocationInfo(LoggingEvent event) {
        LocationInfo locationInfo = event.getLocationInformation();
        final String line = locationInfo.getLineNumber();
        String className = locationInfo.getClassName();
        className = className.substring(className.lastIndexOf('.') + 1);

        return "[\u001b[0;33m" + className + ":" + line + "\u001B[m]";
    }

    /**
     * @param event The event that gets formatted
     * @return Formatted message.
     */
    private String formatMessage(LoggingEvent event) {
        if (event.getMessage() == null) {
            return "";
        }

        return event.getMessage().toString();
    }
}