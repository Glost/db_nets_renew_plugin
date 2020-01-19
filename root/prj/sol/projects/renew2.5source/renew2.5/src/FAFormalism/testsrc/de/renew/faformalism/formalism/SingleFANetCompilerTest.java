package de.renew.faformalism.formalism;

import junit.framework.Assert;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.fa.FADrawing;
import de.renew.fa.figures.EndDecoration;
import de.renew.fa.figures.FAArcConnection;
import de.renew.fa.figures.FAStateFigure;
import de.renew.fa.figures.FATextFigure;
import de.renew.fa.figures.NullDecoration;
import de.renew.fa.figures.StartDecoration;
import de.renew.fa.figures.StartEndDecoration;

import de.renew.faformalism.compiler.FANetCompiler;
import de.renew.faformalism.compiler.SingleFANetCompiler;
import de.renew.faformalism.shadow.FAShadowLookupExtension;
import de.renew.faformalism.shadow.ShadowFAArc;
import de.renew.faformalism.shadow.ShadowFAState;

import de.renew.gui.CPNTextFigure;

import de.renew.net.Net;
import de.renew.net.NetElementID;
import de.renew.net.Place;
import de.renew.net.Transition;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowInscribable;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowLookup;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowNetSystem;
import de.renew.shadow.SyntaxException;

import java.io.File;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


public class SingleFANetCompilerTest implements StatusDisplayer {
    private ShadowNetSystem netSystem;
    private ShadowCompilerFactory compFactory;
    private static File samplesFolder;

    /**
     * Sets the samples/state-diagrams directory for loading
     * state-diagram drawings.
     *
     */
    @BeforeClass
    public static void setUpOnce() {
        samplesFolder = null;
        File location = new File("./");
        System.out.println("PWD is " + location.getAbsolutePath());
        File faDir = null;
        List<File> roots = Arrays.asList(File.listRoots());
        for (File loc = location; !roots.contains(loc);
                     loc = loc.getParentFile()) {
            if (Arrays.asList(location.list()).contains("FA")) {
                faDir = loc;
                break;
            }
        }
        if (faDir != null) {
            samplesFolder = new File(faDir, "FA/samples/test/state-diagrams");
            System.out.println("samples Folder is "
                               + samplesFolder.getAbsolutePath());
        } else {
            Assert.fail("Could not find samples folder");
        }
    }

    /**
     * Creates the CompilerFactory and a ShadowNetSystem, so that compilation
     * can be initiated.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        compFactory = new FANetCompiler();
        netSystem = new ShadowNetSystem(compFactory);
    }

    /**
        * Remove references for allowing garbage collection.
        */
    @After
    public void tearDown() throws Exception {
        compFactory = null;
        netSystem = null;
    }

    /**
     * Tests the compilation of every state type, which are:
     * <ul>
     *   <li> Normal states</li>
     *   <li> Start states</li>
     *   <li> End states</li>
     *   <li> Start and end states</li>
     * </ul>
     * @throws SyntaxException
     */
    @Test
    public void testCompilationOfStateTypes() throws SyntaxException {
        checkStateCompilation("state.fa", NullDecoration.class, false);

        checkStateCompilation("startState.fa", StartDecoration.class, false);

        checkStateCompilation("endState.fa", EndDecoration.class, false);

        checkStateCompilation("startEndState.fa", StartEndDecoration.class,
                              false);
    }

    /**
     * Tests the compilation of different inscriptions to states, which are:
     * <ul>
     *   <li> Default inscription (z_{i})</li>
     *   <li> Multiple lines</li>
     *   <li> Java statement (System.out.println("Hello World");)</li>
     *   <li> Tuple ([])</li>
     *   <li> Uplink of synchronous channel</li>
     *   <li> Downlink of synchronous channel</li>
     *   <li> Strange inscription</li>
     * </ul>
     * @throws SyntaxException
     */
    @Test
    public void testCompilationOfStateInscriptions() throws SyntaxException {
        checkStateCompilation("state-inscr-default.fa", NullDecoration.class,
                              true);

        checkStateCompilation("state-inscr-multipleLines.fa",
                              NullDecoration.class, true);

        checkStateCompilation("state-inscr-javaStatement.fa",
                              NullDecoration.class, true);

        checkStateCompilation("state-inscr-tuple.fa", NullDecoration.class, true);

        checkStateCompilation("state-inscr-uplink.fa", NullDecoration.class,
                              true);

        checkStateCompilation("state-inscr-downlink.fa", NullDecoration.class,
                              true);

        checkStateCompilation("state-inscr-strange.fa", NullDecoration.class,
                              true);
    }

