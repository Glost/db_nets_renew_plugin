package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowViewPlace;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;

import java.awt.Color;
import java.util.Objects;

/**
 * The db-net view place UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ViewPlaceFigure extends PlaceFigure {

    /**
     * The shadow (non-compiled) level representation of the db-net view place
     * formed from the Renew UI db-net's drawing.
     */
    private transient ShadowPlace shadow;

    /**
     * Releases the figure's resources.
     */
    @Override
    public void release() {
        super.release();
        if (Objects.nonNull(shadow)) {
            shadow.discard();
        }
    }

    /**
     * Builds the shadow (non-compiled) level representation of the db-net view place.
     *
     * @param net The shadow (non-compiled) level representation of the db-net.
     * @return The shadow (non-compiled) level representation of the db-net view place.
     */
    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowViewPlace(net);
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        logger.debug("View place created");
        return shadow;
    }

    /**
     * Returns the previously built shadow (non-compiled) level representation of the db-net view place.
     *
     * @return The previously built shadow (non-compiled) level representation of the db-net view place.
     */
    @Override
    public ShadowPlace getShadow() {
        return shadow;
    }

    /**
     * Returns the brown (RGB: #BFBF00) view place's fill color.
     *
     * @return The brown (RGB: #BFBF00) view place's fill color.
     */
    @Override
    public Color getFillColor() {
        return new Color(0xBFBF00);
    }
}
