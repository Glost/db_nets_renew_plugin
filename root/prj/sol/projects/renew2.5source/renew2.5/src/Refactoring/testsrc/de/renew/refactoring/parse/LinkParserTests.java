package de.renew.refactoring.parse;

import junit.framework.TestCase;

import org.junit.Test;

import de.renew.refactoring.match.StringMatch;

import java.util.List;


public abstract class LinkParserTests extends TestCase {
    LinkParser _parser;

    protected abstract LinkParser makeLinkParser();

    @Override
    public void setUp() {
        _parser = makeLinkParser();
    }

    @Test
    public void testIsValidChannelName() {
        assertTrue(_parser.isValidChannelName("channel"));
        assertTrue(_parser.isValidChannelName("c"));

        // whitespace does not matter
        assertTrue(_parser.isValidChannelName(" channel"));

        assertFalse(_parser.isValidChannelName("c;"));
        assertFalse(_parser.isValidChannelName("channel c"));
        assertFalse(_parser.isValidChannelName("c:"));
    }

    @Test
    public void testContainsUplink() {
        assertTrue(_parser.containsUplink(":c()"));
        assertTrue(_parser.containsUplink(":c();"));
        assertTrue(_parser.containsUplink("net:c(); :channel()"));
        assertTrue(_parser.containsUplink(": channel ( )"));
        assertTrue(_parser.containsUplink(":c(param)"));
        assertTrue(_parser.containsUplink(":c(a,b,c, d)"));
        assertTrue(_parser.containsUplink(":c([a,b,c], d)"));

        assertFalse(_parser.containsUplink(""));
        assertFalse(_parser.containsUplink(":()"));
        assertFalse(_parser.containsUplink(": c hannel()"));
        assertFalse(_parser.containsUplink("channel()"));
        assertFalse(_parser.containsUplink("net:channel()"));
        assertFalse(_parser.containsUplink("net: channel()"));
    }

    @Test
    public void testContainsDownlink() {
        assertTrue(_parser.containsDownlink("net:c()"));
        assertTrue(_parser.containsDownlink("net:c();"));
        assertTrue(_parser.containsDownlink(":c();net:c()"));
        assertTrue(_parser.containsDownlink("net : c ()"));
        assertTrue(_parser.containsDownlink("net:c(param)"));
        assertTrue(_parser.containsDownlink("net:c(a,b,c, d)"));
        assertTrue(_parser.containsDownlink("net:c([a,b,c], d)"));

        assertFalse(_parser.containsDownlink(""));
        assertFalse(_parser.containsDownlink("net:()"));
        assertFalse(_parser.containsDownlink("net channel()"));
        assertFalse(_parser.containsDownlink(":channel()"));
    }

    @Test
    public void testFindUplink() {
        assertEquals(":c()", _parser.findUplink(":c()").match());

        assertNull(_parser.findUplink("net:uplink()"));
    }

    @Test
    public void testFindChannelName() {
        assertEquals("c", _parser.findChannelName(":c(param)").match());
        assertEquals("channel", _parser.findChannelName(" : channel ()").match());
        assertEquals("c", _parser.findChannelName("\n\n\n:c(param)").match());
    }

    @Test
    public void testFindParameterCount() {
        assertEquals(0, _parser.findParameterCount(":c()"));
        assertEquals(1, _parser.findParameterCount(":c(param)"));
        assertEquals(2, _parser.findParameterCount(":c(a,b)"));
        assertEquals(2, _parser.findParameterCount(":c(a , b)"));
        assertEquals(2, _parser.findParameterCount(":c([a, b] , c)"));
    }

    @Test
    public void testFindDownlinks() {
        String string = "test;\n net:s(a);\n net:s([b, c]); this:s(d, e)";
        List<StringMatch> matches = _parser.findDownlinks(string, "s", 1);
        assertEquals(2, matches.size());
        assertEquals("net:s(a)", matches.get(0).match().trim());

        string = "test; :uplink();";
        matches = _parser.findDownlinks(string, "uplink", 0);
        assertEquals(0, matches.size());
    }
}