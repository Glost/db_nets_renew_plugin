package de.renew.lola.parser;

import CH.ifa.draw.figures.AttributeFigure;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNDrawingHelper;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class LolaParser {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LolaParser.class);
    private List<Transition> transitions = new ArrayList<Transition>();
    private List<Place> places = new ArrayList<Place>();
    private Map<String, Node> elementsMap = new HashMap<String, Node>();
    private Map<Node, AttributeFigure> figureMap = new HashMap<Node, AttributeFigure>();

    /**
     * instead of using ShadowArc.ordinary
     */
    private final int ORDINARY_ARC = 1;

    /**
     * Here the parsing takes place, during the process it fills the
     * instance variables.
     * @param stream
     * @throws NetFileParseError
     */
    public void parse(InputStream stream) throws NetFileParseError {
        logger.info("[Lola] Lola Import: Starting parser.");
        Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(stream)));
        scanner.useDelimiter(";");
        try {
            scanner.findWithinHorizon("PLACE", 0);
            places = parsePlaces(scanner.next());
            for (Place p : places) {
                elementsMap.put(p.getName(), p);
            }
            scanner.findWithinHorizon("MARKING", 0);
            Map<Place, Integer> markings = parseMarking(scanner.next());
            for (Place p : markings.keySet()) {
                p.setInitialMarking(markings.get(p));
            }
            scanner.useDelimiter(";?\\s*TRANSITION");
            while (scanner.hasNext()) {
                Transition t = parseTransition(scanner.next());
                transitions.add(t);
                elementsMap.put(t.getName(), t);
            }
        } catch (NetFileParseError e) {
            throw e;
        }
    }

    public Transition parseTransition(String transition)
            throws NetFileParseError {
        if (transition.trim().endsWith(";")) {
            transition = transition.substring(0, transition.trim().length());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Now parsing the transition\n" + transition);
        }
        Scanner transitionScanner = new Scanner(transition);
        String nameLine = transitionScanner.next();
        if (logger.isDebugEnabled()) {
            logger.debug(nameLine);
        }
        String[] nameAndCoordinates = nameLine.split("\\{");
        String transitionName = nameAndCoordinates[0].trim();
        Transition result = new Transition(transitionName);
        if (nameLine.contains("{") && nameAndCoordinates.length > 1) {
            int[] coords = parseCoordinates(nameAndCoordinates[1].trim());
            if (coords != null) {
                result.setX(coords[0]);
                result.setY(coords[1]);
            }
        }
        if (transitionScanner.next().equals("CONSUME")) {
            transitionScanner.useDelimiter(";\\s*PRODUCE");
            String consumeBlock = transitionScanner.next();
            if (consumeBlock != null) {
                Map<Place, Integer> pre = parseMarking(consumeBlock);
                result.setPre(pre);
            }
        } else {
            throw (new NetFileParseError("Missing CONSUME keyword"));
        }
        if (transitionScanner.hasNext()) {
            Map<Place, Integer> post = parseMarking(transitionScanner.next());
            result.setPost(post);
        } else {
            throw (new NetFileParseError("Missing PRODUCE keyword"));
        }
        return result;
    }

    /**
     * Parses a marking, in the context of Lola this is a comma-separated
     * list of place names followed by a colon (:) and a number of tokens.
     * Markings occur in the MARKINGS section of a net file and also in the
     * CONSUME and PRODUCE blocks of transitions.
     * The returned map may be empty (if a transition has no pre- or post-set).
     * @param marking - a String
     * @return map of {@link Place}s to Integers
     * @throws NetFileParseError
     */
    public Map<Place, Integer> parseMarking(String marking)
            throws NetFileParseError {
        if (logger.isDebugEnabled()) {
            logger.debug("Now parse the marking");
        }
        Scanner markingScanner = new Scanner(marking);
        markingScanner.useDelimiter(",");
        Map<Place, Integer> markings = new HashMap<Place, Integer>();
        while (markingScanner.hasNext()) {
            try {
                Marking found = parseToken(markingScanner.next());
                if (found != null) {
                    Place place = (Place) forName(found.getName());
                    markings.put(place, found.getTokens());
                }
            } catch (NetFileParseError e) {
                e.printStackTrace();
                throw (e);
            }
        }
        return markings;
    }

    /**
     * Gets the node for a name.
     * @param name
     * @return the node (place or transition) named 'name'
     */
    public Node forName(String name) {
        return elementsMap.get(name);
    }

    /**
     * Parses a single token on a place and returns a {@link Marking}
     * or {@code null} if the token is an empty String.
     * @param token - a String token
     * @throws NetFileParseError
     */
    public static Marking parseToken(String token) throws NetFileParseError {
        if (token != null && !token.trim().isEmpty()) {
            String[] placeTokens = token.split(":");
            if (placeTokens.length > 1) {
                if (logger.isDebugEnabled()) {
                    logger.debug(placeTokens[0] + " has " + placeTokens[1]
                                 + " tokens.");
                }
                try {
                    Integer tokenCount = Integer.parseInt(placeTokens[1].trim());
                    Marking marking = new Marking(placeTokens[0].trim(),
                                                  tokenCount);
                    return marking;
                } catch (NumberFormatException e) {
                    throw (new NetFileParseError("Could not parse token number"));
                }
            } else {
                throw (new NetFileParseError("Place without marking not allowed in MARKING section"));
            }
        } else {
            // the token is an empty string, return null
            return null;
        }
    }

    /**
     * Parses a comma-separated list of places
     * @param places
     * @throws NetFileParseError
     */
    public static List<Place> parsePlaces(String places)
            throws NetFileParseError {
        if (logger.isDebugEnabled()) {
            logger.debug("First parse the places");
        }
        Scanner placeScanner = new Scanner(places);
        placeScanner.useDelimiter(",");
        List<Place> result = new ArrayList<Place>();
        while (placeScanner.hasNext()) {
            result.add(parsePlace(placeScanner.next()));
        }
        return result;
    }


    /**
     * Parses a single place with optional location comment
     * @param place
     * @throws NetFileParseError
     */
    public static Place parsePlace(String place) throws NetFileParseError {
        if (logger.isDebugEnabled()) {
            logger.debug(place);
        }
        Scanner placeScanner = new Scanner(place);
        placeScanner.useDelimiter("\\{");
        String placeName = placeScanner.next().trim();
        Place result = new Place(placeName);
        if (placeScanner.hasNext()) {
            int[] coords = parseCoordinates(placeScanner.next());
            if (coords != null) {
                result.setX(coords[0]);
                result.setY(coords[1]);
            }
        }
        return result;
    }

    private static int[] parseCoordinates(String coordinates)
            throws NetFileParseError {
        if (logger.isDebugEnabled()) {
            logger.debug(coordinates);
        }
        if (coordinates.startsWith("x") && coordinates.contains("y")) {
            int[] coordArray = new int[2];
            try {
                String xs = coordinates.substring(coordinates.indexOf(":") + 1,
                                                  coordinates.indexOf("y"));
                String ys = coordinates.substring(coordinates.lastIndexOf(":")
                                                  + 1, coordinates.indexOf("}"));
                coordArray[0] = Integer.parseInt(xs);
                coordArray[1] = Integer.parseInt(ys);
                if (logger.isDebugEnabled()) {
                    logger.debug("Coordinates: (x=" + xs + "|y=" + ys + ")");
                }
            } catch (NumberFormatException e) {
                throw (new NetFileParseError("Coordinates could not be parsed"));
            }
            return coordArray;
        } else {
            throw (new NetFileParseError("Comment does not contain coordinates"));
        }
    }

    /**
    * Imports places, transitions, arcs and their names from a stream which
    * needs to be in Lolas net file format.
    * It returns a CPNDrawing corresponding to the net file.
    *
    * @param stream
    */
    public CPNDrawing importNet(InputStream stream) {
        try {
            parse(stream);
        } catch (NetFileParseError e) {
            logger.error("[LolaParser] : " + e.getMessage());
            return new CPNDrawing();
        }
        CPNDrawing drawing = new CPNDrawing();
        CPNDrawingHelper drawer = new CPNDrawingHelper();

        // draw places
        if (logger.isDebugEnabled()) {
            logger.debug("Drawing places");
        }
        for (Place p : places) {
            if (logger.isDebugEnabled()) {
                logger.debug("Drawing place " + p + " (" + p.getX() + "|"
                             + p.getY());
            }
            PlaceFigure place = drawer.createPlace();
            if (p.hasCoordinates()) {
                place.moveBy(p.getX(), p.getY());
            }
            drawing.add(place);
            drawing.add(drawer.createNameTextFigure(p.getName(), place));
            if (logger.isDebugEnabled()) {
                logger.debug("Added and named place " + p);
            }
            figureMap.put(p, place);
            // add initial marking
            if (p.initiallyMarked()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding marking " + p.getInitialMarking());
                }
                for (int j = 0; j < p.getInitialMarking(); j++) {
                    drawing.add(drawer.createInscription("[]", place));
                }
            }
        }

        // draw transitions and arcs
        if (logger.isDebugEnabled()) {
            logger.debug("Drawing transitions");
        }
        for (Transition t : transitions) {
            TransitionFigure transition = drawer.createTransition();
            if (t.hasCoordinates()) {
                transition.moveBy(t.getX(), t.getY());
            }
            drawing.add(transition);
            figureMap.put(t, transition);
            drawing.add(drawer.createNameTextFigure(t.getName(), transition));
            for (Place p : t.getPre().keySet()) {
                ArcConnection arc = drawer.createArcConnection(figureOf(p),
                                                               transition,
                                                               ORDINARY_ARC);
                drawing.add(arc);
                int tokenpre;
                if ((tokenpre = t.getPre().get(p)) > 1) {
                    drawing.add(drawer.createWeightTextFigure(arc, tokenpre));
                }
            }
            for (Place p : t.getPost().keySet()) {
                ArcConnection arc = drawer.createArcConnection(transition,
                                                               figureOf(p),
                                                               ORDINARY_ARC);
                drawing.add(arc);
                int tokenpost;
                if ((tokenpost = t.getPost().get(p)) > 1) {
                    drawing.add(drawer.createWeightTextFigure(arc, tokenpost));
                }
            }
        }
        return drawing;
    }

    private AttributeFigure figureOf(Node p) {
        return figureMap.get(p);
    }
}