    private void checkStateCompilation(String fileName, Class expectedDeco,
                                       boolean checkInscriptions)
            throws SyntaxException {
        File testState = new File(samplesFolder, fileName);
        FADrawing drawing = (FADrawing) DrawingFileHelper.loadDrawing(testState,
                                                                      this);
        FigureEnumeration figures = drawing.figures();
        FAStateFigure stateFig = null;

        // if there are inscriptions, find the FAStateFigure
        while (figures.hasMoreElements()) {
            Figure fig = figures.nextElement();
            if (fig instanceof FAStateFigure) {
                stateFig = (FAStateFigure) fig;
            }
        }

        // Drawing should contain only one figure, which is an FAStateFigure,
        // which has the specified FigureDecoration
        assertNotNull(stateFig);
        assertTrue(stateFig instanceof FAStateFigure);
        assertTrue(stateFig.getDecoration().getClass() == expectedDeco);
        assertFalse(figures.hasMoreElements());

        // State should be transferred to shadow layer, and compiled to a place
        FAShadowLookupExtension faLookup = compileFADrawing(drawing);
        ShadowFAState shadowState = (ShadowFAState) stateFig.getShadow();
        assertTrue(shadowState instanceof ShadowFAState);
        Place place = faLookup.get((ShadowFAState) shadowState);
        assertNotNull(place);
        assertEquals(new NetElementID(stateFig.getID()), place.getID());

        if (checkInscriptions) {
            checkStateInscription(stateFig);
        }
    }

    private void checkStateInscription(FAStateFigure stateFig) {
        FigureEnumeration children = stateFig.children();
        while (children.hasMoreElements()) {
            Figure child = children.nextElement();
            if (child instanceof FATextFigure) {
                // Only allow NAMEs for states
                assertTrue(((FATextFigure) child).getType() == CPNTextFigure.NAME);
            } else {
                fail("Only FATextFigures are allowed to be attached to states.");
            }
        }
    }

    /**
     * Tests the compilation of a transition and a loop with no inscription.
     * @throws SyntaxException
     */
    @Test
    public void testCompilationOfArcWithoutInscription()
            throws SyntaxException {
        checkArcCompilation("transition-noInscr.fa", false);
        checkArcCompilation("loop-noInscr.fa", false);
    }

    /**
     * Tests the compilation of different inscriptions to transitions, which are:
     * <ul>
     *   <li> No inscription </li>
     *   <li> Default inscription (a)</li>
     *   <li> Multiple lines</li>
     *   <li> Java statement (System.out.println("Hello World");)</li>
     *   <li> Uplink of synchronous channel</li>
     *   <li> Downlink of synchronous channel</li>
     *   <li> Strange inscription</li>
     *   <li> Loop default (a)</li>
     * </ul>
     * @throws SyntaxException
     */
    @Test
    public void testCompilationOfArcInscription() throws SyntaxException {
        checkArcCompilation("transition-noInscr.fa", false);
        checkArcCompilation("loop-noInscr.fa", false);

        checkArcCompilation("transition-inscr-default.fa", true);

        checkArcCompilation("transition-inscr-multipleLines.fa", true);

        checkArcCompilation("transition-inscr-javaStatement.fa", true);

        checkArcCompilation("transition-inscr-uplink.fa", true);

        checkArcCompilation("transition-inscr-downlink.fa", true);

        checkArcCompilation("loop-inscr.fa", false);
    }

    private void checkArcCompilation(String fileName, boolean checkInscriptions) {
        File testArc = new File(samplesFolder, fileName);
        FADrawing drawing = (FADrawing) DrawingFileHelper.loadDrawing(testArc,
                                                                      this);
        FigureEnumeration figures = drawing.figures();
        FAArcConnection arcConnection = null;

        // find the FAArcConnection
        while (figures.hasMoreElements()) {
            Figure fig = figures.nextElement();
            if (fig instanceof FAArcConnection) {
                arcConnection = (FAArcConnection) fig;
            }
        }

        assertNotNull(arcConnection);
        assertTrue(arcConnection instanceof FAArcConnection);
        assertFalse(figures.hasMoreElements());

        // Arc should be transferred to shadow layer, and compiled to a transition 
        FAShadowLookupExtension faLookup = compileFADrawing(drawing);
        ShadowFAArc shadowArc = (ShadowFAArc) arcConnection.getShadow();
        assertTrue(shadowArc instanceof ShadowFAArc);
        Transition transition = faLookup.get((ShadowFAArc) shadowArc);
        assertNotNull(transition);
        assertEquals(new NetElementID(arcConnection.getID()), transition.getID());
    }

