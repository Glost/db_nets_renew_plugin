/*
 * @(#)FigureSelection.java 5.1
 *
 */
package CH.ifa.draw.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Enumeration;
import java.util.Vector;


/**
 * FigureSelection enables to transfer the selected figures
 * to a clipboard.<p>
 * Will soon be converted to the JDK 1.1 Transferable interface.
 *
 * @see CH.ifa.draw.util.Clipboard
 */
public class FigureSelection extends Object {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(FigureSelection.class);

    /**
     * The type identifier of the selection.
     */
    public final static String TYPE = "CH.ifa.draw.Figures";
    private byte[] fData; // flattened figures, ready to be resurrected

    /**
     * Constructes the Figure selection for the vector of figures.
     */
    public FigureSelection(Vector<Figure> figures) {
        fData = writeData(figures);
    }

    public FigureSelection(Enumeration<Figure> figures) {
        Vector<Figure> vector = new Vector<Figure>();
        while (figures.hasMoreElements()) {
            vector.addElement(figures.nextElement());
        }
        fData = writeData(vector);
    }

    private byte[] writeData(Vector<Figure> vector) {
        // a FigureSelection is represented as a flattened ByteStream
        // of figures.
        ByteArrayOutputStream output = new ByteArrayOutputStream(200);
        try {
            ObjectOutputStream writer = new ObjectOutputStream(output);
            writer.writeInt(vector.size());
            Enumeration<Figure> figures = vector.elements();
            while (figures.hasMoreElements()) {
                Figure figure = figures.nextElement();
                writer.writeObject(figure);
            }
            writer.close();
        } catch (IOException e) {
            logger.error("Figure serialization failed: " + e + "\n");
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    /**
     * Gets the type of the selection.
     */
    public String getType() {
        return TYPE;
    }

    /**
     * Gets the data of the selection. The result is returned
     * as a Vector of Figures.
     *
     * @return a copy of the figure selection.
     */
    public Vector<Figure> getData(String type) {
        if (type.equals(TYPE)) {
            InputStream input = new ByteArrayInputStream(fData);
            Vector<Figure> result = new Vector<Figure>(10);
            try {
                ObjectInputStream reader = new ObjectInputStream(input);

                int numRead = 0;
                int count = reader.readInt();
                while (numRead < count) {
                    Figure newFigure = (Figure) reader.readObject();
                    result.addElement(newFigure);
                    numRead++;
                }
                reader.close();
            } catch (IOException e) {
                logger.error("Figure deserialization failed: " + e);
            } catch (ClassNotFoundException e) {
                logger.error("Figure deserialization failed: " + e);
            }
            return result;
        }
        return null;
    }
}