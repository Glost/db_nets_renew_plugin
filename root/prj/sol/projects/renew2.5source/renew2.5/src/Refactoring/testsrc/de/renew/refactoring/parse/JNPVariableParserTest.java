package de.renew.refactoring.parse;

import junit.framework.TestCase;

import org.junit.Test;

import de.renew.refactoring.match.StringMatch;

import java.util.List;


public class JNPVariableParserTest extends TestCase {
    VariableParser _parser;

    @Override
    public void setUp() {
        _parser = new JNPVariableParser("de.renew.net.NetInstance net; String s; int i, j; boolean bool;");
    }

    @Test
    public void testContainsVariable() {
        // variable expression
        assertTrue(_parser.containsVariable("s"));

        // equals expression, contains type checking expression, constant expression
        assertTrue(_parser.containsVariable("s = \"constant\""));

        // call expression
        assertTrue(_parser.containsVariable("String.valueOf(i)"));
        assertTrue(_parser.containsVariable("s.charAt(0)"));

        // aggregate expression (tuple)
        assertTrue(_parser.containsVariable("[0,1,2,i]"));

        // composed call
        assertTrue(_parser.containsVariable("s.toString().indexOf(\"e\")"));

        assertTrue(_parser.containsVariable(":ch(i)"));

        assertFalse(_parser.containsVariable("syntax exception"));

    }

    @Test
    public void testFindVariables() {
        // Various assignments
        String string = "s = \"test\";\n bool; action i = 4; action i = i+1;";
        List<StringMatch> variables = _parser.findVariables(string);
        assertEquals(5, variables.size());

        assertEquals("bool", variables.get(1).match());
        assertEquals(string.indexOf("bool"), variables.get(1).start());

        // Method call
        string = "s.charAt(i)";
        variables = _parser.findVariables(string);
        assertEquals(2, variables.size());
        assertEquals("i", variables.get(1).match());
        assertEquals(string.indexOf("i"), variables.get(1).start());

        // Creation inscription
        variables = _parser.findVariables("net: new example");
        assertEquals(1, variables.size());

        // Uplinks and downlinks
        variables = _parser.findVariables(":ch(i)");
        assertEquals(1, variables.size());
        variables = _parser.findVariables("this:ch(i,j)");
        assertEquals(2, variables.size());
        variables = _parser.findVariables("net:ch()");
        assertEquals(1, variables.size());

        // 'this' is not a variable
        variables = _parser.findVariables("this");
        assertEquals(0, variables.size());

        // Syntax exception
        variables = _parser.findVariables("syntax exception");
        assertEquals(0, variables.size());
    }

    @Test
    public void testImports() {
        _parser = new JNPVariableParser("import java.util.List; import java.util.ArrayList;");
        String string = "a = new ArrayList();\na.add(\"3\");\na.add(\"1\");\na.add(\"2\");\nb = new ArrayList();\naction b.addAll(a);\naction b.sort(null);";
        List<StringMatch> variables = _parser.findVariables(string);
        assertEquals(8, variables.size());
    }

    @Test
    public void testFindVariablesAndTypes() {
        Class<?> type = _parser.findVariableType("s");
        assertEquals("java.lang.String", type.getName());
    }
}