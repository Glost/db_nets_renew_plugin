package de.renew.refactoring.parse;

import de.renew.refactoring.match.StringMatch;

import java.util.List;


/**
 * Interface for parsers for uplinks and downlinks.
 *
 * @author 2mfriedr
 */
public interface LinkParser {

    /**
    * Checks if the name is a valid channel name.
    *
    * @param name the name to be checked
    * @return {@code true} if the name is a valid channel name, otherwise
    * {@code false}
    */
    public boolean isValidChannelName(String name);

    /**
     * Checks if the string contains an uplink, like ":ch(x,y,z)".
     *
     * @param string the string to be checked
     * @return {@code true} if the string contains an uplink, otherwise
     * {@code false}
     */
    public boolean containsUplink(String string);

    /**
     * Checks if the string contains a downlink, like "this:ch(x,y,z)".
     *
     * @param string the string to be checked
     * @return {@code true} if the string contains a downlink, otherwise
     * {@code false}
     */
    public boolean containsDownlink(String string);

    /**
     * Finds the channel name in a string that must either contain only an
     * uplink, like ":ch(x,y,z)", or only a downlink, like "this:ch(x,y,z)".
     *
     * @param link the string to be searched.
     * @return a string match object that describes the match.
     */
    public StringMatch findChannelName(String link);

    /**
     * Finds the parameter count in a string that must contain either only
     * an uplink, like ":ch(x,y,z)", or only a downlink, like "this:ch(x,y,z)".
     *
     * @param link the string to be searched
     * @return the parameter count
     */
    public int findParameterCount(String link);

    /**
     * Checks if the net reference in a string that must contain only a
     * downlink, like "net:ch(x,y,z)", is the reserved word "this", i.e. the
     * downlink points the current net.
     *
     * @param link the string to be searched
     * @return {@code true} if the downlink is a downlink to "this",
     * otherwise {@code false}
     */
    @Deprecated
    public boolean isDownlinkToThis(String link);

    /**
     * Finds the first uplink, like ":ch(x,y,z)", in a string.
     *
     * @param string the string to be searched
     * @return a string match object that describes the match. May be null
     * if {@code string} does not contain an uplink
     */
    public StringMatch findUplink(String string);

    /**
     * Finds the first uplink, like ":ch(x,y,z)", to a channel with the
     * specified name and parameter count, in a string.
     *
     * @param string the string to be searched
     * @param channel the channel name
     * @param parameterCount the parameter count
     * @return a string match object that describes the match. May be null
     * if {@code string} does not contain an uplink to the specified channel
     */
    public StringMatch findUplink(String string, String channel,
                                  int parameterCount);

    /**
     * Returns a list of all downlinks, like "this:ch(x,y,z)", in a string.
     *
     * @param string the string to search
     * @return a list of downlinks. should be empty if no downlinks are found
     */
    public List<StringMatch> findDownlinks(String string);

    /**
     * Returns a list of all downlinks, like "this:ch(x,y,z)", to a channel
     * with a specified name and parameter count, in a string.
     *
     * @param string the string to search
     * @param channel the channel name
     * @param parameterCount the parameter count
     * @return a list of downlinks. should be empty if no downlinks are found
     */
    public List<StringMatch> findDownlinks(String string, String channel,
                                           int parameterCount);
}