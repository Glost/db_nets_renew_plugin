package de.renew.refactoring.parse;

import de.renew.expression.AggregateExpression;
import de.renew.expression.Expression;
import de.renew.expression.LocalVariable;
import de.renew.expression.VariableExpression;

import de.renew.net.TransitionInscription;
import de.renew.net.UplinkInscription;
import de.renew.net.inscription.DownlinkInscription;

import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.util.StringHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * LinkParser implementation using JavaNetParser.
 *
 * @author 2mfriedr
 */
public class JNPLinkParser implements LinkParser {
    @Override
    public boolean isValidChannelName(final String name) {
        return JNPParser.isIdentifier(JNPParser.netParser(name));
    }

    @Override
    public boolean containsUplink(final String string) {
        return containsInscriptionOfType(UplinkInscription.class, string);
    }

    @Override
    public boolean containsDownlink(final String string) {
        return containsInscriptionOfType(DownlinkInscription.class, string);
    }

    /**
     * Generalization of containsUplink(String) and containsDownlink(String).
     *
     * @param inscriptionType an inscription type
     * @param string the string to be checked
     * @return {@code true}, if the string contains an inscription of the
     * specified type, otherwise {@code false}.
     */
    private static boolean containsInscriptionOfType(final Class<?> inscriptionType,
                                                     final String string) {
        for (TransitionInscription inscription : JNPParser
                 .transitionInscriptions(JNPParser.netParser(string))) {
            if (inscriptionType.isInstance(inscription)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public StringMatch findChannelName(final String link) {
        try {
            LinkInscription inscription = new LinkInscription(JNPParser
                                              .firstInscription(JNPParser
                                              .netParser(link)));
            return StringHelper.makeStringMatch(link,
                                                inscription.getNameBeginLine(),
                                                inscription.getNameBeginColumn(),
                                                inscription.getNameEndLine(),
                                                inscription.getNameEndColumn());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public int findParameterCount(final String link) {
        return findParameterCount(JNPParser.firstInscription(JNPParser.netParser(link)));
    }

    /**
     * Finds the parameter count in an {@link UplinkInscription} or
     * {@link DownlinkInscription} object.
     *
     * @param inscription the inscription
     * @return the parameter count
     */
    private static int findParameterCount(final Object inscription) {
        Expression params = new LinkInscription(inscription).getParams();
        try {
            return ((AggregateExpression) params).getExpressions().length;
        } catch (ClassCastException e) {
            return -1; // shouldn't happen if the input was correct
        }
    }

    @Override
    public boolean isDownlinkToThis(final String link) {
        try {
            DownlinkInscription downlinkInscription = (DownlinkInscription) JNPParser
                                                      .firstInscription(JNPParser
                                                                        .netParser(link));
            VariableExpression variableExpression = (VariableExpression) downlinkInscription.callee;
            LocalVariable variable = variableExpression.getVariable();
            return variable.name.equals("this");
        } catch (ClassCastException e) {
            return false;
        }
    }

    private static String CHANNEL_NAME_ALLOW_ALL = "CHANNEL_NAME_ALLOW_ALL";
    private static int PARAMETER_COUNT_ALLOW_ALL = -1;

    @Override
    public StringMatch findUplink(final String string) {
        return findUplink(string, CHANNEL_NAME_ALLOW_ALL,
                          PARAMETER_COUNT_ALLOW_ALL);
    }

    @Override
    public StringMatch findUplink(final String string, final String channel,
                                  final int parameterCount) {
        UplinkInscription uplink = null;
        for (TransitionInscription inscription : JNPParser
                 .transitionInscriptions(JNPParser.netParser(string))) {
            if (inscription instanceof UplinkInscription) {
                uplink = (UplinkInscription) inscription;

                if (matchesChannelName(uplink, channel)
                            && matchesParameterCount(uplink, parameterCount)) {
                    return makeStringMatch(string, uplink);
                }
                return null; // only look at the first uplink 
            }
        }
        return null;
    }

    @Override
    public List<StringMatch> findDownlinks(final String string) {
        return findDownlinks(string, CHANNEL_NAME_ALLOW_ALL,
                             PARAMETER_COUNT_ALLOW_ALL);
    }

    @Override
    public List<StringMatch> findDownlinks(final String string,
                                           final String channel,
                                           final int parameterCount) {
        List<StringMatch> downlinks = new ArrayList<StringMatch>();

        for (TransitionInscription inscription : JNPParser
                 .transitionInscriptions(JNPParser.netParser(string))) {
            if (inscription instanceof DownlinkInscription) {
                DownlinkInscription downlink = (DownlinkInscription) inscription;

                if (matchesChannelName(downlink, channel)
                            && matchesParameterCount(downlink, parameterCount)) {
                    if (downlink.name.equals("new")) {
                        break;
                    }
                    downlinks.add(makeStringMatch(string, downlink));
                }
            }
        }
        return downlinks;
    }

    /**
     * Makes a string match object from an {@link UplinkInscription} or {@link
     * DownlinkInscription}.
     *
     * @param string the string
     * @param uplinkOrDownlink the uplink or downlink
     * @return a string match object
     */
    private static StringMatch makeStringMatch(final String string,
                                               final Object uplinkOrDownlink) {
        LinkInscription inscription = new LinkInscription(uplinkOrDownlink);
        return StringHelper.makeStringMatch(string, inscription.getBeginLine(),
                                            inscription.getBeginColumn(),
                                            inscription.getEndLine(),
                                            inscription.getEndColumn());
    }

    /**
     * Checks if an {@link UplinkInscription} or {@link DownlinkInscription}
     * matches the specified channel name. Always returns {@code true} if the
     * channel name is {@link #CHANNEL_NAME_ALLOW_ALL}.
     *
     * @param uplinkOrDownlink the uplink or downlink
     * @param channel the channel name
     * @return {@code true} if the inscription matches the channel name,
     * otherwise {@code false}
     */
    private static boolean matchesChannelName(final Object uplinkOrDownlink,
                                              final String channel) {
        if (channel.equals(CHANNEL_NAME_ALLOW_ALL)) {
            return true;
        }
        LinkInscription inscription = new LinkInscription(uplinkOrDownlink);
        return inscription.getName().equals(channel);
    }

    /**
     * Checks if an {@link UplinkInscription} or {@link DownlinkInscription}
     * matches the specified parameter count. Always returns {@code true} if the
     * parameter count is {@link #PARAMETER_COUNT_ALLOW_ALL}.
     *
     * @param uplinkOrDownlink the uplink or downlink
     * @param parameterCount the parameter count
     * @return {@code true} if the inscription matches the parameter count,
     * otherwise {@code false}
     */
    private static boolean matchesParameterCount(final Object uplinkOrDownlink,
                                                 final int parameterCount) {
        if (parameterCount == PARAMETER_COUNT_ALLOW_ALL) {
            return true;
        }
        return findParameterCount(uplinkOrDownlink) == parameterCount;
    }
}