package de.renew.dbnets.shadow.node;

import de.renew.shadow.ShadowArc;

/**
 * The shadow (non-compiled) level representation of the db-net read arc formed from the Renew UI db-net's drawing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
 */
public class ShadowReadArc extends ShadowArc {

    /**
     * The shadow read arc type number.
     */
    public static final int READ_ARC = 1006;

    /**
     * The shadow db-net read arc's constructor.
     *
     * @param from The shadow view place which is one of the ends of the shadow read arc.
     * @param to The shadow transition which is one of the ends of the shadow read arc.
     */
    public ShadowReadArc(ShadowViewPlace from, ShadowDBNetTransition to) {
        super(from, to, READ_ARC);
    }

    /**
     * The shadow db-net read arc's constructor.
     *
     * @param from The shadow view place which is one of the ends of the shadow read arc.
     * @param to The shadow transition which is one of the ends of the shadow read arc.
     * @param inscription The shadow read arc's inscription string.
     */
    public ShadowReadArc(ShadowViewPlace from, ShadowDBNetTransition to, String inscription) {
        super(from, to, READ_ARC, inscription);
    }
}
