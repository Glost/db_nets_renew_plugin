package de.renew.lola.test;

import junit.framework.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.gui.CPNDrawing;
import de.renew.gui.TransitionFigure;

import de.renew.lola.LolaAnalyzer;
import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaHelper;
import de.renew.lola.LolaResult;
import de.renew.lola.LolaTask;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LolaTest implements StatusDisplayer {
    File testNet;
    private List<File> tasknets;
    private int taskTests;
    private Map<File, List<String>> validTasks;
    private Map<String, Integer> taskResults;
    private static File samplesFolder;
    private static File libFolder;


    /**
     * Finds the samples folder
     */
    @BeforeClass
    public static void setUpOnce() {
        File location = new File("./").getAbsoluteFile().getParentFile();
        System.out.println("PWD is " + location.getAbsolutePath());
        System.out.println("Name is " + location.getName());
        File lolaDir = null;
        if (location.getName().equals("Lola")) {
            lolaDir = location.getParentFile();
        } else {
            lolaDir = location;
        }

//        List<File> roots = Arrays.asList(File.listRoots());
//        for (File loc = location; !roots.contains(loc);
//                     loc = loc.getParentFile()) {
//            System.out.println("ROOTS: " + roots + " : " + " LOC: " + loc.getPath());
//            System.out.println(Arrays.asList(location.list()).get(0));
//            if (Arrays.asList(location.list()).contains("Lola")) {
//                lolaDir = loc;
//                break;
//            }
//        }
        if (lolaDir != null) {
            samplesFolder = new File(lolaDir, "Lola/samples");
            libFolder = new File(lolaDir, "Lola/lib");
            System.out.println("samples Folder is "
                               + samplesFolder.getAbsolutePath());
        } else {
            Assert.fail("Could not find samples folder");
        }
    }

    @Test
    public void testBoundedPlaces() {
        //URL testNetUrl = LolaTest.class.getResource("/samples/test-bounded-places.rnw");
        String netName = "test-bounded-places.rnw";
        testNet = new File(samplesFolder, netName);
        System.out.println("Checking net in " + testNet.getAbsolutePath());
        if (testNet.exists()) {
            Map<String, Integer> places2results = new HashMap<String, Integer>();
            places2results.put("p1", 1);
            places2results.put("p2", 0);
            places2results.put("p3", 1);
            places2results.put("notexisting", 3);
            System.out.println(places2results);
            //ArrayList<String> places = new ArrayList<String>(Arrays.asList("p1","p2")); 
            Drawing drawing = DrawingFileHelper.loadDrawing(testNet, this);
            File netFile = LolaFileCreator.writeTemporaryLolaFile((CPNDrawing) drawing);
            LolaAnalyzer analyzer = new LolaAnalyzer(libFolder.toString());

            for (String p : places2results.keySet()) {
                LolaResult bounded = analyzer.checkPlace(p, netFile);
                int i = places2results.get(p);
                int j = bounded.getExitValue();
                System.out.println("Expected: " + i + " - Returned: " + j);
                Assert.assertEquals(i, j);
            }
        } else {
            Assert.fail("Net not found");
        }
    }

    @Test
    public void testDeadTransitions() {
        String netName = "test-dead-transitions.rnw";
        testNet = new File(samplesFolder, netName);
        System.out.println("Checking net in " + testNet.getAbsolutePath());
        if (testNet.exists()) {
            Map<String, Integer> transitions2results = new HashMap<String, Integer>();
            transitions2results.put("t1", 0);
            transitions2results.put("t2", 0);
            transitions2results.put("t3", 0);
            transitions2results.put("t4", 0);
            transitions2results.put("t5", 0);
            transitions2results.put("t6", 1);
            transitions2results.put("t7", 1);
            transitions2results.put("notexisting", 3);
            System.out.println(transitions2results);
            Drawing drawing = DrawingFileHelper.loadDrawing(testNet, this);
            File netFile = LolaFileCreator.writeTemporaryLolaFile((CPNDrawing) drawing);
            LolaAnalyzer analyzer = new LolaAnalyzer(libFolder.toString());

            for (String p : transitions2results.keySet()) {
                LolaResult bounded = analyzer.checkTransition(p, netFile);
                int i = transitions2results.get(p);
                int j = bounded.getExitValue();
                System.out.println("Expected: " + i + " - Returned: " + j);
                Assert.assertEquals(i, j);
            }
        } else {
            Assert.fail("Net not found");
        }
    }

    @Before
    public void setupTaskRelated() {
        String task1 = "ANALYSE MARKING p_end1: 4, p_start2: 1, p_end2: 1";
        String task2 = LolaHelper.placeKey + " p1";
        String task3 = LolaHelper.placeKey + " p_end1";
        String task4 = "ANALYSE MARKING p_end1: 4, p_end2: 1";

        tasknets = new ArrayList<File>();
        File net1 = new File(samplesFolder, "test-tasks-marking.rnw");
        tasknets.add(net1);

        validTasks = new HashMap<File, List<String>>();
        List<String> validTasksForNet1 = new ArrayList<String>();
        validTasksForNet1.add(task1);
        validTasksForNet1.add(task2);
        validTasksForNet1.add(task3);
        validTasksForNet1.add(task4);

        taskResults = new HashMap<String, Integer>();
        taskResults.put(task1, 0); // marking (0,4,1,1) is not reachable
        taskResults.put(task2, 3); // syntax error p1 does not exist
        taskResults.put(task3, 1); // place is bounded
        taskResults.put(task4, 1); // marking (0,4,0,1) is reachable


        validTasks.put(net1, validTasksForNet1);
        taskTests = 1;
    }

    /**
     * Reads all sample _tasknets, creates LolaTask objects for all
     * TextFigures in the net, writes them into lola task files and
     * checks them with lola.
     */
    @Test
    public void testLolaTask() {
        assertTrue("Number of task tests should match number of test nets.",
                   tasknets.size() == taskTests);
        System.out.println("--------------------- testLolaTask ----------------------");
        LolaAnalyzer analyzer = new LolaAnalyzer(libFolder.getPath());
        for (File f : tasknets) {
            Drawing taskDrawing = DrawingFileHelper.loadDrawing(f,
                                                                (StatusDisplayer) this);
            if (!(taskDrawing instanceof CPNDrawing)) {
                fail("Net " + f + " is not a CPNDrawing");
            }
            File netFile = LolaFileCreator.writeTemporaryLolaFile((CPNDrawing) taskDrawing);
            FigureEnumeration figs = taskDrawing.figures();
            while (figs.hasMoreElements()) {
                Figure fig = figs.nextElement();
                if (fig instanceof TextFigure) {
                    LolaTask task = new LolaTask((TextFigure) fig,
                                                 (CPNDrawing) taskDrawing);
                    System.out.println("=======================================\n "
                                       + task.toString());
                    System.out.println(((TextFigure) fig).getText() + " is "
                                       + (task.isValid() ? "valid" : "invalid"));
                    if (task.isValid()) {
                        //doesn't work, no Lola plugin means path to lib not set
                        //LolaResult output = task.check(); 
                        File taskFile = task.writeToFile();
                        String lolaCommand = LolaHelper.taskCommandMap.get(task
                                                                           .getType());
                        LolaResult result = analyzer.checkTask(taskFile,
                                                               netFile,
                                                               lolaCommand);
                        System.out.println("[Text Figure] "
                                           + ((TextFigure) fig).getText());
                        System.out.println("[TaskResult Expected] "
                                           + taskResults.get(((TextFigure) fig)
                                                             .getText())
                                                        .intValue());
                        System.out.println("[Result] " + result.getExitValue());
                        assertEquals("Result must match expected result",
                                     result.getExitValue(),
                                     taskResults.get(((TextFigure) fig).getText())
                                                .intValue());
                    }
                }
            }
        }
    }

    /**
     * Tests the parsing of textFigures into {@link LolaTask}'s.
     */
    @Test
    public void testTaskValidity() {
        for (File f : tasknets) {
            Drawing taskDrawing = DrawingFileHelper.loadDrawing(f,
                                                                (StatusDisplayer) this);
            if (!(taskDrawing instanceof CPNDrawing)) {
                fail("Net " + f + " is not a CPNDrawing");
            }
            List<String> valid = validTasks.get(f);
            FigureEnumeration figs = taskDrawing.figures();
            while (figs.hasMoreElements()) {
                Figure fig = figs.nextElement();
                if (fig instanceof TextFigure) {
                    LolaTask task = new LolaTask((TextFigure) fig,
                                                 (CPNDrawing) taskDrawing);
                    if (valid.contains(((TextFigure) fig).getText())) {
                        assertTrue("Task " + task + " should be valid",
                                   task.isValid());
                    } else {
                        assertFalse("Task " + task + " should be invalid",
                                    task.isValid());
                    }
                }
            }
        }
    }

    @Test
    public void testLiveTransitions() {
        String netName = "simple-unbounded.rnw";
        testNet = new File(samplesFolder, netName);
        System.out.println("Checking net in " + testNet.getAbsolutePath());
        if (testNet.exists()) {
            Map<String, Integer> transitions2results = new HashMap<String, Integer>();
            transitions2results.put("t1", 0);
            transitions2results.put("notexisting", 3);
            System.out.println(transitions2results);
            Drawing drawing = DrawingFileHelper.loadDrawing(testNet, this);
            FigureEnumeration figs = drawing.figures();
            File netFile = LolaFileCreator.writeTemporaryLolaFile((CPNDrawing) drawing);
            LolaAnalyzer analyzer = new LolaAnalyzer(libFolder.toString());

            for (String p : transitions2results.keySet()) {
                TransitionFigure found = null;
                while (figs.hasMoreElements()) {
                    Figure fig = figs.nextFigure();
                    if (fig instanceof TransitionFigure) {
                        FigureEnumeration childs = ((TransitionFigure) fig)
                                                       .children();
                        while (childs.hasMoreElements()) {
                            Figure textFig;
                            if ((textFig = childs.nextFigure()) instanceof TextFigure
                                        && ((TextFigure) textFig).getText()
                                                    .equals(p)) {
                                found = (TransitionFigure) fig;
                                break;
                            }
                        }
                    }
                }
                if (found != null) {
                    System.out.println(found);
                    LolaResult live = analyzer.checkTransitionLiveness(found,
                                                                       netFile,
                                                                       drawing);
                    int i = transitions2results.get(p);
                    int j = live.getExitValue();
                    System.out.println("Expected: " + i + " - Returned: " + j);
                    System.out.println(live.getOutput());
                    Assert.assertEquals(i, j);
                }
            }
        } else {
            Assert.fail("Net not found");
        }
    }

    @Override
    public void showStatus(String message) {
        // TODO Auto-generated method stub
        System.out.println(">>" + message);
    }
}