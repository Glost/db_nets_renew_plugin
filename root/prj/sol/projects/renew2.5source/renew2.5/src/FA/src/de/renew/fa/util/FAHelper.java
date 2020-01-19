/*
 * Created on Sep 16, 2005
 *
 */
package de.renew.fa.util;

import org.apache.log4j.Logger;

import CH.ifa.draw.figures.ArrowTip;
import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.PolyLineFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.util.Geom;

import de.renew.fa.FADrawing;
import de.renew.fa.figures.EndDecoration;
import de.renew.fa.figures.FAArcConnection;
import de.renew.fa.figures.FAStateFigure;
import de.renew.fa.figures.FATextFigure;
import de.renew.fa.figures.FigureDecoration;
import de.renew.fa.figures.StartDecoration;
import de.renew.fa.figures.StartEndDecoration;
import de.renew.fa.model.Arc;
import de.renew.fa.model.FA;
import de.renew.fa.model.FAImpl;
import de.renew.fa.model.Letter;
import de.renew.fa.model.State;
import de.renew.fa.model.Word;

import de.renew.gui.CPNTextFigure;



//import de.renew.netanalysis.materials.BoolMatrix;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;


/**
 * Provides some static methods for Finite Automata (FA) to convert model
 * <code>FA</code> into Renew drawing representations <code>FADrawing</code>
 * and vice versa. Also: incidence matrix generation, uniform arc name and
 * converts an FA into the <code>Properties</code> format.
 *
 *
 * @author cabac
 *
 */
public class FAHelper {
    private static Logger logger = Logger.getLogger(FAHelper.class);

    /**
     * Converts a model of a Finite Automata as <code> FA</code> into a Renew
     * drawing.
     *
     * @param fa -
     *            The model of the FA.
     * @return The Renew <code>Drawing</code>
     */
    public static Drawing convertModelToDrawing(FA fa) {
        HashMap<State, FAStateFigure> figures = new HashMap<State, FAStateFigure>();

        FADrawing faDrawing = new FADrawing();
        Iterator<State> itStates = fa.getStates();

        // States
        int xOffset = 0;
        int yToggle = 0;
        while (itStates.hasNext()) {
            State state = itStates.next();
            FAStateFigure stateFigure = new FAStateFigure();
            FATextFigure text = new FATextFigure(CPNTextFigure.LABEL,
                                                 state.getName());
            text.setParent(stateFigure);
            faDrawing.add(stateFigure);
            faDrawing.add(text);

            stateFigure.setFillColor(Color.white);

            // FigureDecoration
            if (fa.startStates().contains(state)) {
                if (fa.endStates().contains(state)) {
                    stateFigure.setDecoration(new StartEndDecoration());
                } else {
                    stateFigure.setDecoration(new StartDecoration());
                }
            } else if (fa.endStates().contains(state)) {
                stateFigure.setDecoration(new EndDecoration());
            }
            Point loc = stateFigure.displayBox().getLocation();
            Dimension d = FAStateFigure.defaultDimension();
            int w2 = d.width / 2;
            int h2 = d.height / 2;
            stateFigure.displayBox(new Point(loc.x - w2, loc.y - h2),
                                   new Point(loc.x - w2 + d.width,
                                             loc.y - h2 + d.height));
            xOffset = xOffset + 100;
            yToggle = 1 - yToggle;
            stateFigure.moveBy(xOffset, 50 + 100 * yToggle);

            figures.put(state, stateFigure);
        }


        // System.out.println(FAHelper.getStates(fa));
        // Arcs
        // Iterator iter = arcs.values().iterator();
        // HashMap tuple = new HashMap();
        // while (iter.hasNext()) {
        // Arc arc = (Arc) iter.next();
        // StateTuple st = new StateTuple(arc);
        // tuple.put(st.getName(),st);
        // }
        Iterator<Arc> itArcs = fa.getArcs();
        while (itArcs.hasNext()) {
            Arc arc = itArcs.next();
            FAArcConnection faArcConnection = new FAArcConnection(null,
                                                                  new ArrowTip(),
                                                                  AttributeFigure.LINE_STYLE_NORMAL);
            faArcConnection.setAttribute("LineShape",
                                         new Integer(PolyLineFigure.BSPLINE_SHAPE));
            Word inscription = arc.getInscription();
            State startState = arc.getFrom();
            State endState = arc.getTo();
            Figure fromFigure = figures.get(startState);
            Figure toFigure = figures.get(endState);

            Connector fromCon = fromFigure.connectorAt(fromFigure.center());
            Connector toCon = toFigure.connectorAt(toFigure.center());

            faArcConnection.startPoint(fromFigure.center());
            faArcConnection.connectStart(fromCon);
            faArcConnection.endPoint(toFigure.center());
            faArcConnection.connectEnd(toCon);

            if (fromFigure.equals(toFigure)) {
                Point p = fromFigure.center();
                faArcConnection.insertPointAt(new Point(p.x - 50, p.y + 50), 1);

                faArcConnection.insertPointAt(new Point(p.x + 50, p.y + 50), 2);
            } else if (fa.hasArc(endState.getName(), startState.getName())) {
                Point d = getOrthonormalPointOffset(fromFigure, toFigure);
                faArcConnection.insertPointAt(d, 1);
            }
            FATextFigure text = new FATextFigure(CPNTextFigure.INSCRIPTION,
                                                 inscription.getName());
            if (!inscription.isEmpty()) {
                text.setParent(faArcConnection);
            }
            faArcConnection.updateConnection();

            faDrawing.add(faArcConnection);
            if (!inscription.isEmpty()) {
                faDrawing.add(text);
            }
        }

        faDrawing.setName(fa.getName());
        return faDrawing;
    }

