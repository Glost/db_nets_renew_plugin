package de.renew.gui;

import de.renew.dbnets.shadow.node.ShadowDBNetTransition;
import de.renew.dbnets.shadow.node.ShadowReadArc;
import de.renew.dbnets.shadow.node.ShadowViewPlace;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetElement;

import java.util.Objects;

/**
 * The db-net read arc connection UI figure.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ReadArcConnection extends ArcConnection {

    /**
     * The db-net read arc connection UI figure constant.
     */
    public static final ReadArcConnection READ_ARC_CONNECTION = new ReadArcConnection();

    /**
     * The shadow (non-compiled) level representation of the db-net read arc
     * formed from the Renew UI db-net's drawing.
     */
    private transient ShadowReadArc shadow;

    /**
     * The db-net read arc connection UI figure's constructor.
     */
    public ReadArcConnection() {
        setStartDecoration(null);
        setEndDecoration(null);
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
     * Builds the shadow (non-compiled) level representation of the db-net read arc.
     *
     * @param net The shadow (non-compiled) level representation of the db-net.
     * @return The shadow (non-compiled) level representation of the db-net read arc.
     */
    @Override
    public ShadowNetElement buildShadow(ShadowNet net) {
        shadow = new ShadowReadArc(((ShadowViewPlace) startShadow()), ((ShadowDBNetTransition) endShadow()));
        shadow.context = this;
        shadow.setID(getID());
        shadow.setTrace(getTraceMode());
        return shadow;
    }

    /**
     * Returns the previously built shadow (non-compiled) level representation of the db-net read arc.
     *
     * @return The previously built shadow (non-compiled) level representation of the db-net read arc.
     */
    @Override
    public ShadowReadArc getShadow() {
        return shadow;
    }

    /**
     * Returns the shadow read arc type number.
     *
     * @return The shadow read arc type number.
     */
    @Override
    public int getArcType() {
        return ShadowReadArc.READ_ARC;
    }
}
