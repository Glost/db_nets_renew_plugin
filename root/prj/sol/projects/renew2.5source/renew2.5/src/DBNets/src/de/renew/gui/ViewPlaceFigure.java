package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowViewPlace;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;

import java.util.Objects;

/**
 * The db-net view place UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
}
