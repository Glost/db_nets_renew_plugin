package de.renew.lola.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.renew.lola.parser.LolaParser;
import de.renew.lola.parser.Marking;
import de.renew.lola.parser.NetFileParseError;
import de.renew.lola.parser.Node;
import de.renew.lola.parser.Place;
import de.renew.lola.parser.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParserTest {
    private Map<String, Node> elements = new HashMap<String, Node>();

    @Before
    public void setup() {
        Place p1 = new Place("p1", 263, 99);
        elements.put(p1.getName(), p1);
        Place p2 = new Place("p2");
        elements.put(p2.getName(), p2);
        Place p5 = new Place("p5", 263, 99);
        elements.put(p5.getName(), p5);
    }

    @Test
    public void checkTokenParsing() {
        try {
            // spacing does not matter
            Marking spacedMarking1 = LolaParser.parseToken("\n\tp2  :  1");
            Marking spacedMarking2 = LolaParser.parseToken("\n\tp2:\n1");
            Assert.assertEquals("p2", spacedMarking1.getName());
            Assert.assertEquals(1, spacedMarking1.getTokens());
            Assert.assertEquals(spacedMarking1, spacedMarking2);

            // all empty markings are equal
            Marking nullMarking1 = LolaParser.parseToken(null);
            Marking nullMarking2 = LolaParser.parseToken("");
            Marking nullMarking3 = LolaParser.parseToken("\n");
            Assert.assertEquals(nullMarking1, nullMarking2);
            Assert.assertEquals(nullMarking3, nullMarking2);
        } catch (NetFileParseError e) {
        }
    }

    @Test(expected = NetFileParseError.class)
    public void parseTokenErrorNoTokens() throws Exception {
        LolaParser.parseToken("p1");
    }

    @Test(expected = NetFileParseError.class)
    public void parseTokenErrorNoNumber() throws Exception {
        LolaParser.parseToken("p2:dreizehn");
    }

    @Test
    public void checkPlaceParsing() {
        try {
            Place result1 = LolaParser.parsePlace("p1{x:263y:99}");
            Assert.assertEquals("p1", result1.getName());
            Assert.assertTrue(263 == result1.getX());
            Place result2 = LolaParser.parsePlace("p2");
            Assert.assertEquals(elements.get("p2"), result2);
            Place result5 = LolaParser.parsePlace("p5{x:263y:99}, p6{x:111y:100};");
            Assert.assertEquals(elements.get("p5"), result5);
        } catch (NetFileParseError e) {
            Assert.fail();
        }
        try {
            LolaParser.parsePlace("p3{x:263y:neunundneunzig}");

            //Assert.assertEquals(p3, new Place("p3"));
        } catch (NetFileParseError e) {
            Assert.assertEquals(e.getMessage(),
                                new NetFileParseError("Coordinates could not be parsed")
                                .getMessage());
        }
        try {
            Place p4 = LolaParser.parsePlace("p4{x:263}");
            Assert.assertEquals(p4, new Place("p4"));
        } catch (NetFileParseError e) {
            Assert.assertEquals(e.getMessage(),
                                new NetFileParseError("Comment does not contain coordinates")
                                .getMessage());
        }
    }

    @Test
    public void checkPlacesParsing() {
        List<Place> places = new ArrayList<Place>();
        places.add(new Place("p1"));
        places.add(new Place("p2"));
        places.add(new Place("p3"));
        String testString1 = "p1, p2, p3";
        try {
            List<Place> result = LolaParser.parsePlaces(testString1);
            Assert.assertEquals(result, places);
        } catch (NetFileParseError e) {
            e.printStackTrace();
            Assert.fail();
        }
        places = new ArrayList<Place>();
        places.add(new Place("p1", 33, 60));
        places.add(new Place("p2", 0, 0));
        places.add(new Place("p3", 900, 7000));
        String testString2 = "p1{x:33y:60},\n    p2{x:0y:0},\n    p3{x:900y:7000};\n   ";
        try {
            List<Place> result = LolaParser.parsePlaces(testString2);
            Assert.assertEquals(result, places);
            for (Place p : result) {
                Assert.assertTrue(p.hasCoordinates());
            }
        } catch (NetFileParseError e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void checkTransitionParsing() {
        String transition1 = "t3\nCONSUME\n  p1: 1,\n  p2: 3;\nPRODUCE\n  p2: 1";
        String transition2 = "t3{x:100y:50} CONSUME p1: 1, p2: 3; PRODUCE p2: 1";
        LolaParser parser = new LolaParser();

//        Map<Place, Integer> pre = new HashMap<Place, Integer>();
//        pre.put((Place) LolaParser.forName("p1"), 1);
//        pre.put((Place) LolaParser.forName("p2"), 3);
//        Map<Place, Integer> post = new HashMap<Place, Integer>();
//        pre.put((Place) LolaParser.forName("p2"), 1);
//        Transition expected = new Transition("t3", pre, post);
        try {
            Transition result = parser.parseTransition(transition1);
            Assert.assertEquals(result.getName(), "t3");
            Assert.assertFalse(result.hasCoordinates());

            result = parser.parseTransition(transition2);
            //Assert.assertEquals(result.);
            Assert.assertTrue(result.hasCoordinates());
        } catch (NetFileParseError e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void checkMarkingParsing() {
        String marking1 = "p1: 1";
        String marking2 = "p2:2";
        try {
            Marking m1 = LolaParser.parseToken(marking1);
            Assert.assertEquals(m1.getName(), "p1");
            Assert.assertEquals(m1.getTokens(), 1);
            Marking m2 = LolaParser.parseToken(marking2);
            Assert.assertEquals(m2.getName(), "p2");
            Assert.assertEquals(m2.getTokens(), 2);
        } catch (NetFileParseError e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}