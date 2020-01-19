package de.renew.unify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Vector;


/**
 * JUnit test case for checking the {@link TupleIndex} and
 * related classes.
 * <p>
 * </p>
 * TestTupleIndex.java
 * Created: Fri Nov  9  2001
 * @author Michael Duvigneau
 **/
public class TestTupleIndex {
    private TupleIndex indexA;
    private TupleIndex indexB;
    private Tuple A1;
    private Tuple A2;
    private Tuple A3;
    private List A4;
    private List A5;
    private List A6;
    private Integer A7;
    private Integer A8;
    private Tuple B1;
    private Tuple B2;
    private Tuple B3;
    private Tuple B4;

    @Before
    public void setUp() {
        // indexA contains:
        // A1 - t1
        // A2 - t2
        // A3 - []
        // A4 - {}
        // A5 - {null, t2, {}}
        // A6 - {t1, t2, {}}
        // A7 - 1
        // A8 - 2
        // where t1 = ["a", new Object(), true]
        //       t2 = [["kuno"]]
        indexA = new TupleIndex();
        Tuple t1 = new Tuple(new Object[] { "a", new Object(), Boolean.TRUE },
                             null);
        Tuple t2 = new Tuple(new Object[] { new Tuple(new Object[] { "kuno" },
                                                      null) }, null);
        A1 = t1;
        indexA.insert(A1);
        A2 = t2;
        indexA.insert(A2);
        A3 = new Tuple(0);
        indexA.insert(A3);
        A4 = new List(0);
        indexA.insert(A4);
        A5 = new List(null,
                      new List(t2, new List(new List(0), new List(0), null),
                               null), null);
        indexA.insert(A5);
        A6 = new List(t1,
                      new List(t2, new List(new List(0), new List(0), null),
                               null), null);
        indexA.insert(A6);
        A7 = new Integer(1);
        indexA.insert(A7);
        A8 = new Integer(2);
        indexA.insert(A8);


        // indexB contains:
        // B1 - ["a", "a", "a"]
        // B2 - ["a", "a", null]
        // B3 - ["a", "a", "c"]
        // B4 - ["c", "a", "a"]
        indexB = new TupleIndex();
        B1 = new Tuple(new Object[] { "a", "a", "a" }, null);
        indexB.insert(B1);
        B2 = new Tuple(new Object[] { "a", "a", null }, null);
        indexB.insert(B2);
        B3 = new Tuple(new Object[] { "a", "a", "c" }, null);
        indexB.insert(B3);
        B4 = new Tuple(new Object[] { "c", "a", "c" }, null);
        indexB.insert(B4);
    }

    @Test
    public void testIndexAforTupleA1() {
        // Exact match for Tuple A1.
        assertSearchResults(indexA, A1, new Object[] { A1 });
    }

    @Test
    public void testIndexAforUnknown() {
        // Any index entry matches the UNKNOWN.
        assertSearchResults(indexA, new Unknown(),
                            new Object[] { A1, A2, A3, A4, A5, A6, A7, A8 });
    }

    @Test
    public void testIndexAforFalse() {
        // This value is not registered in the index.
        assertSearchResults(indexA, Boolean.FALSE, new Object[] {  });
    }

    @Test
    public void testIndexAforValue8() {
        // Exact match for the Integer 2.
        assertSearchResults(indexA, new Integer(2), new Object[] { A8 });
    }

    @Test
    public void testIndexAfor0Tuple() {
        // Exact match for the 0-Tuple.
        assertSearchResults(indexA, new Tuple(0), new Object[] { A3 });
    }

    @Test
    public void testIndexAfor3Tuple() {
        // Any 3-component tuple should match, but there is only one
        // in the index.
        assertSearchResults(indexA, new Tuple(3), new Object[] { A1 });
    }

    @Test
    public void testIndexAforEmptyList() {
        // Only the empty List {}.
        assertSearchResults(indexA, new List(0), new Object[] { A4 });
    }

    @Test
    public void testIndexAforUnknownHeadAndTailList() {
        // Any list with head and tail should match.
        assertSearchResults(indexA, new List(2), new Object[] { A5, A6 });
    }

    @Test
    public void testIndexAforNullNullList() {
        // There is no list {null:null}.
        assertSearchResults(indexA, new List(null, null, null),
                            new Object[] {  });
    }

    @Test
    public void testIndexAforUnknownTailList() {
        // Any list with A1 as head should match.
        assertSearchResults(indexA, new List(A1, new Unknown(), null),
                            new Object[] { A6 });
    }

    @Test
    public void testIndexBfor_aXc() {
        // Here the exact match would be B3, but the best set found
        // during the search lies in the arity branch object for the
        // 3rd component of 3-tuples. There the remainder 'c' points
        // to a set of two possible matches.
        assertSearchResults(indexB,
                            new Tuple(new Object[] { "a", new Unknown(), "c" },
                                      null), new Object[] { B3, B4 });
    }

