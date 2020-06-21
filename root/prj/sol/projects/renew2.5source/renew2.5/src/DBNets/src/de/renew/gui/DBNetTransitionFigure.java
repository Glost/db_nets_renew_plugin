package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.util.Objects;

/**
 * The db-net transition UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class DBNetTransitionFigure extends TransitionFigure {

    /**
     * The shadow (non-compiled) level representation of the db-net transition
     * formed from the Renew UI db-net's drawing.
     */
    private transient ShadowDBNetTransition shadow;

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
     * Builds the shadow (non-compiled) level representation of the db-net transition.
     *
     * @param net The shadow (non-compiled) level representation of the db-net.
     * @return The shadow (non-compiled) level representation of the db-net transition.
     */
    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowDBNetTransition(net);
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        logger.debug("DB-net transition shadow created");
        return shadow;
    }

    /**
     * Returns the previously built shadow (non-compiled) level representation of the db-net transition.
     *
     * @return The previously built shadow (non-compiled) level representation of the db-net transition.
     */
    @Override
    public ShadowDBNetTransition getShadow() {
        return shadow;
    }
}
