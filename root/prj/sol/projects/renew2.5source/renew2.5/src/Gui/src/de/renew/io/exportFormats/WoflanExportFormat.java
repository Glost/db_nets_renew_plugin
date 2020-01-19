package de.renew.io.exportFormats;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.framework.Figure;
import CH.ifa.draw.framework.FigureEnumeration;

import CH.ifa.draw.io.exportFormats.ExportFormatAbstract;

import de.renew.gui.ArcConnection;
import de.renew.gui.CPNDrawing;
import de.renew.gui.ModeReplacement;
import de.renew.gui.PlaceFigure;
import de.renew.gui.ShadowHolder;
import de.renew.gui.TransitionFigure;

import de.renew.io.WoflanFileFilter;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;
import de.renew.shadow.ShadowTransition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.util.Iterator;
import java.util.Vector;


public class WoflanExportFormat extends ExportFormatAbstract {
    // Attributes
    // Construktor
    public WoflanExportFormat() {
        super("Woflan", new WoflanFileFilter());
    }

    // Methods


    /**
     * @see de.renew.io.ExportFormat#canExportNto1()
     */
    public boolean canExportNto1() {
        return false;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing, java.net.URI)
     */
    public File export(Drawing drawing, File path) throws Exception {
        File result = null;
        if (drawing != null && path != null) {
            result = path;
            ModeReplacement.getInstance().getSimulation().buildAllShadows();
            PrintWriter p = new PrintWriter(new FileOutputStream(result));
            int cnt = 0;
            FigureEnumeration placeenumeration = drawing.figures();

            while (placeenumeration.hasMoreElements()) {
                Figure figure = placeenumeration.nextFigure();

                if (figure instanceof PlaceFigure) {
                    ShadowPlace place = (ShadowPlace) ((ShadowHolder) figure)
                                            .getShadow();
                    String name = place.getName();

                    if (name == null) {
                        name = "P" + (++cnt);
                        place.setName(name);
                    }
                    p.println("place " + name + ";");
                }
            }
            cnt = 0;
            FigureEnumeration transenumeration = drawing.figures();

            while (transenumeration.hasMoreElements()) {
                Figure figure = transenumeration.nextFigure();

                if (figure instanceof TransitionFigure) {
                    ShadowTransition trans = (ShadowTransition) ((ShadowHolder) figure)
                                             .getShadow();
                    String name = trans.getName();

                    if (name == null) {
                        name = "T" + (++cnt);
                        trans.setName(name);
                    }
                    Vector<String> inarcs = new Vector<String>(); // ...of Placenames
                    Vector<String> outarcs = new Vector<String>(); // dito


                    // test if transition is :init(...)-transition:
                    Iterator<ShadowNetElement> inscrenumeration = trans.elements()
                                                                       .iterator();

                    while (inscrenumeration.hasNext()) {
                        Object elem = inscrenumeration.next();

                        if (elem instanceof ShadowInscription) {
                            if (((ShadowInscription) elem).inscr.startsWith(":new(")) {
                                inarcs.addElement("pinit");
                            }
                        }
                    }
                    FigureEnumeration arcenumeration = drawing.figures();

                    while (arcenumeration.hasMoreElements()) {
                        figure = arcenumeration.nextFigure();
                        if (figure instanceof ArcConnection) {
                            ArcConnection ac = (ArcConnection) figure;
                            ShadowArc arc = (ShadowArc) ac.getShadow();

                            if (arc.transition == trans) {
                                String placename = arc.place.getName();

                                if (arc.shadowArcType == ShadowArc.both
                                            || arc.shadowArcType == ShadowArc.test) {
                                    inarcs.addElement(placename);
                                    outarcs.addElement(placename);
                                } else if (arc.shadowArcType == ShadowArc.ordinary) {
                                    if (arc.placeToTransition) {
                                        inarcs.addElement(placename);
                                    } else {
                                        outarcs.addElement(placename);
                                    }
                                }
                            }
                        }
                    }
                    p.println("trans " + name + placeVector(inarcs, " in")
                              + placeVector(outarcs, " out") + ";");
                }
            }
            p.close();
        }
        assert (result != null) : "Failure in WoflanExportFormat: result == null";
        return result;
    }

    /**
     * @see de.renew.io.ExportFormat#export(CH.ifa.draw.framework.Drawing[], java.net.URI)
     */
    public File export(Drawing[] drawings, File path) throws Exception {
        File result = null;
        assert (result != null) : "Failure in WoflanExportFormat: result == null";
        return result;
    }

    private String placeVector(Vector<String> places, String title) {
        StringBuffer output = new StringBuffer("");

        if (places.size() > 0) {
            output.append(title);
            for (int i = places.size() - 1; i >= 0; --i) {
                output.append(" ").append(places.elementAt(i));
            }
        }
        return output.toString();
    }

    public boolean canExportDrawing(Drawing drawing) {
        boolean result = false;
        if (drawing instanceof CPNDrawing) {
            result = true;
        }
        return result;
    }
}