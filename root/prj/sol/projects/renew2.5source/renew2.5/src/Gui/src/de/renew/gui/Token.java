/**
 * Token.java
 *
 * Created: Wed Dec 20 12:54:59 2000
 */
package de.renew.gui;



/**
 * A class that may occur as a token in a Petri net may
 * implement the interface <code>Token</code>.
 * The class needs to implement a single method that returns
 * an object that describes the graphical appearence of the token.
 */
public interface Token {

    /**
     * Convert the object into one of the forms supported by the
     * simulation GUI.
     *
     * @param expanded Whether the 'show expanded marking' flag
     *                 is set for the current token bag.
     *
     * @return the displayed value, which is typically one of the following:
     * <ul>
     * <li> a <code>java.lang.String</code> object, or</li>
     * <li> a <code>java.awt.Image</code> object, or</li>
     * <li> a <code>java.awt.image.ImageProducer</code> object, or</li>
     * <li> a <code>java.net.URL</code> object, or</li>
     * <li> a <code>CH.ifa.draw.framework.Figure</code> object.</li>
     * </ul>
     *
     * @see java.lang.String
     * @see java.awt.Image
     * @see java.awt.image.ImageProducer
     * @see java.net.URL
     * @see CH.ifa.draw.framework.Figure
     */
    public Object getTokenRepresentation(boolean expanded);
}