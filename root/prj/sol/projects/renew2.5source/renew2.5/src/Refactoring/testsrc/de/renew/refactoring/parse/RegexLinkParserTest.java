package de.renew.refactoring.parse;

import org.junit.Test;

import java.util.regex.Pattern;


public class RegexLinkParserTest extends LinkParserTests {
    @Override
    protected LinkParser makeLinkParser() {
        return new RegexLinkParser();
    }

    @Test
    public void testDownlinkPattern() {
        RegexLinkParser parser = (RegexLinkParser) _parser;

        Pattern pattern = parser.downlinkPatternForChannel("channel", 0);
        assertTrue(pattern.matcher("net:channel()").find());
        assertTrue(pattern.matcher("net : channel ( )").find());

        assertFalse(pattern.matcher("net:channel(a)").find());
        assertFalse(pattern.matcher("net:channel([a,b])").find());
    }

    @Test
    public void testDownlinkPatternWithParameters() {
        RegexLinkParser parser = (RegexLinkParser) _parser;

        Pattern pattern = parser.downlinkPatternForChannel("channel", 2);
        assertTrue(pattern.matcher("net:channel(a,b)").find());
        assertTrue(pattern.matcher("net:channel([a,b],c)").find());
        assertTrue(pattern.matcher("net : channel ( a , b )").find());

        assertFalse(pattern.matcher("net:channel()").find());
        assertFalse(pattern.matcher("net:channel(a)").find());
        assertFalse(pattern.matcher("net:channel([a,b])").find());
        assertFalse(pattern.matcher(":channel(a,b)").find());
    }
}