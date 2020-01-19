package de.renew.refactoring.util;

import static org.junit.Assert.*;
import org.junit.Test;

import de.renew.refactoring.util.StringHelper;


public class StringHelperTest {
    @Test
    public void testStringReplace() {
        String numbers = "0123456789";
        String replaced = StringHelper.replaceRange(numbers, 3, 5, " ABC ");
        assertEquals("012 ABC 56789", replaced);

        String everything = StringHelper.replaceRange(numbers, 0, 10,
                                                      "replaced everything");
        assertEquals("replaced everything", everything);

        String deleted = StringHelper.replaceRange(numbers, 3, 5, "");
        assertEquals("01256789", deleted);
    }

    @Test
    public void testIndexForLineAndColumn() {
        assertEquals(0, StringHelper.indexForLineAndColumn("abc", 1, 1)); // 'a'
        assertEquals(4, StringHelper.indexForLineAndColumn("abc\nd", 2, 1)); // 'd'
        assertEquals(5, StringHelper.indexForLineAndColumn("abc\ndef\ng", 2, 2)); // 'e'
    }
}