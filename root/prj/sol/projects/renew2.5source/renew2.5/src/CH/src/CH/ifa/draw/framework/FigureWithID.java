package CH.ifa.draw.framework;



/**
 * Figures providing identification by an ID.
 * <p>
 * The ID should be persistent until changed explicitly
 * by setID(). Instances should default to NOID until
 * setID() is called the first time, unless the Constructor
 * sets the ID explicitly.
 *
 * Be careful not to use the NOID value as an ID to
 * avoid confusion. Uniqueness of IDs is not guaranteed,
 * it lies in the responsibility of the assigning instance.
 * </p><p>
 * The only known class (at the time this comment was written)
 * which makes use of IDs is <code>de.renew.gui.CPNDrawing</code>.
 *
 * An analogous concept is used to identify shadow net
 * elements (package <code>de.renew.shadow</code). If the
 * internal representation of NOID is the same in both
 * identification schemes, the mapping will be easier.
 * </p>
 *
 * FigureWithID.java
 * Created: Tue Mar 14  2000
 *
 * @author Michael Duvigneau
 * @see de.renew.gui.CPNDrawing
 * @see de.renew.shadow.ShadowNetElement
 **/
public interface FigureWithID extends Figure {

    /**
     * See interface description for details.
     * Currently represented as 0.
     **/
    static int NOID = 0;

    void setID(int id);

    int getID();
}