    private static Point getOrthonormalPointOffset(Figure fromFigure,
                                                   Figure toFigure) {
        Point a = fromFigure.center();
        Point b = toFigure.center();
        Point s = Geom.middle(a, b);
        Point d = null;
        double distance = a.distance(b);
        double alpha = 50;
        double deltaX = (a.x - b.x) / distance;
        double deltaY = (a.y - b.y) / distance;

        d = new Point(new Double(s.x - alpha * deltaY).intValue(),
                      new Double(s.y + alpha * deltaX).intValue());

        return d;

    }

    /**
     * Generates a standardized name for an Arc.
     *
     * @param inscription -
     *            the arc inscription.
     * @param from -
     *            the state
     * @param to -
     *            the other state
     */
    public static String getArcName(Word inscription, State from, State to) {
        String inscriptionText;
        if (inscription.isEmpty()) {
            inscriptionText = "";
        } else {
            inscriptionText = inscription.getName();
        }
        return "(" + from.getName() + ":" + inscriptionText + ":"
               + to.getName() + ")";
    }

//    /**
//     * Calculates the incidence matrix as a <code>BoolMatrix</code>.
//     *
//     * @see de.renew.netanalysis.BoolMatrix for a given fa.
//     * @param fa -
//     *            the finite automata model.
//     * @return The incidence matrix.
//     */
//    public static BoolMatrix getIncidenceMatrix(FA fa) {
//        Iterator<State> iter = fa.getStates();
//        HashMap<State, Integer> inverseStates = new HashMap<State, Integer>();
//        int i = 0;
//        while (iter.hasNext()) {
//            State state = iter.next();
//            // System.out.println(state.getName());
//            inverseStates.put(state, new Integer(i++));
//        }

//        int size = fa.numberOfStates();
//        BoolMatrix result = new BoolMatrix(size);
//        Iterator<Arc> it = fa.getArcs();
//        while (it.hasNext()) {
//            Arc arc = it.next();
//            State from = arc.getFrom();
//            State to = arc.getTo();
//            int fromInt = inverseStates.get(from).intValue();
//            int toInt = inverseStates.get(to).intValue();
//            result.setElement(fromInt, toInt, true);
//        }
//        result.toString();
//        return result;
//    }

