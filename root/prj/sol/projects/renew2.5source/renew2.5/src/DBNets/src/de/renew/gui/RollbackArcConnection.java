package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.dbnets.shadow.node.ShadowRollbackArc;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;
import de.renew.shadow.ShadowPlace;

import java.util.Objects;

/**
 * The db-net rollback arc connection UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class RollbackArcConnection extends ArcConnection {

    /**
     * The db-net rollback arc connection UI figure constant.
     */
    public static final RollbackArcConnection ROLLBACK_ARC_CONNECTION = new RollbackArcConnection();

    /**
     * The shadow (non-compiled) level representation of the db-net rollback arc
     * formed from the Renew UI db-net's drawing.
     */
    private transient ShadowRollbackArc shadow;

    /**
     * The db-net rollback arc connection UI figure's constructor.
     */
    public RollbackArcConnection() {
        super(ShadowRollbackArc.ROLLBACK_ARC);
    }

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
     * Builds the shadow (non-compiled) level representation of the db-net rollback arc.
     *
     * @param net The shadow (non-compiled) level representation of the db-net.
     * @return The shadow (non-compiled) level representation of the db-net rollback arc.
     */
    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowRollbackArc(((ShadowDBNetTransition) startShadow()), ((ShadowPlace) endShadow()));
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        return shadow;
    }

    /**
     * Returns the previously built shadow (non-compiled) level representation of the db-net rollback arc.
     *
     * @return The previously built shadow (non-compiled) level representation of the db-net rollback arc.
     */
    @Override
    public ShadowRollbackArc getShadow() {
        return shadow;
    }

    /**
     * Returns the shadow rollback arc type number.
     *
     * @return The shadow rollback arc type number.
     */
    @Override
    public int getArcType() {
        return ShadowRollbackArc.ROLLBACK_ARC;
    }
}
