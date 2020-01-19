package de.renew.plugin.command;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


public class ScriptCommandTest {
    private ScriptCommand cmd;

    @Before
    public void prepare() {
        cmd = new ScriptCommand();
    }

    @Test
    public void testSplitStringNoQuotes() {
        List<String> result = cmd.splitString("this is a sample");
        assertEquals(4, result.size());
        assertEquals("this", result.get(0));
        assertEquals("is", result.get(1));
        assertEquals("a", result.get(2));
        assertEquals("sample", result.get(3));
    }

    @Test
    public void testSplitStringWithQuotes() {
        List<String> result = cmd.splitString("this is \"a sample\"");
        assertEquals("Result is " + result, 3, result.size());
        assertEquals("this", result.get(0));
        assertEquals("is", result.get(1));
        assertEquals("a sample", result.get(2));
    }

    @Test
    public void testSplitStringMixed() {
        List<String> result = cmd.splitString("this is \"a sample\" test");
        assertEquals("Result is " + result, 4, result.size());
        assertEquals("this", result.get(0));
        assertEquals("is", result.get(1));
        assertEquals("a sample", result.get(2));
        assertEquals("test", result.get(3));
    }
}