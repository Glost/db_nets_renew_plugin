package de.renew.refactoring.parse;

import junit.framework.TestCase;

import org.junit.Test;

import de.renew.refactoring.match.StringMatch;
import de.renew.refactoring.parse.name.NameFinder;

import java.util.List;


public abstract class NameFinderTests extends TestCase {
    NameFinder _finder;

    protected abstract NameFinder makeNameFinder(String name);

    @Override
    public void setUp() {
        _finder = makeNameFinder("testname");
    }

    @Test
    public void testFindName() {
        assertTrue(_finder.find("testname"));
        assertTrue(_finder.find("test testname test"));
        assertTrue(_finder.find("test.testname.test"));

        assertFalse(_finder.find("test"));
        assertFalse(_finder.find("testname2"));

    }

    @Test
    public void testListOfMatches() {
        List<StringMatch> matches = _finder.listOfMatches("testing the testname (testname) test.testname");
        System.out.println(matches);
        assertEquals(3, matches.size());

        assertEquals("testname", matches.get(0).match());
        assertEquals(12, matches.get(0).start());
        assertEquals(20, matches.get(0).end());
    }
}