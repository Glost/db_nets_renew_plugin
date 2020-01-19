package de.renew.refactoring.parse;

import de.renew.formalism.java.JavaNetParser;
import de.renew.formalism.java.JavaNetParserConstants;
import de.renew.formalism.java.JavaNetParserTokenManager;
import de.renew.formalism.java.ParseException;
import de.renew.formalism.java.ParsedDeclarationNode;
import de.renew.formalism.java.Token;

import de.renew.net.TransitionInscription;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.Collection;
import java.util.Collections;


/**
 * Provides static methods to assist refactoring parsers that use {@link
 * JavaNetParser}.
 *
 * @author 2mfriedr
 */
public class JNPParser {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(JNPParser.class);

    /**
     * Should not be instantiated
     */
    private JNPParser() {
    }

    /**
     * @see JNPParser#netParser(String, ParsedDeclarationNode)
     */
    public static JavaNetParser netParser(final String input) {
        return netParser(input, new ParsedDeclarationNode());
    }

    /**
     * Returns a {@link JavaNetParser} object with the refactoring flag set to
     * {@code true}, the specified declaration node, and the specified input
     * string. If the declaration node is not needed or does not exist, use
     * {@link #netParser(String)}.
     *
     * @see JavaNetParser, ParsedDeclarationNode
     * @param input the input string
     * @param declarationNode the declaration node
     * @return a net parser
     */
    public static JavaNetParser netParser(final String input,
                                          final ParsedDeclarationNode declarationNode) {
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        JavaNetParser netParser = new JavaNetParser(inputStream);
        netParser.refactoring = true;
        netParser.setDeclarationNode(declarationNode);
        return netParser;
    }

    /**
     * Returns a token manager for the specified input string. This method is
     * useful if the tokens and their kinds need to be examined.
     *
     * @param input the input string
     * @return the token manager
     */
    public static JavaNetParserTokenManager tokenManager(final JavaNetParser netParser) {
        return netParser.token_source;
    }

    /**
     * Checks if the input string contains only one token of kind {@code
     * IDENTIFIER}. This method is useful to check if an input string is i.e.
     * a valid variable or channel name.
     *
     * @param input the input string
     * @return {@code true}, if the input string is an identifier, otherwise
     * {@code false}
     */
    public static boolean isIdentifier(final JavaNetParser netParser) {
        JavaNetParserTokenManager tokenManager = tokenManager(netParser);
        Token token = tokenManager.getNextToken();
        boolean isOnlyToken = tokenManager.getNextToken().kind == JavaNetParserConstants.EOF;
        return (isOnlyToken && token.kind == JavaNetParserConstants.IDENTIFIER);
    }

    /**
     * Tries to parse the input as a declaration node. If there is a parse
     * error, {@code null} is returned.
     *
     * @param input the input string
     * @return the parsed declaration node
     */
    public static ParsedDeclarationNode declarationNode(final String input) {
        try {
            return netParser(input).DeclarationNode();
        } catch (ParseException e) {
            logger.debug("Declaration node could not be parsed: " + input
                         + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * Tries to parse the input as a transition inscription. If there is a
     * parse error, an empty list is returned.
     *
     * @param input the input string
     * @return a collection of transition inscriptions
     */
    public static Collection<TransitionInscription> transitionInscriptions(final JavaNetParser netParser) {
        try {
            return netParser.TransitionInscription(false, null);
        } catch (ParseException e) {
            logger.debug(e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
    * Tries to parse the input as a transition inscription and returns only the
    * first inscription.
    *
    * @param input the input string
    * @return the first transition inscription
    */
    public static TransitionInscription firstInscription(final JavaNetParser netParser) {
        for (TransitionInscription inscription : JNPParser
                 .transitionInscriptions(netParser)) {
            return inscription;
        }
        return null;
    }
}