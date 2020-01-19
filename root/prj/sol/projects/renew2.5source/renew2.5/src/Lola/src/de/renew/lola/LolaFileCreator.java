/**
 *
 */
package de.renew.lola;

import CH.ifa.draw.figures.AttributeFigure;
import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;
import CH.ifa.draw.framework.FigureFilter;
import CH.ifa.draw.framework.FigureWithID;

import CH.ifa.draw.standard.FilteredFigureEnumerator;

import de.renew.formalism.FormalismPlugin;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNDrawing;
import de.renew.gui.CPNTextFigure;
import de.renew.gui.PlaceFigure;
import de.renew.gui.TransitionFigure;
import de.renew.gui.VirtualPlaceFigure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


/**
 *
 * @author hewelt, wagner
 *
 */
public class LolaFileCreator {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(LolaFileCreator.class);
    private HashMap<Figure, String> usedFigureNames = new HashMap<Figure, String>();
    protected HashMap<TransitionFigure, HashMap<PlaceFigure, Integer>> preAll = new HashMap<TransitionFigure, HashMap<PlaceFigure, Integer>>();
    protected HashMap<TransitionFigure, HashMap<PlaceFigure, Integer>> postAll = new HashMap<TransitionFigure, HashMap<PlaceFigure, Integer>>();
    private boolean ptnetcompiler;

    /**
     * Writes the drawing into the stream.
     *
     * @param OutputStream
     *            stream
     * @param CPNDrawing
     *            drawing
     */
    public void writeLolaFile(OutputStream stream, CPNDrawing drawing) {
        writeLolaFile(stream, drawing, new StringBuffer(""));
    }

    public LolaFileCreator() {
        super();
        // if the PT formalism is selected calculate weight using integers
        FormalismPlugin plugin = FormalismPlugin.getCurrent();
        if (plugin != null) {
            String compiler = plugin.getCompiler();
            if (logger.isInfoEnabled()) {
                logger.info("[Lola] Using compiler: " + compiler);
                if (compiler.equals(FormalismPlugin.PT_COMPILER)) {
                    ptnetcompiler = true;
                }
            }
        } else {
            ptnetcompiler = true;
        }
    }

    /**
     * Writes the drawing into the stream. Additional parameter to manually add
     * an initial marking
     *
     * @param OutputStream
     *            stream
     * @param CPNDrawing
     *            drawing
     * @param StringBuffer
     *            initialMarking
     */
    public void writeLolaFile(OutputStream stream, CPNDrawing drawing,
                              StringBuffer initialMarking) {
        // check if net is non-empty
        if (emptyNet(drawing)) {
            logger.error("[Lola Export] Net drawing needs at least 1 place and 1 transition.");
            return;
        }
        logger.info("[Lola Export] Converting drawing to lola net file format");
        if (logger.isDebugEnabled()) {
            logger.debug("  Extracting Places");
        }
        StringBuffer placeBuffer = extractPlaces(drawing);
        StringBuffer markingBuffer = initialMarking;
        if (initialMarking.toString().equals("")) {
            if (logger.isDebugEnabled()) {
                logger.debug("  Extracting Marking");
            }
            markingBuffer = extractMarking(drawing);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("  Extracting Transitions");
        }
        StringBuffer transitionsBuffer = extractTransitions(drawing);
        try {
            stream.write(placeBuffer.toString().getBytes());
            stream.write(markingBuffer.toString().getBytes());
            stream.write(transitionsBuffer.toString().getBytes());
        } catch (IOException e) {
            logger.error("[Lola] Writing to output stream failed");
            e.printStackTrace();
        }
    }

