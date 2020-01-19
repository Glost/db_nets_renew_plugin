package de.renew.fa.service;

import CH.ifa.draw.framework.Drawing;

import de.renew.fa.FADrawing;
import de.renew.fa.model.Arc;
import de.renew.fa.model.FA;
import de.renew.fa.model.FAImpl;
import de.renew.fa.model.Letter;
import de.renew.fa.model.State;
import de.renew.fa.model.Word;
import de.renew.fa.util.FAHelper;

import de.renew.gui.CPNApplication;
import de.renew.gui.GuiPlugin;

import de.renew.util.StringUtil;

import java.awt.Desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class FAFileParser {

    /**
     * @author Cabac
     *
     */
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(FAFileParser.class);

//     public static void main(String[] args) {
//         FAFileParser fp = new FAFileParser();


//         //fp.parseTFA(TESTDIR + "test.tfa");
//         //fp.writeFA();
//         FA fa = fp.parseXFA(TestHelper.TESTDIR + "test.xfa");

//         // Test: write the output to a file.
//         String fileName = TestHelper.TESTDIR + "test.xfa";
//         try {
//             FileWriter fw = new FileWriter(new File(fileName));

//             fw.write(fa.toString());
//             fw.close();

//         } catch (Exception e) {
//             System.err.println("FileParser: Error opening " + fileName + ": "
//                                + e);
//         }

//         // Read from PropertiesFile test.
//         FA fa2 = fp.parseXFA(TestHelper.TESTDIR + "test.xfa");
//         System.out.println("FAFileParser: debug:  \n"
//                            + FAHelper.getIncidenceMatrix(fa2).toString());
//     }

    /**
     * Writes a given fa model to an output stream.
     *
     * @param stream - The output stream.
     * @param drawing A given fa model that shall be stored.
     */
    public static void writeToXFA(OutputStream stream, FA fa) {
        Properties properties = FAHelper.toProperties(fa);
        try {
            properties.store(stream, "FA Model for Drawing: " + fa.getName());
        } catch (IOException e) {
            logger.error(FAFileParser.class.getName() + ": "
                         + "Could not write fa " + fa.getName() + " to file.");
            if (logger.isDebugEnabled()) {
                logger.debug(FAFileParser.class.getName() + ": ", e);
            }
        }
    }

    /**
     * Writes a given drawing to an output stream.
     *
     * @param stream - The output stream.
     * @param drawing A given drawing that shall be stored, if it is a <code>FADrawing</code>.
     */
    public static void writeToXFA(OutputStream stream, Drawing drawing) {
        if (drawing instanceof FADrawing) {
            FADrawing faDrawing = (FADrawing) drawing;
            FA fa = FAHelper.getModel(faDrawing);
            writeToXFA(stream, fa);
            String latexCode = getLatexCode(fa);
            producepdf(latexCode, fa.getName());
        }
    }

    private static void producepdf(String latexCode, String name) {
        File file = null;
        try {
            file = File.createTempFile(name, ".tex");
            PrintWriter pw;
            pw = new PrintWriter(file);
            pw.write("\\documentclass[a4paper]{article}");
            pw.write("\\begin{document}");
            pw.write(latexCode);
            pw.write("\\end{document}");
            pw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (file != null && file.exists()) {
            String[] command = new String[] { "pdflatex", "-output-directory="
                               + file.getAbsoluteFile().getParent(), file
                                   .getAbsolutePath() };
            runCommand(command);
            String pdffile = StringUtil.extendFileNameBy(file.getAbsolutePath(),
                                                         "pdf");
            try {
                Desktop.getDesktop().browse(new URI("file://" + pdffile));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /** Runs the command.
    The output of the command is logged at the verbose level.
    @param command the command.
    @param wordy   whether the command tends to scroll on the
                   standard error channel.
    */
    static private final int runCommand(String[] command) {
        int exitCode = 0;
        StringBuffer com = new StringBuffer();
        for (int i = 0; i < command.length; i++) {
            if (i > 0) {
                com.append(' ');
            }
            com.append(command[i]);
        }
        System.out.println("running command: " + com.toString());
        Process exec = null;
        try {
            exec = Runtime.getRuntime().exec(command);


        } catch (IOException exp) {
        }
        if (exec != null) {
            try {
                exitCode = exec.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return exitCode;
    }

    private static String getLatexCode(FA fa) {
        StringBuffer result = new StringBuffer();
        if (logger.isInfoEnabled()) {
            logger.info(FAFileParser.class.getName() + ": ");
            result.append("\\newcommand{\\faname}{A}\n");
            result.append("\\begin{eqnarray*}\n");
            result.append("\\faname &:=& (Q,\\Sigma,\\delta,S_{0},F) \\\\\n");

            result.append("Q &:=& \\{"); //nnl
            Iterator<State> states = fa.getStates();
            if (states.hasNext()) {
                State state = states.next();
                result.append(texify(state.getName())); //nnl
            }
            while (states.hasNext()) {
                State state = states.next();
                result.append("," + texify(state.getName())); //nnl
            }
            result.append("\\} \\\\\n");

            result.append("\\Sigma&:=&\\{"); //nnl
            Iterator<Letter> alphabet = fa.getAlphabet();
            if (alphabet.hasNext()) {
                Letter letter = alphabet.next();
                result.append(letter.getName()); //nnl
            }
            while (alphabet.hasNext()) {
                Letter letter = alphabet.next();
                result.append("," + letter.getName()); //nnl
            }
            result.append("\\} \\\\\n");

            result.append("S_{0}&:=&\\{"); //nnl
            Iterator<State> iterator = fa.startStates().iterator();
            if (iterator.hasNext()) {
                State state = iterator.next();
                result.append(texify(state.getName())); //nnl
            }
            while (iterator.hasNext()) {
                State state = iterator.next();
                result.append("," + texify(state.getName())); //nnl
            }
            result.append("\\} \\\\\n");

            result.append("F&:=&\\{"); //nnl
            Iterator<State> endstates = fa.endStates().iterator();
            if (endstates.hasNext()) {
                State state = endstates.next();
                result.append(texify(state.getName()));
            }
            while (endstates.hasNext()) {
                State state = endstates.next();
                result.append("," + texify(state.getName()));
            }
            result.append("\\} \\\\\n");

            HashMap<State, HashMap<Letter, Vector<State>>> map = new HashMap<State, HashMap<Letter, Vector<State>>>();
            Iterator<State> states2 = fa.getStates();
            while (states2.hasNext()) {
                State state = states2.next();
                map.put(state, new HashMap<Letter, Vector<State>>());
                Iterator<Letter> alphabet2 = fa.getAlphabet();
                while (alphabet2.hasNext()) {
                    Letter letter = alphabet2.next();
                    Vector<State> images = new Vector<State>();
                    map.get(state).put(letter, images);
                    Iterator<Arc> arcs = fa.getArcs();
                    while (arcs.hasNext()) {
                        Arc arc = arcs.next();
                        if (state.equals(arc.getFrom())
                                    && arc.getInscription().toString()
                                                  .contains(letter.getName())) {
                            //System.out.println("++++++++++====> putting "+arc.toString());
                            images.add(arc.getTo());
                        }
                    }
                    if (!images.isEmpty()) {
                        result.append("\\delta(" + texify(state.getName())
                                      + "," + letter + ")&:=& \\{"); //nnl
                        boolean check = false;
                        for (State state2 : images) {
                            if (check) {
                                result.append("," + texify(state2.getName())); //nnl
                            } else {
                                result.append(texify(state2.getName())); //nnl
                                check = true;
                            }
                        }

                        result.append("\\} \\\\\n");
                    }
                }
            }


            //            while (arcs.hasNext()) {
            //                Arc arc = (Arc) arcs.next();
            //                result.append("\\delta("+arc.getFrom().getName()+","+arc.getInscription()+
            //                        ")&:=& \\{");//nnl
            //                result.append(arc.getTo().getName());//nnl
            //                result.append("\\} \\\\");
            //                
            //            }
            result.append("\\end{eqnarray*}\n");

            // print delta as matrix
            result.append("$$\\delta:=\\bordermatrix{\n");
            Iterator<Letter> alphabet3 = fa.getAlphabet();
            result.append("     "); //nnl
            while (alphabet3.hasNext()) {
                Letter letter = alphabet3.next();
                result.append(" & " + letter.getName()); //nnl
            }
            result.append(" \\cr\n");

            Iterator<State> states3 = fa.getStates();
            while (states3.hasNext()) {
                State state = states3.next();
                //                result.append("    p & \\{p,q\\} & \\{p\\} \\cr");//nnl
                result.append(texify(state.getName())); //nnl
                Iterator<Letter> alphabet2 = fa.getAlphabet();
                while (alphabet2.hasNext()) {
                    Letter letter2 = alphabet2.next();
                    Iterator<State> iterator2 = map.get(state).get(letter2)
                                                   .iterator();
                    result.append(" & \\{"); //nnl
                    while (iterator2.hasNext()) {
                        State state2 = iterator2.next();
                        result.append(" " + texify(state2.getName()) + " "); //nnl
                    }
                    result.append("\\} "); //nnl

                }
                result.append(" \\cr\n");
            }

            //            result.append("    q & \\{r\\} & \\{r\\} \\cr");
            //            result.append("    r & \\emptyset & \\emptyset \\cr");
            result.append("}.$$\n");

        }
        return result.toString();
    }

    private static String texify(String s) {
        return s.replaceAll("(\\w)([\\d]+)", "$1_{$2}");
    }

    protected String _fileName;

    //   public static org.apache.log4j.Logger logger =
    // org.apache.log4j.Logger.getLogger(FAFileParser.class);
    public FAFileParser() {
    }

    /*
     * Convenience method to add Arcs to the FA. Parses a token and takes care
     * of lambda. @param fa - the paresd finite automata. @param token - the
     * string to be parsed. @return <code> null </code> if token could not be
     * parsed to a valid Arc, the new arc else.
     */
    private Arc newArc(FA fa, String token) {
        StringTokenizer st = new StringTokenizer(token, "(:) ");
        String subtoken;
        State from = null;
        Word inscription = null;
        State to = null;
        if (st.hasMoreTokens()) {
            subtoken = st.nextToken();
            from = fa.getStateByName(subtoken);
            if (st.hasMoreTokens()) {
                subtoken = st.nextToken();
                if (st.hasMoreTokens()) {
                    //ignoring "lambda" in .xfa descriptions
                    if ("lambda".equals(subtoken)) {
                        subtoken = "";
                    }
                    inscription = fa.newWord(subtoken);
                    subtoken = st.nextToken();
                    to = fa.getStateByName(subtoken);
                } else {
                    inscription = fa.newWord("");
                    to = fa.getStateByName(subtoken);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }

        //System.out.println("Arc: " +" "+token +" "+ from +" "+ inscription +"
        // "+ to );
        if (logger.isDebugEnabled()) {
            logger.debug(FAFileParser.class.getName() + ": " + from
                         + inscription + to);
        }
        if (from == null || inscription == null || to == null) {
            return null;
        }
        Arc newArc = fa.newArc(from, inscription, to);


        //System.out.println("Arc: " +" "+token +" "+ from +" "+ inscription +"
        // "+ to +" "+newArc);
        return newArc;
    }

    /**
     * Parses an XFA file to a finite automata model.
     * @param stream - The  input stream.
     * @return The finite automata model
     */
    private FA parseXFA(InputStream stream) {
        Properties elements = new Properties();
        try {
            elements.load(stream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        FA fa = new FAImpl();

        //States:
        String states = (String) elements.get("Z");

        //System.out.println(this.getClass().getName() + ": st= " + states);
        StringTokenizer st = new StringTokenizer(states, "[,] ");
        while (st.hasMoreElements()) {
            String stateName = (String) st.nextElement();
            fa.newState(stateName);
        }

        //Sigma:
        String sigma = (String) elements.get("Sigma");
        st = new StringTokenizer(sigma, "[,] ");
        while (st.hasMoreElements()) {
            String letterName = (String) st.nextElement();
            if (letterName.length() == 1) {
                fa.newLetter(letterName);

            } else {
                String message = "Element \"" + letterName
                                 + "\" is not a single character. Ignoring element.";
                String title = "Ignoring element";
                simpleMessage(title, message);

            }
        }

        //Arcs:
        String arcs = (String) elements.get("K");
        st = new StringTokenizer(arcs, "[,] ");
        while (st.hasMoreElements()) {
            String arcName = (String) st.nextElement();

            //System.out.println(this.getClass().getName() + ": " + arcName);
            if (newArc(fa, arcName) == null) {
                simpleMessage("Ignoring Arc",
                              arcName + " is not a valid arc name.");
            }
        }

        //      Start states:
        String startStates = (String) elements.get("Z_Start");
        st = new StringTokenizer(startStates, "[,] ");
        while (st.hasMoreElements()) {
            String ssName = (String) st.nextElement();
            if (fa.getStateByName(ssName) != null) {
                fa.getStateByName(ssName).setStartState(true);
            } else {
                simpleMessage("Ignoring start state attribute",
                              ssName + " is not in the set of states.");
            }
        }

        //      End states:
        String endStates = (String) elements.get("Z_End");
        st = new StringTokenizer(endStates, "[,] ");
        while (st.hasMoreElements()) {
            String esName = (String) st.nextElement();
            if (fa.getStateByName(esName) != null) {
                fa.getStateByName(esName).setEndState(true);
            } else {
                simpleMessage("Ignoring end state attribute",
                              esName + " is not in the set of states.");
            }
        }


        return fa;
    }


    /**
     * Parses a fa represented as XFA.
     * @param stream - The input strem.
     * @param name - The name of the model.
     * @return The finite automata model.
     */
    public FA parseXFA(InputStream stream, String name) {
        FA fa = parseXFA(stream);
        fa.setName(StringUtil.stripFilenameExtension(name));
        if (logger.isInfoEnabled()) {
            logger.info(FAFileParser.class.getName()
                        + "------ FA Check Name1: " + fa.getName()
                        + " ----------------------------\n\n" + fa.toString());
        }
        return fa;
    }

    /**
     * Parses a fa represented as XFA.
     * @param fileName - The file name of the XFA-File.
     * @return The finite automata model.
     */
    public FA parseXFA(String fileName) {
        InputStream in;
        FA fa = new FAImpl();
        try {
            in = new FileInputStream(fileName);
            fa = parseXFA(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        fa.setName(StringUtil.stripFilenameExtension(fileName));
        if (logger.isInfoEnabled()) {
            logger.info(FAFileParser.class.getName()
                        + "------ FA Check Name2: " + fa.getName()
                        + " ----------------------------\n\n" + fa.toString());
        }
        return fa;
    }

    /**
     * Displays a message box if the gui is present.
     * If no gui (parent) is present the message is send to <code>System.out</code>.
     *
     * @param title - The title bar inscription of the message box.
     * @param message - The message to be displayed in the message box.
     */
    private void simpleMessage(String title, String message) {
        GuiPlugin gui = GuiPlugin.getCurrent();
        if (gui == null) {
            if (logger.isInfoEnabled()) {
                logger.info(FAFileParser.class.getName() + ": " + title + ": "
                            + message);
            }
            return;
        }
        JOptionPane.showMessageDialog(gui.getGuiFrame(), message, title,
                                      JOptionPane.WARNING_MESSAGE,
                                      new ImageIcon(gui.getClass()
                                                       .getResource(CPNApplication.CPNIMAGES
                                                                    + "RENEW.gif")));
    }

    public static void main(String[] args) {
        String s = "z001aaa44";
        System.out.println(s.replaceAll("(\\w)([\\d]+)", "$1_{$2}"));
    }
}