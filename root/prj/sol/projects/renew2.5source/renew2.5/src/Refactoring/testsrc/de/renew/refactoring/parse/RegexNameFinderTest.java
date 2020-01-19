package de.renew.refactoring.parse;

import de.renew.refactoring.parse.name.NameFinder;
import de.renew.refactoring.parse.name.RegexNameFinder;


public class RegexNameFinderTest extends NameFinderTests {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(RegexNameFinderTest.class);

    @Override
    protected NameFinder makeNameFinder(String name) {
        return new RegexNameFinder(name);
    }
}