package de.renew.dbnets.shadow.node;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowPlace;

/**
 * The shadow (non-compiled) level representation of the db-net view place formed from the Renew UI db-net's drawing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ShadowViewPlace extends ShadowPlace {

    /**
     * The shadow db-net view place's constructor.
     *
     * @param shadowNet The shadow (non-compiled) level representation of the db-net.
     */
    public ShadowViewPlace(ShadowNet shadowNet) {
        super(shadowNet);
    }
}
