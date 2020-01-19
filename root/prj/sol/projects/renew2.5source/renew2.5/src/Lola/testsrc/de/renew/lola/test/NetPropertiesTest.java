/**
 *
 */
package de.renew.lola.test;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StatusDisplayer;

import de.renew.gui.CPNDrawing;

import de.renew.lola.LolaAnalyzer;
import de.renew.lola.LolaFileCreator;
import de.renew.lola.LolaHelper;
import de.renew.lola.LolaResult;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;


/** Tests properties of the given sample rnw files against the desired properties.
 *  This test can be run from junit/eclipse as well as ant task.
 *
 * @author Cabac
 *
 */
public class NetPropertiesTest implements StatusDisplayer {
    static private File[] _files;
    static private HashMap<String, File> _netfiles = new HashMap<String, File>();
    static private HashMap<String, CPNDrawing> _netDrawings = new HashMap<String, CPNDrawing>();
    static private LolaAnalyzer _analyzer;
    static private HashMap<String, Integer> _boundedness = new HashMap<String, Integer>();
    static private HashMap<String, Integer[]> nets2results = new HashMap<String, Integer[]>();
    static private String _hereName;
    static private int _numTestNets;

    /** Determine Renew Checkout directory.
     * Prepare properties:
     * - boundedness
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpOnce() throws Exception {
        // trying to find the Renew checkout directory
        // - pwd for running in eclipse is Renew/ (default)
        // - pwd for running ant test is Renew/Lola/
        File here = new File("./");
        _hereName = here.getAbsolutePath();
        System.out.println("PWD is " + _hereName);
        while (!_hereName.endsWith("Renew")) {
            _hereName = _hereName.substring(0, _hereName.lastIndexOf('/'));
            System.out.println("PWD is " + _hereName);
        }
        nets2results.put("bounded-live-reversible.rnw",
                         new Integer[] { 1, 0, 1 });
        nets2results.put("bounded-live-irreversible.rnw",
                         new Integer[] { 1, 0, 0 });
        nets2results.put("bounded-nonlive-reversible.rnw",
                         new Integer[] { 1, 1, 1 });
        nets2results.put("bounded-nonlive-irreversible.rnw",
                         new Integer[] { 1, 1, 0 });
        nets2results.put("unbounded-live-reversible.rnw",
                         new Integer[] { 0, 0, 1 });
        nets2results.put("unbounded-live-irreversible.rnw",
                         new Integer[] { 0, 1, 0 });
        nets2results.put("unbounded-nonlive-reversible.rnw",
                         new Integer[] { 0, 1, 5 });
        nets2results.put("unbounded-nonlive-irreversible.rnw",
                         new Integer[] { 0, 1, 5 });
        // for liveness 1 means live, 0 means not live
        _boundedness.put("bounded-live-reversible.rnw", new Integer(1));
        _boundedness.put("bounded-nonlive-irreversible.rnw", new Integer(1));
        _boundedness.put("simple-deadlock.rnw", new Integer(1));
        _boundedness.put("simple-unbounded.rnw", new Integer(0));
        _boundedness.put("unbounded-live-irreversible.rnw", new Integer(0));
        _boundedness.put("unbounded-live-reversible.rnw", new Integer(0));
        _numTestNets = 19;


        //exportTest();
        _analyzer = new LolaAnalyzer(_hereName + "/Lola/lib/");

    }

    /**
     * Erase temporary files
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownOnce() throws Exception {
        Iterator<String> iterator = _netfiles.keySet().iterator();
        while (iterator.hasNext()) {
            String net = (String) iterator.next();
            _netfiles.get(net).delete();
        }
    }

    /** Read rnw files from Renew/Lola/samples, prepare in hashmap
     * <code>_netfiles</code> and export to temporary net file.
     * @throws IOException
     */
    @Test
    public void exportTest() throws IOException {
        // Read rnw files from Lola/samples, prepare in hashmap netfiles and export to temporary net file
        File dir = new File(_hereName + "/Lola/samples");
        System.out.println(dir.getCanonicalPath());
        if (dir.isDirectory()) {
            _files = dir.listFiles();
            for (int i = 0; i < _files.length; i++) {
                File file = _files[i];
                if (file.getName().endsWith("rnw")) {
                    System.out.println("" + i + " " + _files[i]);
                    Drawing loadDrawing = DrawingFileHelper.loadDrawing(file,
                                                                        (StatusDisplayer) this);
                    File temporaryLolaFile = LolaFileCreator
                                                 .writeTemporaryLolaFile((CPNDrawing) loadDrawing);
                    _netDrawings.put(file.getName(), (CPNDrawing) loadDrawing);
                    _netfiles.put(file.getName(), temporaryLolaFile);
                }
            }
        }
        System.out.println("netfiles = " + _netfiles);
        System.out.println("netfiles = " + _netfiles.keySet().size());

        assertEquals(_netfiles.keySet().size(), _numTestNets);
    }

    /**
     * Check of boundedness properties for 6 rnw files
     */
    @Test
    public void checkBoundednessTest() {
        System.out.println("netfiles117 = " + _netfiles);
        for (String net : nets2results.keySet()) {
            File tempLolaFile = _netfiles.get(net);
            System.out.println("Net: " + net + " - Lola file: " + tempLolaFile);
            LolaResult checkNet = _analyzer.checkNet(LolaHelper.netBoundedCommand,
                                                     tempLolaFile);
            System.out.println("Result for property \"bounded\" is "
                               + checkNet.getExitValue());
            assertEquals(checkNet.getExitValue(), (int) nets2results.get(net)[0]);
        }
    }

    @Test
    public void checkLivenessTest() {
        System.out.println("=================loaded Net Drawings===========================");
        for (String name : _netDrawings.keySet()) {
            System.out.println(name);
            System.out.println(_netDrawings.get(name));
        }

        for (String net : nets2results.keySet()) {
            File tempLolaFile = _netfiles.get(net);
            System.out.println("Net: " + net + " - Lola file: " + tempLolaFile);
            CPNDrawing drawing = _netDrawings.get(net);
            System.out.println("Drawing      = " + drawing);
            System.out.println("Drawing Name = " + drawing.getName());
            LolaResult checkNet = _analyzer.checkNetLiveness(drawing);
//            LolaResult checkNet = _analyzer.checkNet(LolaHelper.livepropCommand,
//                                                     tempLolaFile);
            System.out.println("Result for property \"live\" is "
                               + checkNet.getExitValue());
            assertEquals((int) nets2results.get(net)[1], checkNet.getExitValue());
        }
    }

    @Override
    public void showStatus(String message) {
        System.out.println(message);

    }
}