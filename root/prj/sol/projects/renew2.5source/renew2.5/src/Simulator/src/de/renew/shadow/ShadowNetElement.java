package de.renew.shadow;

public abstract class ShadowNetElement implements java.io.Serializable {
    static final long serialVersionUID = -5409843866403864922L;

    // ---- ID-Handling ------------------------------------------------


    /**
     * Special value for the ID indicating that no ID
     * is assigned. Currently represented as 0.
     * @see #setID
     **/
    public static final int NOID = 0;

    /**
     * Not used by the shadow classes. May be used to
     * denote the origin of this shadow in an application
     * specific way. Will not be serialized, but that should
     * not be a problem in the usual application domains.
     **/
    public transient Object context = null;

    /**
     * Every shadow net element is permanently associated to a shadow net.
     **/
    protected final ShadowNet shadowNet;

    /**
     * Holds the ID assigned to this shadow net element,
     * defaults to NOID.
     * <p>
     * This field is serializable. Hopefully, the default
     * value created on deserialization of earlier versions
     * of shadow nets not containing the field will be 0
     * (equal to NOID), too.
     **/
    private int _id = NOID;

    protected ShadowNetElement(ShadowNet shadowNet) {
        this.shadowNet = shadowNet;
        shadowNet.add(this);
    }

    public ShadowNet getNet() {
        return shadowNet;
    }

    public void discard() {
        shadowNet.remove(this);
    }

    /**
     * Assigns the given ID to this shadow net element.
     * <p>
     * The ID will be persistent until changed explicitly
     * by another call to <code>setID()</code>. Instances
     * default to <code>NOID</code> until setID() is called
     * the first time.
     * Be careful not to use the NOID value as an ID to
     * avoid confusion. Uniqueness of IDs is not guaranteed,
     * it lies in the responsibility of the assigning instance.
     * </p><p>
     * The usual way IDs are assigned is by the method
     * <code>buildShadow()</code> of <code>ShadowHolder</code>
     * instances, immediately after the shadow net element was
     * created.
     * </p><p>
     * An analogous concept is used to identify graphical net
     * elements (packages <code>de.renew.gui</code> and
     * <code>CH.ifa.draw.*</code>). If the
     * internal representation of NOID is the same in both
     * identification schemes, the mapping will be easier.
     * </p>
     * @see #NOID
     * @see CH.ifa.draw.framework.FigureWithID
     * @see de.renew.gui.ShadowHolder
     * @see de.renew.gui.CPNDrawing
     **/
    public void setID(int newID) {
        _id = newID;
    }

    /**
     * Returns the ID assigned to this shadow net element
     * or NOID, if no ID was assigned.
     * @see #setID
     **/
    public int getID() {
        return _id;
    }
}