    /**
     * Converts a Finite Automata represented as Renew <code>FADrawing</code>
     * into the </code> model.
     *
     * @param drawing -
     *            The drawing representing an fa.
     * @return The model of the Finite Automata as <code>FA</code>.
     */
    public static FA getModel(FADrawing drawing) {
        FA fa = new FAImpl();
        fa.setName(drawing.getName());


        // first run: all figures getting the states
        // adding states to fa
        FigureEnumeration enumeration = drawing.figures();
        while (enumeration.hasMoreElements()) {
            Figure fig = enumeration.nextElement();

            // System.out.println("Figure " + fig);
            if (fig instanceof FAStateFigure) {
                State state = null;
                FAStateFigure stateFig = (FAStateFigure) fig;
                FigureEnumeration childEnum = stateFig.children();
                if (childEnum.hasMoreElements()) {
                    TextFigure child = (TextFigure) childEnum.nextFigure();

                    // System.out.println(" StateText " + child.getText());
                    // Create new states in fa!
                    state = fa.newState(child.getText());
                }
                if (state != null) {
                    // Take care of decorations.
                    FigureDecoration deco = stateFig.getDecoration();
                    if (deco != null) {
                        if (deco instanceof StartDecoration) {
                            state.setStartState(true);
                        }
                        if (deco instanceof StartEndDecoration) {
                            state.setEndState(true);
                            state.setStartState(true);
                        }
                        if (deco instanceof EndDecoration) {
                            state.setEndState(true);
                        }
                    }
                }
            }
        }

        // System.out.println(fa.getStates());
        // second run getting the connections.
        enumeration = drawing.figures();
        while (enumeration.hasMoreElements()) {
            Figure fig = enumeration.nextElement();
            if (fig instanceof FAArcConnection) {
                FAArcConnection arc1 = ((FAArcConnection) fig);
                FigureEnumeration childEnum = arc1.children();
                Word inscription = null;
                if (childEnum.hasMoreElements()) {
                    TextFigure child = (TextFigure) childEnum.nextFigure();

                    // System.out.println(" LineText " + child.getText());
                    String inscriptionText = child.getText();
                    if (inscriptionText.length() > 0) {
                        // new Word as inscription for the arc
                        inscription = fa.newWord(inscriptionText);


                        // add each letter of the Word to the Alphabet,
                        // if not already added.
                        for (int i = 0; i < inscriptionText.length(); i++) {
                            char character = inscriptionText.charAt(i);
                            if (character != ',') {
                                fa.newLetter(Character.toString(character));
                            }
                        }
                    } else {
                        System.err.println("FAHelper: length of text of inscription is 0.");


                        // 
                        // this should not happen
                    }
                } else {
                    // Arc has no inscription. Treat as lambda arc.
                    inscription = fa.newWord("");
                }
                FAArcConnection line = (FAArcConnection) fig;
                Figure startfig = line.startFigure();
                Figure endfig = line.endFigure();


                // get names of connected figures; add arc to fa
                // if figures are FAStateFigure AND have inscription
                if (startfig instanceof FAStateFigure
                            && endfig instanceof FAStateFigure) {
                    FAStateFigure start = (FAStateFigure) startfig;
                    FAStateFigure end = (FAStateFigure) endfig;
                    FigureEnumeration se = (start).children();
                    FigureEnumeration ee = (end).children();

                    String startName = null;
                    if (se.hasMoreElements() && ee.hasMoreElements()) {
                        startName = ((TextFigure) se.nextFigure()).getText();

                        State from = fa.getStateByName(startName);
                        String endName = null;
                        endName = ((TextFigure) ee.nextFigure()).getText();

                        State to = fa.getStateByName(endName);

                        fa.newArc(from, inscription, to);
                    } else {
                        logger.warn("Ignoring Arc: no inscriptions on states.");
                    }
                } else {
                    logger.warn("Ignoring Arc: not connected to state figure.");
                }
            }
        }


        // System.out.println(fa.getArcs());
        // System.out.println(fa.toString());
        return fa;
    }

    /**
     * Returns a given fa as Properties. Thus an fa can be stored in a unique
     * way that can be read again.
     *
     * @param fa -
     *            The finite automata model.
     * @return The fa as Properties.
     */
    public static Properties toProperties(FA fa) {
        Properties properties = new Properties();
        properties.setProperty("Z", getStates(fa).toString());
        properties.setProperty("Sigma", getAlphabet(fa).toString());
        properties.setProperty("K", getArcs(fa).toString());
        properties.setProperty("Z_Start", fa.startStates().toString());
        properties.setProperty("Z_End", fa.endStates().toString());
        return properties;
    }

    private static Vector<State> getStates(FA fa) {
        Vector<State> v = new Vector<State>();
        Iterator<State> it = fa.getStates();
        while (it.hasNext()) {
            State state = it.next();
            v.add(state);
        }
        return v;
    }

    private static Vector<Letter> getAlphabet(FA fa) {
        Vector<Letter> v = new Vector<Letter>();
        Iterator<Letter> it = fa.getAlphabet();
        while (it.hasNext()) {
            Letter letter = it.next();
            v.add(letter);
        }
        return v;
    }

    private static Vector<Arc> getArcs(FA fa) {
        Vector<Arc> v = new Vector<Arc>();
        Iterator<Arc> it = fa.getArcs();
        while (it.hasNext()) {
            Arc arc = it.next();
            v.add(arc);
        }
        return v;
    }

    // /**
    // * Writes the FA in XFA format.
    // */
    // public String toString() {
    // return "Z = " + states.values().toString() + "\nSigma = "
    // + alphabet.values().toString() + "\nK = "
    // + arcs.values().toString() + "\nZ_Start = "
    // + startStates().toString() + "\nZ_End = "
    // + endStates().toString();
    // }
}