package de.renew.fa.service;

import CH.ifa.draw.figures.TextFigure;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import de.renew.fa.FADrawing;
import de.renew.fa.figures.EndDecoration;
import de.renew.fa.figures.FAArcConnection;
import de.renew.fa.figures.FAStateFigure;
import de.renew.fa.figures.StartDecoration;
import de.renew.fa.figures.StartEndDecoration;

import java.io.OutputStream;
import java.io.PrintWriter;


public class JflapFileCreator {

    /**
     * Writes a given drawing to an output stream.
     *
     * @param stream - The output stream.
     * @param drawing A given drawing that shall be stored, if it is a <code>FADrawing</code>.
     */
    public static void writeToJflap(OutputStream stream, Drawing drawing) {
        if (drawing instanceof FADrawing) {
            FADrawing faDrawing = (FADrawing) drawing;
            export(stream, faDrawing);
        }
    }

    public static void export(OutputStream strem, Drawing drawing) {
        if (drawing instanceof FADrawing) {
            FADrawing fad = (FADrawing) drawing;
            PrintWriter pw;
            pw = new PrintWriter(strem);
            pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!--Created with JFLAP 6.0.--><structure>\n");
            pw.write("  <type>fa</type>\n");
            pw.write("  <automaton>\n");
            pw.write("      <!--The list of states.-->\n");
            FigureEnumeration figures = fad.figures();
            while (figures.hasMoreElements()) {
                Figure figure = figures.nextElement();
                if (figure instanceof FAStateFigure) {
                    FAStateFigure state = (FAStateFigure) figure;
                    pw.write("      <state id=\"" + state.getID()
                             + "\" name=\""
                             + (state.children().hasMoreElements()
                                ? ((TextFigure) state.children().nextElement())
                                  .getText() : "") + "\">\n");
                    pw.write("          <x>" + state.displayBox().x
                             + ".0</x>\n");
                    pw.write("          <y>" + state.displayBox().y
                             + ".0</y>\n");
                    if (state.getDecoration() instanceof StartDecoration) {
                        pw.write("          <initial/>\n");
                    }
                    if (state.getDecoration() instanceof EndDecoration) {
                        pw.write("          <final/>\n");
                    }
                    if (state.getDecoration() instanceof StartEndDecoration) {
                        pw.write("          <initial/>\n");
                        pw.write("          <final/>\n");
                    }
                    pw.write("      </state>\n");
                }
            }

            pw.write("      <!--The list of transitions.-->\n");

            figures = fad.figures();
            while (figures.hasMoreElements()) {
                Figure figure = figures.nextElement();
                if (figure instanceof FAArcConnection) {
                    FAArcConnection arc = (FAArcConnection) figure;
                    String s = (arc.children().hasMoreElements()
                                ? ((TextFigure) arc.children().nextElement())
                                   .getText() : "");
                    String[] strings = s.split(",");
                    for (String string : strings) {
                        pw.write("      <transition>\n");
                        pw.write("          <from>"
                                 + ((FAStateFigure) arc.startFigure()).getID()
                                 + "</from>\n");
                        pw.write("          <to>"
                                 + ((FAStateFigure) arc.endFigure()).getID()
                                 + "</to>\n");
                        if (string.equals("ε")) {
                            pw.write("          <read/>\n");
                        } else {
                            pw.write("          <read>" + string + "</read>\n");
                        } // endif
                        pw.write("      </transition>\n");
                    }
                }
            }
            pw.write("  </automaton>\n");
            pw.write("</structure>\n");
            pw.close();
        }
    }

    protected String _fileName;

    //   public static org.apache.log4j.Logger logger =
    // org.apache.log4j.Logger.getLogger(FAFileParser.class);
    public JflapFileCreator() {
    }

    /*not used
    /**
     * Displays a message box if the gui is present.
     * If no gui (parent) is present the message is send to <code>System.out</code>.
     *
     * @param title - The title bar inscription of the message box.
     * @param message - The message to be displayed in the message box.
     */


    /*
    private void simpleMessage(String title, String message) {
     GuiPlugin gui = GuiPlugin.getCurrent();
     if (gui == null) {
         System.out.println(this.getClass().getName() + ": " + title + ": "
                            + message);
         return;
     }
     JOptionPane.showMessageDialog(gui.getGuiFrame(), message, title,
                                   JOptionPane.WARNING_MESSAGE,
                                   new ImageIcon(gui.getClass()
                                                    .getResource(CPNApplication.CPNIMAGES
                                                                 + "RENEW.gif")));
    }*/
}