    /**
     * Tests whether the given drawing has no places or no transitions. In both
     * cases lola can't handle the exported net file.
     *
     * @param drawing
     * @return true if net is empty, false otherwise
     */
    private boolean emptyNet(CPNDrawing drawing) {
        int p = 0;
        int t = 0;
        FigureEnumeration figs = drawing.figures();
        while (figs.hasMoreElements()) {
            Figure fig = figs.nextElement();
            if (fig instanceof PlaceFigure) {
                p++;
            }
            if (fig instanceof TransitionFigure) {
                t++;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola] Net " + drawing.getName() + " has " + p
                         + " Places and " + t + " Transitions");
        }
        return (p < 1 || t < 1);
    }

    /**
     * This method takes care of the creation of a temporary lola file for a
     * given CPNDrawing.
     *
     * @param drawing
     *            - A CPNDrawing to be exported to lola format and written into
     *            a net file
     * @return netFile - The created temporary LolaFile
     */
    public static File writeTemporaryLolaFile(CPNDrawing drawing) {
        return writeTemporaryLolaFile(drawing, new StringBuffer(""));
    }

    /**
     * This method takes care of the creation of a temporary lola file for a
     * given CPNDrawing. This method also uses a custum, manually input initial
     * marking, which is ignored if it is the empty string.
     *
     * @param drawing
     *            - A CPNDrawing to be exported to lola format and written into
     *            a net file
     * @param initMarking
     *            - A manually input initial marking
     * @return netFile - The created temporary LolaFile
     */
    public static File writeTemporaryLolaFile(CPNDrawing drawing,
                                              StringBuffer initMarking) {
        File netFile = null;
        OutputStream fileStream = null;
        LolaFileCreator creator = new LolaFileCreator();
        String netName = drawing.getName();
        try {
            netFile = File.createTempFile("renew" + netName, ".net",
                                          LolaHelper.findTmpDir());
            if (logger.isInfoEnabled()) {
                logger.info("[Lola] temporay lola net file: "
                            + netFile.getAbsolutePath());
            }
            fileStream = new FileOutputStream(netFile);
            creator.writeLolaFile(fileStream, drawing, initMarking);
        } catch (FileNotFoundException e) {
            logger.error("[Lola] Could not create stream to write lola file");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("[Lola] Could not create temporary lola file in directory "
                         + LolaHelper.findTmpDir());
            e.printStackTrace();
        }
        return netFile;
    }

    /**
     * Writes verification requests (e.g. ANALYSE MARKING) found in the drawing
     * to separate .task files. We support the following requests: ANALYSE
     * MARKING, ANALYSE PLACE, ANALYSE TRANSITION and FORMULA
     *
     * @param path
     *            - where to store task files
     * @param CPNDrawing
     *            drawing
     */
    public void createTasks(CPNDrawing drawing, File path) {
        logger.info("[Lola] Start writing task files");
        logger.info("[Lola] Extracting ANALYSE MARKING Tasks");
        // taskFilePath is the absolute path given as a parameter
        String taskFilePath = path.getAbsolutePath();

        // taskFileBaseName is the base name e.g. path/netname without ".net"
        String taskFileBaseName = taskFilePath.substring(0,
                                                         taskFilePath
                                      .lastIndexOf(".net"));
        HashMap<String, ArrayList<String>> taskMap = extractAllTasks(drawing);
        for (String key : taskMap.keySet()) {
            ArrayList<String> tempList = taskMap.get(key);
            if (tempList != null) {
                writeTaskFiles(taskFileBaseName, tempList,
                               LolaHelper.taskFilenameMap.get(key));
            }
        }

        //        writeTaskFiles(taskFileBaseName, taskMap.get(LolaHelper.markingKey), LolaHelper.markingFileName);
        //        writeTaskFiles(taskFileBaseName, taskMap.get(LolaHelper.placeKey), LolaHelper.placeFileName);
        //        writeTaskFiles(taskFileBaseName, taskMap.get(LolaHelper.transitionKey), LolaHelper.transitionFileName);
        //        writeTaskFiles(taskFileBaseName, taskMap.get(LolaHelper.stateFormulaKey), LolaHelper.stateFormulaFileName);
        //        writeTaskFiles(taskFileBaseName, taskMap.get(LolaHelper.CTLFormulaKey), LolaHelper.CTLFormulaFileName);
        //        
        //        ArrayList<String> markingTasks = extractTasks(drawing, "MARKING");
        //        writeTaskFiles(taskFileBaseName, markingTasks, "Marking");
        //        ArrayList<String> transitionTasks = extractTasks(drawing, "TRANSITION");
        //        writeTaskFiles(taskFileBaseName, transitionTasks, "Transition");
        //        ArrayList<String> placeTasks = extractTasks(drawing, "PLACE");
        //        writeTaskFiles(taskFileBaseName, placeTasks, "Place");
        //        ArrayList<String> formulaTasks = extractStatePredicateFormulas(drawing);
        //        writeTaskFiles(taskFileBaseName, formulaTasks, "StatePredicate");
        //        ArrayList<String> CTLTasks = extractCTLFormulas(drawing);
        //        writeTaskFiles(taskFileBaseName, CTLTasks, "CTL");
    }

    /**
     * Writes a single task to a file. Internally calls writeTaskFiles with the
     * task converted into an one element ArrayList
     *
     * @param taskFileBaseName
     *            base name for the files (e.g. path/filename without ".net")
     * @param task
     *            task to be written into file
     * @param type
     *            type of the task (e.g. marking, place, etc.)
     */
    public File writeTaskFile(String taskFileBaseName, String task, String type) {
        ArrayList<String> taskList = new ArrayList<String>();
        taskList.add(task);
        if (logger.isDebugEnabled()) {
            logger.debug("LolaFileCreator.writeTaskFile: Writing single task to "
                         + taskFileBaseName);
        }
        ArrayList<File> files = writeTaskFiles(taskFileBaseName, taskList, type);

        return files.get(0);
    }

    /**
     * Writes the actual task files
     *
     * @param taskFileBaseName
     *            base name for the files (e.g. path/filename without ".net")
     * @param tasks
     *            Arraylist of Tasks to be written
     * @param type
     *            type of tasks to be analysed (e.g. marking, place, transition)
     */
    private ArrayList<File> writeTaskFiles(String taskFileBaseName,
                                           ArrayList<String> tasks, String type) {
        String taskText;

        // taskFileSpecificName will be appended for each new file with "runningnumber.task"
        String taskFileSpecificName;
        ArrayList<File> files = new ArrayList<File>();

        // For each found markingTask create a new file (.task) and write the specified marking predicate
        int i = 1;
        Iterator<String> markIt = tasks.iterator();
        while (markIt.hasNext()) {
            taskText = markIt.next();
            taskFileSpecificName = taskFileBaseName + type + i + ".task";
            logger.info("[Lola] Write task to file: " + taskFileSpecificName);
            logger.info("[Lola] Task Text: " + taskText);
            File tempFile = new File(taskFileSpecificName);
            files.add(tempFile);

            OutputStream stream;
            try {
                stream = new FileOutputStream(tempFile);
                stream.write(taskText.getBytes());
                stream.flush();
                stream.close();
            } catch (FileNotFoundException e) {
                logger.error("[Lola] Creating file output stream failed");
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("[Lola] Writing to file output stream failed");
                e.printStackTrace();
            }
            i = i + 1;
        }
        return files;
    }

    /**
     * Determines for a TextFigure which type of verification task it is.
     *
     * @param fig
     * @return The type or ""
     */
    public String parseTask(TextFigure fig) {
        String result = "";
        String request = fig.getText();
        logger.debug("[Lola] Parsing verification task " + request);
        if (((TextFigure) fig).parent() == null) {
            if (request.startsWith(LolaHelper.markingKey)) {
                result = LolaHelper.taskFilenameMap.get(LolaHelper.markingKey);
            } else if (request.startsWith(LolaHelper.transitionKey)) {
                result = LolaHelper.transitionFileName;
            } else if (request.startsWith(LolaHelper.placeKey)) {
                result = LolaHelper.placeFileName;
            } else if (request.startsWith(LolaHelper.formulaKey)) {
                if (LolaHelper.ctlp(request)) {
                    result = LolaHelper.CTLFormulaFileName;
                } else {
                    result = LolaHelper.stateFormulaFileName;
                }
            } else {
                logger.error("[Lola] Didn't recognize the type of the task.");
            }
        }
        return result;
    }

    /**
     * Exctracts all different types of verification tasks at once from the
     * given drawing. It checks all TextFigures whether they start with a
     * keyword and if so adds them into a hashmap.
     *
     * @param drawing
     * @return a hashmap with different types of tasks as keys and ArrayList of
     *         found TextFigures
     */
    private HashMap<String, ArrayList<String>> extractAllTasks(CPNDrawing drawing) {
        HashMap<String, ArrayList<String>> allTasks = new HashMap<String, ArrayList<String>>();
        allTasks.put(LolaHelper.markingKey, new ArrayList<String>());
        allTasks.put(LolaHelper.placeKey, new ArrayList<String>());
        allTasks.put(LolaHelper.transitionKey, new ArrayList<String>());
        allTasks.put(LolaHelper.stateFormulaKey, new ArrayList<String>());
        allTasks.put(LolaHelper.CTLFormulaKey, new ArrayList<String>());
        //allTasks.put(LolaHelper.assertKey, new ArrayList<String>());
        for (FigureEnumeration it = drawing.figures(); it.hasMoreElements();) {
            Figure figure = it.nextFigure();
            if (figure instanceof TextFigure) {
                String tempText = ((TextFigure) figure).getText();
                if (tempText.startsWith(LolaHelper.markingKey)) {
                    allTasks.get(LolaHelper.markingKey).add(tempText);
                }
                if (tempText.startsWith(LolaHelper.transitionKey)) {
                    allTasks.get(LolaHelper.transitionKey).add(tempText);
                }
                if (tempText.startsWith(LolaHelper.placeKey)) {
                    allTasks.get(LolaHelper.placeKey).add(tempText);
                }
                if (tempText.startsWith(LolaHelper.formulaKey)) {
                    if (LolaHelper.ctlp(tempText)) {
                        allTasks.get(LolaHelper.CTLFormulaKey).add(tempText);
                    } else {
                        allTasks.get(LolaHelper.stateFormulaKey).add(tempText);
                    }
                }
                if (tempText.startsWith(LolaHelper.assertKey)) {
                    Figure parent = ((TextFigure) figure).parent();
                    String parentName = name((FigureWithID) parent);
                    String assertTask = "";
                    if (parent instanceof PlaceFigure) {
                        assertTask = LolaHelper.placeKey + " " + parentName;
                        allTasks.get(LolaHelper.placeKey).add(assertTask);
                    } else if (parent instanceof TransitionFigure) {
                        assertTask = LolaHelper.transitionKey + " "
                                     + parentName;
                        allTasks.get(LolaHelper.transitionKey).add(assertTask);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Lola] Export: Found assert task: "
                                     + assertTask);
                    }
                }
            }
        }
        return allTasks;
    }

    /**
     * @param drawing
     */
    private StringBuffer extractTransitions(CPNDrawing drawing) {
        StringBuffer buffer = new StringBuffer();
        Boolean check = false;
        for (FigureEnumeration it = drawing.figures(); it.hasMoreElements();) {
            Figure figure = it.nextFigure();
            if (figure instanceof TransitionFigure) {
                TransitionFigure trans = (TransitionFigure) figure;
                buffer.append("TRANSITION " + name(trans) + "{" + "x:"
                              + (int) trans.displayBox().getX() + "y:"
                              + (int) trans.displayBox().getY() + "}" + "\n");

                HashMap<String, Integer> preset = preset(drawing, trans);
                buffer.append("CONSUME\n  ");
                check = false;
                for (Iterator<String> iter = preset.keySet().iterator();
                             iter.hasNext();) {
                    if (check) {
                        buffer.append(",\n  ");
                    } else {
                        check = true;
                    }
                    String placeName = (String) iter.next();
                    buffer.append(placeName + ": " + preset.get(placeName));

                }
                buffer.append(";\n\n");

                HashMap<String, Integer> postset = postset(drawing, trans);
                buffer.append("PRODUCE\n  ");
                check = false;
                for (Iterator<String> iter = postset.keySet().iterator();
                             iter.hasNext();) {
                    if (check) {
                        buffer.append(",\n  ");
                    } else {
                        check = true;
                    }
                    String placeName = (String) iter.next();
                    buffer.append(placeName + ": " + postset.get(placeName));

                }
                buffer.append(";\n\n");

            }
        }
        return buffer;
    }

    /**
     * @param drawing
     *
     */
    private StringBuffer extractMarking(CPNDrawing drawing) {
        StringBuffer buffer = new StringBuffer();
        Boolean check = false;
        buffer.append("MARKING\n");
        for (FigureEnumeration iterator = drawing.figures();
                     iterator.hasMoreElements();) {
            Figure fig = iterator.nextFigure();
            if (fig instanceof PlaceFigure) {
                PlaceFigure place = (PlaceFigure) fig;
                if (logger.isDebugEnabled()
                            && place instanceof VirtualPlaceFigure) { // warn about virtual places
                    logger.debug("[Lola Export] Place " + name(place)
                                 + " is virtual.\n It is not allowed that both virtual and original place are marked.");
                }
                FigureEnumeration childs = place.children();
                StringBuffer marking = new StringBuffer();
                Boolean hasMarking = false;
                int tokenCounter = 0;
                // are we first entry, otherwise add colon and line break 
                marking.append(check ? ",\n  " : "  ");
                marking.append((place instanceof VirtualPlaceFigure)
                               ? name(place.getSemanticPlaceFigure())
                               : name(place));
                marking.append(": ");
                while (childs.hasMoreElements()) {
                    Figure child = childs.nextFigure();
                    if (child instanceof CPNTextFigure) { // only Textfigures...
                        String text = ((CPNTextFigure) child).getText();
                        if (((CPNTextFigure) child).getType() == CPNTextFigure.INSCRIPTION) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("[Lola Export: Extract Marking] Place "
                                             + name(place)
                                             + " has inscription " + text);
                            }
                            if (ptnetcompiler) {
                                if (logger.isInfoEnabled()) {
                                    logger.info(LolaFileCreator.class
                                        .getSimpleName()
                                                + ": Encountered ptnetcomp and number: "
                                                + text);
                                }
                                // this might throw a Numberformat exception, if the int is to big
                                tokenCounter = (text.matches("^[0-9]*$"))
                                               ? tokenCounter
                                               + Integer.parseInt(text)
                                               : tokenCounter + 1;
                            } else {
                                tokenCounter++;
                            }
                            hasMarking = true;
                        }
                    }
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("[Lola Export: Extract Marking] Place "
                                 + name(place) + " has " + tokenCounter
                                 + " token.");
                }
                marking.append(tokenCounter);
                if (hasMarking) {
                    check = true;
                    buffer.append(marking);
                }
            }
        }
        buffer.append(";\n\n");
        return buffer;
    }

    /**
     * @param drawing
     */
    private StringBuffer extractPlaces(CPNDrawing drawing) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("PLACE\n");
        boolean check = false;
        for (FigureEnumeration iterator = drawing.figures();
                     iterator.hasMoreElements();) {
            Figure fig = iterator.nextFigure();
            if (fig instanceof PlaceFigure
                        && !(fig instanceof VirtualPlaceFigure)) {
                if (check) {
                    buffer.append(",\n");
                } else {
                    check = true;
                }
                PlaceFigure place = (PlaceFigure) fig;
                buffer.append("  " + this.name(place) + "{" + "x:"
                              + (int) place.displayBox().getX() + "y:"
                              + (int) place.displayBox().getY() + "}");

            }
        }
        buffer.append(";\n\n");
        return buffer;
    }

    /**
     * Creates HashMap with names of the places that are connected with an arc
     * to this Transition trans in the Petri Net as the keys. They are either
     * the source of the arc or it is double arc/test arc in which case they
     * should be in the preset and the postset. So the source/destination
     * doesn't matter. (source/destination depends on where you started drawing)
     *
     * @param drawing
     * @param trans
     * @return result
     */
    private HashMap<String, Integer> preset(Drawing drawing,
                                            TransitionFigure trans) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (FigureEnumeration iterator = drawing.figures();
                     iterator.hasMoreElements();) {
            Figure fig = iterator.nextFigure();
            if (fig instanceof ArcConnection) { // look for arcs...
                ArcConnection arc = (ArcConnection) fig;
                Integer arcType = arc.getArcType();
                Figure end = arc.endFigure();
                Figure start = arc.startFigure();

                // ... ending at trans ...
                if (end.equals(trans) && start instanceof PlaceFigure) {
                    // arcSource = The place the arc starts at.
                    String arcSource = name(unvirtualize((PlaceFigure) start));
                    if (result.get(arcSource) == null) { // Check for multiple arcs consuming marks from this place.
                        result.put(arcSource, parseArcInscription(arc));
                    } else {
                        result.put(arcSource,
                                   parseArcInscription(arc)
                                   + result.get(arcSource));
                    }
                } // ... or being double-headed and starting at trans
                else if (start.equals(trans) && (arcType == 0 || arcType == 2)
                                 && end instanceof PlaceFigure) {
                    // doubleArcDestination = Place the double arc "ends" at.
                    String doubleArcDestination = name(unvirtualize((PlaceFigure) end));
                    if (result.get(doubleArcDestination) == null) { // Check for multiple arcs consuming marks from this place.
                        result.put(doubleArcDestination,
                                   parseArcInscription(arc));
                    } else {
                        result.put(doubleArcDestination,
                                   parseArcInscription(arc)
                                   + result.get(doubleArcDestination));
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola Export] Preset of " + name(trans) + " is: "
                         + result.toString());
        }
        return result;
    }

    private PlaceFigure unvirtualize(PlaceFigure fig) {
        return (fig instanceof VirtualPlaceFigure)
               ? fig.getSemanticPlaceFigure() : fig;
    }

    private Integer parseArcInscription(ArcConnection arc) {
        Integer cardinality = 1;
        boolean check = false;
        int weight = 0;
        for (FilteredFigureEnumerator textChilds = new FilteredFigureEnumerator(arc
                                                                                .children(),
                                                                                new FigureFilter() {
                public boolean isUsed(Figure fig) {
                    return (fig instanceof CPNTextFigure
                           && ((CPNTextFigure) fig).getType() == CPNTextFigure.INSCRIPTION)
                           ? true : false;
                }
            }); textChilds.hasMoreElements();) {
            check = true;
            Figure child = textChilds.nextFigure();
            String text = ((TextFigure) child).getText();
            if (ptnetcompiler) {
                //just take the first found inscription and return the value
                int ret = (text.matches("^[0-9]*$")) ? Integer.parseInt(text) : 1;
                return ret;
            } else {
            }
            String[] split = text.split(";");
            String emptyString = "";
            for (String string : split) {
                if (!(string.equals(emptyString))) {
                    weight++;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("[Lola Export] Parsing arc ("
                             + name((FigureWithID) arc.startFigure()) + ")--"
                             + text + "-->("
                             + name((FigureWithID) arc.endFigure())
                             + ")  -- Cardinality is " + cardinality);
            }
        }

        if (check) {
            cardinality = weight;
        }

        // TODO take care of arcs with multiple inscriptions
        return cardinality;
    }

    /**
     * Creates a HashMap with the names of the places that are connected with an
     * arc to this Transition trans as the keys. They are either the destination
     * of the arc or it is a double arc/test arc in which case it they should be
     * in the preset and the postset. So the source/destination doesn't matter.
     * (source/destination depends on where you started drawing)
     *
     * @param drawing
     * @param trans
     * @return result
     */
    private HashMap<String, Integer> postset(Drawing drawing,
                                             TransitionFigure trans) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (FigureEnumeration iterator = drawing.figures();
                     iterator.hasMoreElements();) {
            Figure fig = iterator.nextFigure();
            if (fig instanceof ArcConnection) { // looking for arcs ...
                ArcConnection arc = (ArcConnection) fig;
                Integer arcType = arc.getArcType();
                Figure end = arc.endFigure();
                Figure start = arc.startFigure();

                // ... starting at trans ...
                if (start.equals(trans) && end instanceof PlaceFigure) {
                    // arcDestination = the place the arc ends at.
                    String arcDestination = name(unvirtualize((PlaceFigure) end));
                    if (result.get(arcDestination) == null) {
                        result.put(arcDestination, parseArcInscription(arc));
                    } else {
                        result.put(arcDestination,
                                   parseArcInscription(arc)
                                   + result.get(arcDestination));
                    }
                } // ... or being double-headed and starting at trans
                else if (end.equals(trans) && (arcType == 0 || arcType == 2)
                                 && start instanceof PlaceFigure) {
                    // doubleArcSource = the Place the double arc was drawn from.
                    String doubleArcSource = name(unvirtualize((PlaceFigure) start));
                    if (result.get(doubleArcSource) == null) {
                        result.put(doubleArcSource, parseArcInscription(arc));
                    } else {
                        result.put(doubleArcSource,
                                   parseArcInscription(arc)
                                   + result.get(doubleArcSource));
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Lola Export] Postset of " + name(trans) + " is: "
                         + result.toString());
        }
        return result;
    }

    /**
     * Provides a name for given figure. If it has a user defined and unique
     * name, this is used. If some figures share a user defined name, the unique
     * figure id is added. Otherwise (no user defined name) we return the id
     * preceded with "p" or "t" for place, transition respectively.
     *
     * @param figure
     * @return String a name for the figure
     */
    public String name(FigureWithID figure) {
        StringBuffer retString = new StringBuffer();
        Boolean named = false;

        if (figure instanceof PlaceFigure || figure instanceof TransitionFigure) {
            //already named, use this name
            int id = figure.getID();
            if (usedFigureNames.containsKey(figure)) {
                retString.append(usedFigureNames.get(figure));
                // we have a name
                named = true;
            } else { // figure yet unnamed, search for name 
                FigureEnumeration childs = ((AttributeFigure) figure).children();
                while (childs.hasMoreElements() && named == false) { // loop for child elements...
                    Figure child = childs.nextElement();
                    if (child instanceof CPNTextFigure
                                && ((CPNTextFigure) child).getType() == CPNTextFigure.NAME) { // that are text figures of type name)
                        String figureName = ((CPNTextFigure) child).getText();
                        /*
                         * check for valid name: starts with letter, followed by
                         * letter or digit && is not one of the reserved
                         * keywords
                         */
                        String validName = "^\\p{Alpha}[\\p{Alnum}_-]*[0-9]*\\b";
                        String[] reserved = new String[] { "IF", "RECORD", "END", "SORT", "FUNCTION", "SAFE", "DO", "ARRAY", "STRONG", "WEAK", "FAIR", "ENUMERATE", "CONSTANT", "BOOLEAN", "OF", "BEGIN", "WHILE", "IF", "THEN", "ELSE", "SWITCH", "CASE", "NEXTSTEP", "REPEAT", "FOR", "TO", "ALL", "EXIT", "EXISTS", "RETURN", "TRUE", "FALSE", "MOD", "VAR", "GUARD", "STATE", "PATH", "GENERATOR", "ANALYSE", "PLACE", "TRANSITION", "MARKING", "CONSUME", "PRODUCE", "FORMULA", "EXPATH", "ALLPATH", "ALWAYS", "UNTIL", "EVENTUALLY", "AND", "OR", "NOT" };
                        Boolean notReserved = true;
                        for (String r : reserved) {
                            if (figureName.equals(r)) {
                                notReserved = false;
                                break;
                            }
                        }

                        //String reserved = "^(IF|RECORD|END|SORT|FUNCTION|SAFE|DO|ARRAY|STRONG|WEAK|FAIR|ENUMERATE|CONSTANT|BOOLEAN|OF|BEGIN|WHILE|IF|THEN|ELSE|SWITCH|CASE|NEXTSTEP|REPEAT|FOR|TO|ALL|EXIT|EXISTS|RETURN|TRUE|FALSE|MOD|VAR|GUARD|STATE|PATH|GENERATOR|ANALYSE|PLACE|TRANSITION|MARKING|CONSUME|PRODUCE|FORMULA|EXPATH|ALLPATH|ALWAYS|UNTIL|EVENTUALLY|AND|OR|NOT)$";
                        if (figureName.matches(validName) && notReserved) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("[Lola Export] Name of figure seems to be okay: "
                                             + figureName);
                            }
                            if (usedFigureNames.containsValue(figureName)) { //name already used by other figure
                                figureName = figureName + id;
                                if (usedFigureNames.containsKey(figureName)) {
                                    logger.error("Exception: Name "
                                                 + figureName
                                                 + " already exists");
                                }
                            }
                            // remember figure and given name
                            retString.append(figureName);
                            usedFigureNames.put(figure, figureName);
                            named = true;
                        } else {
                            logger.error("[Lola Export] Name " + figureName
                                         + " is not valid syntactically.");
                        }
                    }
                }
            }

            // no user defined name at figure, invent one 
            if (named == false) {
                // "p" if place, "t" if transition 
                retString.append(figure instanceof PlaceFigure ? "p" : "t");
                retString.append(id);
                // if *accidentally* the invented name is already in use ... 
                if (usedFigureNames.containsValue("p" + id)
                            || usedFigureNames.containsValue("t" + id)) {
                    retString.append("Exception42");
                }
            }
        }

        // TODO restrict method to only Transition- and PlaceFigures, so far other FiguresWithID return ""
        return retString.toString();
    }

    /**
     * Constructs a state predicate to check whether the given
     * TransitionFigure is live, ie. whether the marking which
     * marks all of its preconditions is coverable.
     *
     * @param fig
     * @param drawing
     * @return
     */
    public StringBuffer constructStatePredicate(TransitionFigure fig,
                                                Drawing drawing) {
        StringBuffer result = new StringBuffer();


        //HashMap<PlaceFigure,Integer> pre = preAll.get(fig);
        HashMap<String, Integer> pre = preset(drawing, fig);
        Iterator<String> places = pre.keySet().iterator();

        int size = pre.size();
        int bracket = size - 2;
        while (places.hasNext()) {
            String place = places.next();
            size--;
            result.append(place);
            result.append(" >= ");
            result.append(pre.get(place));
            if (size >= 1) {
                result.append(" AND ");
            }
            if (size >= 2) {
                result.append("(");
            }
        }

        // add closing brackets
        for (int i = 0; i < bracket; i++) {
            result.append(")");
        }
        return result;
    }

    /**
     * Uses the private extractMarking method to determine a HashMap of the
     * places (key) and initial marking (value) for the drawing. This is more
     * useful then the Stringbuffer returned by extractMarking
     *
     * @param drawing
     *            the drawing
     * @return a HashMap of places/marking
     */
    public HashMap<String, String> getInitialMarking(CPNDrawing drawing) {
        StringBuffer buffer = extractMarking(drawing);
        HashMap<String, String> result = new HashMap<String, String>();

        String substring;
        String place;
        String tokens;
        String marking = buffer.substring(8).trim();
        marking = marking.substring(0, marking.length() - 1);
        while (!(marking.equals(""))) {
            substring = marking.substring(marking.lastIndexOf(",") + 1,
                                          marking.length()).trim();

            place = substring.substring(0, substring.lastIndexOf(":"));
            tokens = substring.substring(substring.lastIndexOf(":") + 2);
            result.put(place, tokens);

            if (marking.lastIndexOf(",") >= 0) {
                marking = marking.substring(0, marking.lastIndexOf(","));
            } else {
                marking = "";
            }
        }

        return result;

    }

    /**
     * Gets all tasks out of a drawing and returns them as vector
     *
     * @param drawing
     * @return a vector of LolaTask
     */
    public Vector<LolaTask> parseTasks(CPNDrawing drawing) {
        // TODO Auto-generated method stub
        Vector<LolaTask> result = new Vector<LolaTask>();
        FigureEnumeration figs = drawing.figures();
        while (figs.hasMoreElements()) {
            Figure fig = figs.nextElement();
            if (fig instanceof TextFigure) {
                LolaTask tmpTask = new LolaTask((TextFigure) fig, drawing);
                if (tmpTask.isValid()) {
                    result.add(tmpTask);
                }
            }
        }
        return result;
    }
}