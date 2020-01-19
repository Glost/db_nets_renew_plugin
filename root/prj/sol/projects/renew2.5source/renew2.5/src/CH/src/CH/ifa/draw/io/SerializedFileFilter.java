/*
 * Created on Apr 13, 2003
 *
 * FileFilter for the serialized form of all possible Drawings.
 */
package CH.ifa.draw.io;



/**
 * @author Lawrence Cabac
 *
 * This FileFilter accepts serialized files. Extension "ser".
 */
public class SerializedFileFilter extends SimpleFileFilter {
    public SerializedFileFilter() {
        this.setExtension("ser");
        setDescription("Serialized (Renew1.0) Drawing");
    }
}