    /**
     * Tests the compilation of two simple but full automata.
     * The test concerns the mapping of all graphical to semantical elements.
     * @throws SyntaxException
     */
    @Test
    public void testAutomataCompilation() throws SyntaxException {
        // DFA without inscriptions
        String dfa = "DFA-noInscr.fa";
        File testDFA = new File(samplesFolder, dfa);
        FADrawing drawing = (FADrawing) DrawingFileHelper.loadDrawing(testDFA,
                                                                      this);
        FigureEnumeration figs = drawing.figures();
        FAShadowLookupExtension faLookup = compileFADrawing(drawing);

        assertTrue(allFiguresHaveNetElements(faLookup));

        // DFA with inscriptions
        dfa = "DFA-inscr.fa";
        testDFA = new File(samplesFolder, dfa);
        drawing = (FADrawing) DrawingFileHelper.loadDrawing(testDFA, this);
        figs = drawing.figures();
        faLookup = compileFADrawing(drawing);

        assertTrue(allFiguresHaveNetElements(faLookup));
        assertTrue(allFiguresHaveInscriptions(faLookup));
    }

    public boolean allFiguresHaveInscriptions(FAShadowLookupExtension faLookup) {
        // Check correct state compilation (there should be any states)
        Enumeration<ShadowFAState> shadowStates = faLookup.allFAStates();
        assertTrue(shadowStates.hasMoreElements());

        ShadowFAState shState;
        Place place;
        FAStateFigure stateFig;
        while (shadowStates.hasMoreElements()) {
            shState = shadowStates.nextElement();
            Iterator<ShadowNetElement> iterator = shState.elements().iterator();
            while (iterator.hasNext()) {
                ShadowNetElement elem = iterator.next();
                if (elem instanceof ShadowInscription) {
                    FATextFigure faText = (FATextFigure) elem.context;
                    assertTrue(faText.getType() == CPNTextFigure.NAME);
                }
            }
        }


        // Check correct arc compilation (there should be any arcs)
        Enumeration<ShadowFAArc> shadowArcs = faLookup.allFAArcs();
        assertTrue(shadowArcs.hasMoreElements());

        ShadowFAArc shArc;
        Transition transition;
        FAArcConnection arcConnection;
        while (shadowArcs.hasMoreElements()) {
            shArc = shadowArcs.nextElement();
            Iterator<ShadowNetElement> iterator = shArc.elements().iterator();
            while (iterator.hasNext()) {
                ShadowNetElement elem = iterator.next();
                if (elem instanceof ShadowInscription) {
                    assertTrue(elem.context instanceof FATextFigure);
                    FATextFigure faText = (FATextFigure) elem.context;
                    assertTrue(faText.parent() instanceof FAArcConnection);
                }
            }
        }
        return true;
    }

    /**
     * Checks whether all graphical elements in the lookup map to a
     * semantical element.
     * @param faLookup - Mapping of fa elements
     * @return
     */
    public boolean allFiguresHaveNetElements(FAShadowLookupExtension faLookup) {
        boolean missingNetElement = true;


        // Check correct state compilation
        Enumeration<ShadowFAState> shadowStates = faLookup.allFAStates();
        assertTrue(shadowStates.hasMoreElements());

        ShadowFAState shState;
        Place place;
        FAStateFigure stateFig;
        while (shadowStates.hasMoreElements()) {
            shState = shadowStates.nextElement();
            stateFig = (FAStateFigure) shState.context;
            place = faLookup.get(shState);

            missingNetElement = missingNetElement && (place != null);

            assertNotNull(place);
            assertEquals(new NetElementID(shState.getID()), place.getID());
            assertEquals(stateFig.getID(), shState.getID());
        }


        // Check correct state compilation
        Enumeration<ShadowFAArc> shadowArcs = faLookup.allFAArcs();
        assertTrue(shadowArcs.hasMoreElements());

        ShadowFAArc shArc;
        Transition transition;
        FAArcConnection arcConnection;
        while (shadowArcs.hasMoreElements()) {
            shArc = shadowArcs.nextElement();
            arcConnection = (FAArcConnection) shArc.context;
            transition = faLookup.get(shArc);

            missingNetElement = missingNetElement && (transition != null);

            assertNotNull(transition);
            assertEquals(new NetElementID(shArc.getID()), transition.getID());
            assertEquals(arcConnection.getID(), shArc.getID());
        }

        return missingNetElement;
    }

    /**
     * Compiles the given FADrawing and returns the FAShadowLookupExtension
     * for getting the places and transitions for the corresponding
     * FAStateFigures and FAArcConnections.
     * @param drawing - To be compiled
     * @return Mapping of graphical to semantical elements
     * @throws SyntaxException
     */
    private FAShadowLookupExtension compileFADrawing(FADrawing drawing) {
        ShadowNet shNet = drawing.buildShadow(netSystem);
        ShadowLookup lookup = null;
        try {
            lookup = netSystem.compile();
        } catch (SyntaxException e) {
            lookup = new ShadowLookup();
        }

        return FAShadowLookupExtension.lookup(lookup);
    }

    @Override
    public void showStatus(String message) {
        System.out.println("Test >>" + message);
    }
}