    @Test
    public void testIndexBfor_aac() {
        // Exact match for B3.
        assertSearchResults(indexB,
                            new Tuple(new Object[] { "a", "a", "c" }, null),
                            new Object[] { B3 });
    }

    @Test
    public void testIndexBfor_caa() {
        assertSearchResults(indexB,
                            new Tuple(new Object[] { "c", "a", "a" }, null),
                            new Object[] {  });
    }

    @Test
    public void testModifyingEquals() {
        TupleIndex index = new TupleIndex();
        Vector<String> v1 = new Vector<String>();
        v1.add("eat");
        Vector<String> v2 = new Vector<String>();
        v2.add("eat");
        Vector<String> v3 = new Vector<String>();
        v3.add("eat");
        Tuple t1 = new Tuple(new Object[] { "a", v1 }, null);
        Tuple t2 = new Tuple(new Object[] { "b", v2 }, null);
        Tuple t3 = new Tuple(new Object[] { "c", v3 }, null);
        index.insert(t1);
        index.insert(t2);
        index.insert(t3);


        // Just to make it clear: 
        // The three Vectors are not identical, but they are equal
        // because they have the same contents.
        assertEquals(v1, v2);
        assertEquals(v1, v3);
        assertEquals(v2, v3);
        assertTrue(!(v1 == v2));
        assertTrue(!(v1 == v3));
        assertTrue(!(v2 == v3));


        // What we want to simulate now:
        // A transition removes one token (vector) from the place
        // and modifies it.
        // This should be allowed and possible, in contrast to the
        // modification of the vector while it is in the place.
        index.remove(t1);
        v1.add("more");
        index.insert(t1);
        assertTrue(!v1.equals(v2));
        assertTrue(!v1.equals(v3));


        // Now comes the interesting part: Are the two other vectors
        // still accessible?
        index.remove(t2);
        index.remove(t3);
    }

    private void assertSearchResults(TupleIndex index, Object pattern,
                                     Object[] expected) {
        // a loop counter :)
        int i;


        // the number of expected search results
        // (e.g. the size of the expected[] array)
        int numExpected = expected.length;


        // the search results as an enumeration
        Iterator<Object> searchResults = index.getPossibleMatches(pattern)
                                              .iterator();


        // foundCounts tell how often an expected object was
        // included in the searchResults enumeration.
        // Should be 1 after all iterations.
        int[] foundCounts = new int[numExpected];
        for (i = 0; i < numExpected; ++i) {
            foundCounts[i] = 0;
        }


        // holds one result value after another during the
        // iteration through all search results.
        Object aResult;


        // expectedCount tells how many expected objects did match
        // the current aResult value.
        // Should be 1 after each iteration. If not, the test
        // programmer did make a mistake...
        int expectedCount;


        // This vector collects String messages of all failures
        // detected during the search results check. If not empty at
        // the end, its contents will make up the error message.
        Vector<Object> failMessages = new Vector<Object>();


        // This vector collects all search results for later display
        // in the error message.
        Vector<Object> results = new Vector<Object>();

        while (searchResults.hasNext()) {
            aResult = searchResults.next();
            results.add(aResult);

            expectedCount = 0;
            for (i = 0; i < numExpected; ++i) {
                if (expected[i] == aResult) {
                    expectedCount++;
                    foundCounts[i]++;
                }
            }
            if (expectedCount == 0) {
                failMessages.add(aResult + " was found, but not expected.");
            } else if (expectedCount > 1) {
                failMessages.add(aResult + " was expected too many times ("
                                 + expectedCount + ").");
            }
        }

        for (i = 0; i < numExpected; ++i) {
            if (foundCounts[i] == 0) {
                failMessages.add(expected[i] + " was expected, but missing.");
            } else if (foundCounts[i] > 1) {
                failMessages.add(expected[i] + " was found too many times ("
                                 + foundCounts[i] + ").");
            }
        }

        if (failMessages.size() > 0) {
            StringBuffer message = new StringBuffer();
            message.append("Tuple index search results did not match expectation:");
            message.append("\n  ");
            message.append(index);
            message.append("\n  search pattern: ");
            message.append(pattern);
            message.append("\n  expected: ");
            for (i = 0; i < numExpected; ++i) {
                if (i > 0) {
                    message.append(", ");
                }
                message.append(expected[i]);
            }
            message.append("\n  found: ");
            for (i = 0; i < results.size(); ++i) {
                if (i > 0) {
                    message.append(", ");
                }
                message.append(results.elementAt(i));
            }
            for (i = 0; i < failMessages.size(); ++i) {
                message.append("\n  - ");
                message.append(failMessages.elementAt(i));
            }
            fail(message.toString());
        }
    }
}