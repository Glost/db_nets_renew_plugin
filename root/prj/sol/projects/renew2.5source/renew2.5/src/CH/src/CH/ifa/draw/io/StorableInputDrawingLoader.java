package CH.ifa.draw.io;

import CH.ifa.draw.framework.Drawing;

import CH.ifa.draw.util.KnownPlugins;
import CH.ifa.draw.util.StorableInput;
import CH.ifa.draw.util.UnknownTypeException;

import java.awt.Dimension;
import java.awt.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JOptionPane;


public class StorableInputDrawingLoader {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                        .getLogger(StorableInputDrawingLoader.class);

    public StorableInputDrawingLoader() {
    }

    /**
     * Override to establish a new policy to create
     * input handlers.
     */
    protected StorableInput makeStorableInput(URL location, boolean useUFT)
            throws IOException {
        return new StorableInput(location, useUFT);
    }

    protected StorableInput makeStorableInput(InputStream stream, boolean useUFT) {
        return new StorableInput(stream, useUFT);
    }

    public PositionedDrawing readFromStorableInput(File file, StatusDisplayer sd)
            throws IOException {
        try {
            return readFromStorableInput(file.toURI().toURL(), sd);
        } catch (MalformedURLException e) {
            sd.showStatus("Error " + e);
            return null;
        }
    }

    public PositionedDrawing readFromStorableInput(InputStream stream)
            throws FileNotFoundException, IOException {
        StorableInput input = null;
        input = makeStorableInput(stream, true);
        return readFromStorableInput(input);
    }

    public PositionedDrawing readFromStorableInput(URL location,
                                                   StatusDisplayer sd)
            throws IOException {
        StorableInput input = null;
        try {
            input = makeStorableInput(location, true);
            return readFromStorableInput(input, sd);
        } catch (IOException e) {
            input = makeStorableInput(location, false);
            return readFromStorableInput(input, sd);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    private PositionedDrawing readFromStorableInput(StorableInput input,
                                                    StatusDisplayer sd) {
        Drawing drawing = null;
        Point newWindowLoc = null;
        Dimension newWindowDim = null;
        try {
            drawing = readStorableDrawing(input);
            // is there a window position to restore?
            if (input.canReadInt()) {
                newWindowLoc = new Point(input.readInt(), input.readInt());
                newWindowDim = new Dimension(input.readInt(), input.readInt());
            }
        } catch (FileNotFoundException fnfe) {
            sd.showStatus("Error: File not found. " + fnfe);
        } catch (IOException ioe) {
            logger.error("Could not open Drawing: " + input.getURI().toString());
            if (logger.isDebugEnabled()) {
                logger.debug(StorableInputDrawingLoader.class.getSimpleName()
                             + ": " + ioe);
            }
            if (ioe instanceof UnknownTypeException) {
                UnknownTypeException ute = (UnknownTypeException) ioe;
                String missingPlugin = KnownPlugins.guessPluginByClass(ute
                                           .getType());

                StringBuilder sb = new StringBuilder();
                sb.append("Error:\n");
                sb.append("Could not open Drawing:\n");
                try {
                    sb.append(URLDecoder.decode(input.getURI().toString(),
                                                "UTF-8"));
                } catch (UnsupportedEncodingException uee) {
                    sb.append(input.getURI().toString());
                }
                sb.append("\n\n");
                sb.append("There might be a plugin missing.");
                if (missingPlugin != null) {
                    sb.append("\n");
                    sb.append("Try to install the plugin: ");
                    sb.append(missingPlugin);
                }
                String message = sb.toString();
                JOptionPane.showMessageDialog(null, message,
                                              "Error: Could not open Drawing",
                                              JOptionPane.PLAIN_MESSAGE);
            }
            sd.showStatus("Error: Could not open Drawing. " + ioe);
        }
        if (drawing == null) {
            return null;
        }
        return new PositionedDrawing(newWindowLoc, newWindowDim, drawing);
    }

    private PositionedDrawing readFromStorableInput(StorableInput input)
            throws FileNotFoundException, IOException {
        Drawing drawing = null;
        Point newWindowLoc = null;
        Dimension newWindowDim = null;
        try {
            drawing = readStorableDrawing(input);
            // is there a window position to restore?
            if (input.canReadInt()) {
                newWindowLoc = new Point(input.readInt(), input.readInt());
                newWindowDim = new Dimension(input.readInt(), input.readInt());
            }
        } catch (FileNotFoundException fnfe) {
            String msg = "StorableInputDrawingLoader.readFromStorableInput : Error: File not found. "
                         + fnfe;
            throw new FileNotFoundException(msg);
        } catch (IOException ioe) {
            String msg = "StorableInputDrawingLoader.readFromStorableInput : Error "
                         + ioe;
            throw new IOException(msg);
        }
        if (drawing == null) {
            return null;
        }
        return new PositionedDrawing(newWindowLoc, newWindowDim, drawing);
    }

    public static Drawing readStorableDrawing(StorableInput input)
            throws IOException {
        if (input.canReadInt()) {
            input.setVersion(input.readInt());
        } else {
            input.setVersion(0);
        }
        return (Drawing) input.readStorable();
    }
}