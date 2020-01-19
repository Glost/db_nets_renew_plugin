package de.renew.refactoring.parse;

import junit.framework.TestCase;

import org.junit.Test;


public class PackageNameParserTest extends TestCase {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(PackageNameParserTest.class);
    PackageParser _parser;

    @Override
    public void setUp() {
        _parser = new PackageParser();
    }

    @Test
    public void testFindPackageName() {
        assertEquals("de.renew.refactoring",
                     _parser.findPackage("package de.renew.refactoring;"));
        assertNull(_parser.findPackage("invalid declaration"));
    }
}