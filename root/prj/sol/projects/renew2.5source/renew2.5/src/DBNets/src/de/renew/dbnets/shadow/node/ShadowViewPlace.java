package de.renew.dbnets.shadow.node;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowPlace;

/**
 * The shadow (non-compiled) level representation of the db-net view place formed from the Renew UI db-net's drawing.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
