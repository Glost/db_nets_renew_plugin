package de.renew.refactoring.parse;

import org.junit.Test;

import de.renew.refactoring.match.StringMatch;

import java.util.List;


public class JNPLinkParserTest extends LinkParserTests {
    @Override
    protected LinkParser makeLinkParser() {
        return new JNPLinkParser();
    }

    @Test
    public void testFindDownlinkInCreationInscription() {
        // Creation inscriptions should not pass as downlinks
        String string = "p: new p(1);";
        List<StringMatch> matches = _parser.findDownlinks(string);
        assertEquals(0, matches.size());
    }
}