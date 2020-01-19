/**
 *
 */
package CH.ifa.draw.util;

import CH.ifa.draw.framework.Drawing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This class contains utility methods for convenience handling of <code>Drawing</code>.
 *
 * @author cabac
 *
 */
public class DrawingHelper {

    /** Clones the drawing.
     *
     * @param originalDrawing
     * @return a deep clone of the original drawing
     * @throws Exception
     */
    public static Drawing cloneDrawing(Drawing originalDrawing)
            throws Exception {
        Drawing drawing;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(originalDrawing);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos
                                            .toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            drawing = (Drawing) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Could not clone drawing: "
                                + originalDrawing.getName(), e);
        }
        return drawing;
    }
}