package de.renew.dbnets.shadow.node;

import de.renew.shadow.ShadowArc;
import de.renew.shadow.ShadowPlace;

/**
 * The shadow (non-compiled) level representation of the db-net rollback arc formed from the Renew UI db-net's drawing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ShadowRollbackArc extends ShadowArc {

    /**
     * The shadow rollback arc type number.
     */
    public static final int ROLLBACK_ARC = 1007;

    /**
     * The shadow db-net rollback arc's constructor.
     *
     * @param from The shadow transition which is one of the ends of the shadow rollback arc.
     * @param to The shadow place which is one of the ends of the shadow rollback arc.
     */
    public ShadowRollbackArc(ShadowDBNetTransition from, ShadowPlace to) {
        super(from, to, ROLLBACK_ARC);
    }

    /**
     * The shadow db-net rollback arc's constructor.
     *
     * @param from The shadow transition which is one of the ends of the shadow rollback arc.
     * @param to The shadow place which is one of the ends of the shadow rollback arc.
     * @param inscription The shadow rollback arc's inscription string.
     */
    public ShadowRollbackArc(ShadowDBNetTransition from, ShadowPlace to, String inscription) {
        super(from, to, ROLLBACK_ARC, inscription);
    }
}
