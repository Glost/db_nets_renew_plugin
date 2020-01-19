package de.renew.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;


/**
 * This is a TestCase for the BijectiveMap where the mapping is bijective. <br>
 *
 * You could use both values as keys and/or as values. <br>
 *
 * @author Benjamin Schleinzer <mailto: 0schlein@informatik.uni-hamburg.de>
 *
 * @version 1.0
 */
public class BijectiveMapTest {
    private BijectiveMap<String, Integer> testMap;

    @Before
    public void setUp() throws Exception {
        testMap = new BijectiveMap<String, Integer>();
        testMap.put("one", 1);
        testMap.put("two", 2);
        testMap.put("three", 3);
    }

    @Test
    public void testClear() {
        testMap.clear();
        assertNull(testMap.get("one"));
        assertNull(testMap.getKey(1));
    }

    @Test
    public void testGetKey() {
        assertTrue(testMap.getKey(1).equals("one"));
    }

    @Test
    public void testRemoveObject() {
        testMap.remove("one");
        assertNull(testMap.getKey(1));
        assertNull(testMap.get("one"));
    }

    @Test
    public void testRemoveKey() {
        testMap.removeKey(1);
        assertNull(testMap.getKey(1));
        assertNull(testMap.get("one"));
    }
}