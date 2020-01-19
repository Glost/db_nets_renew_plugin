package de.renew.gui;

import CH.ifa.draw.figures.AttributeFigure;

import CH.ifa.draw.framework.Connector;
import CH.ifa.draw.framework.FigureChangeListener;
import CH.ifa.draw.framework.ParentFigure;

import CH.ifa.draw.io.DrawingFileHelper;
import CH.ifa.draw.io.StorableInputDrawingLoader;

import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.StorableOutput;

import de.renew.shadow.ShadowArc;

import java.awt.AWTEventMulticaster;
import java.awt.Dimension;
import java.awt.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Vector;


public class CPNDrawingHelper {
    public PlaceFigure createPlace() {
        PlaceFigure result = new PlaceFigure();
        Dimension defaultDimension = PlaceFigure.defaultDimension();
        result.displayBox(new Point(0, 0),
                          new Point((int) defaultDimension.getWidth(),
                                    (int) defaultDimension.getHeight()));
        return result;
    }

    public VirtualPlaceFigure createVirtualPlace(PlaceFigure place) {
        VirtualPlaceFigure result = new VirtualPlaceFigure(place);
        Dimension defaultDimension = VirtualPlaceFigure.defaultDimension();
        result.displayBox(new Point(0, 0),
                          new Point((int) defaultDimension.getWidth(),
                                    (int) defaultDimension.getHeight()));
        return result;
    }

    public TransitionFigure createTransition() {
        TransitionFigure result = new TransitionFigure();
        Dimension defaultDimension = TransitionFigure.defaultDimension();
        result.displayBox(new Point(0, 0),
                          new Point((int) defaultDimension.getWidth(),
                                    (int) defaultDimension.getHeight()));
        return result;
    }

    /**
     * Helper method to create {@link ArcConnection}s between two
     * {@link Connector}s and prevent duplicate code.
     *
     * @param start
     *            The Connector the starting point of the Arc shall connect
     *            with. (Which is only important for ordinary arcs)
     * @param end
     *            The Connector the end point of the Arc shall connect with.
     *            (Which is only important for ordinary arcs)
     * @param type
     *            The type of the Arc. Static constants to be found in
     *            {@link ShadowArc}.
     * @return {@link ArcConnection} A reference to the created
     *         {@link ArcConnection}.
     */
    public ArcConnection createArcConnection(AttributeFigure start,
                                             AttributeFigure end, int type) {
        ArcConnection result = new ArcConnection(type);
        result.startPoint(0, 0);
        result.endPoint(0, 0);
        result.connectStart(start.connectorAt(start.center()));
        result.connectEnd(end.connectorAt(end.center()));
        result.updateConnection();
        return result;
    }

    public ArcConnection createArcConnection(AttributeFigure start,
                                             AttributeFigure end) {
        return createArcConnection(start, end, ShadowArc.ordinary);
    }

    public CPNTextFigure createNameTextFigure(String name, ParentFigure parent) {
        CPNTextFigure result = new CPNTextFigure(CPNTextFigure.NAME);
        result.setText(name);
        result.setParent(parent);
        result.moveBy(0, -20);
        return result;
    }

    public CPNTextFigure createInscription(String inscription,
                                           ParentFigure parent) {
        CPNTextFigure result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        result.setText(inscription);
        result.setParent(parent);
        return result;
    }

    /**
     * Helper method which creates a {@link CPNTextFigure} with the given
     * weight(number) as its textual content and adds it to the given
     * {@link ArcConnection}.
     *
     * @param arcConnection
     *            The given ArcConnection
     * @param weight
     *            The given number to create the weight with
     * @return result A reference to the created text figure.
     */
    public CPNTextFigure createWeightTextFigure(ArcConnection arcConnection,
                                                int weight) {
        CPNTextFigure result = new CPNTextFigure(CPNTextFigure.INSCRIPTION);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < weight; i++) {
            stringBuilder.append("[];");
        }
        result.setText(stringBuilder.toString());
        result.setParent(arcConnection);
        return result;
    }

    public Vector<ArcConnection> getConnectedArcs(AttributeFigure fig) {
        Vector<ArcConnection> arcs = new Vector<ArcConnection>();

        FigureChangeListener[] listeners = AWTEventMulticaster.getListeners(fig
                                                                            .listener(),
                                                                            FigureChangeListener.class);

        for (FigureChangeListener listener : listeners) {
            if (listener instanceof ArcConnection) {
                ArcConnection arc = (ArcConnection) listener;

                if (arc.startFigure().equals(fig)
                            || arc.endFigure().equals(fig)) {
                    arcs.add(arc);
                }
            }
        }

        return arcs;
    }

    /** Convert a drawing to String representation (rnw).
     *
     * @param d a Drawing
     * @return String representing the Drawing
     * @throws IOException if the cloning was not possible
     */
    public static String netAsString(CPNDrawing d) throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        d.lock();
        try {
            StorableOutput output = new StorableOutput(out);
            output.writeInt(DrawingFileHelper.FILEVERSION);
            output.writeStorable(d);
            output.close();
            d.clearModified();
        } finally {
            d.unlock();
        }
        return out.toString();
    }

    /** Converts a rnw String representation into a <code>CPNDrawing</code>
     *
     * @param net A CPNDrawing in String representation (rnw).
     * @param registerForSimulation
     * @return The CPNDrawing constructed from the String
     * @throws IOException if the conversion was not possible.
     */
    public static CPNDrawing netFromString(String net,
                                           boolean registerForSimulation)
            throws IOException {
        if (net == null || net.trim().length() < 10) {
            return null;
        }
        CPNDrawing d;
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(net.getBytes());
            StorableInput si = new PatchingStorableInput(in, true);
            d = (CPNDrawing) StorableInputDrawingLoader.readStorableDrawing(si);
            d.invalidate();
            if (registerForSimulation) {
                ModeReplacement.getInstance().getDrawingLoader().addDrawing(d);
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {
            }
        }
        return d;
    }
}