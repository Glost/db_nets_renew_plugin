package de.renew.gui;

import CH.ifa.draw.DrawPlugin;

import CH.ifa.draw.util.Command;

import de.renew.shadow.ShadowInscription;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.util.Iterator;


public class TypeCheckCommand extends Command {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(TypeCheckCommand.class);

    // private CPNApplication application;
    public TypeCheckCommand(String name) {
        super(name);
        // this.application = application;
    }

    public boolean isExecutable() {
        if (!super.isExecutable()) {
            return false;
        }
        return true;
    }

    public synchronized void execute() {
        CPNDrawing currentDrawing = null;
        ShadowNet sn = null;
        ShadowInscription sii = null;
        Object o = null;
        boolean pn = true; // if pn = true, the net is a P/T Net 
        boolean rn = false; // if rn = true, the net is a reference net, or a net with channels

        CPNApplication application = null;
        try {
            application = (CPNApplication) DrawPlugin.getGui();
        } catch (ClassCastException e) {
            logger.error(e.getMessage(), e);
            return;
        }

        application.syntaxCheck();
        currentDrawing = (CPNDrawing) application.drawing();
        sn = currentDrawing.getShadow();
        Iterator<ShadowNetElement> snIterator = sn.elements().iterator();
        while (snIterator.hasNext()) {
            try {
                o = snIterator.next();
            } catch (java.lang.ClassCastException e1) {
                try {
                    sii = (ShadowInscription) o;
                    if (!sii.inscr.equals("[]")) {
                        pn = false;
                        if (sii.inscr.startsWith(":")
                                    || (sii.inscr.indexOf(":new ") != -1)
                                    || (sii.inscr.indexOf("this:") != -1)) {
                            rn = true;
                        }
                    }
                } catch (java.lang.ClassCastException e2) {
                    logger.error(e2.getMessage(), e2);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (pn) {
            application.showStatus("P/T Net");
        } else if (rn) {
            application.showStatus("Reference Net/Net with Channels");
        } else {
            application.showStatus("Coloured Net");
        }
    }
}