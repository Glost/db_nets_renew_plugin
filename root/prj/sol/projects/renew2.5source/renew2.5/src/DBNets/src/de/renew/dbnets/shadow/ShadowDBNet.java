package de.renew.dbnets.shadow;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;

/**
 * The shadow (non-compiled) level representation of the db-net.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 2nd year student.
 *         Master Thesis on the Topic
 *         "Method of Performance Analysis of Time-Critical Applications Using DB-Nets".
 *         HSE University, Moscow, Russia, 2019 - 2021.
 */
public class ShadowDBNet extends ShadowNet {

    /**
     * The shadow (non-compiled) level representation of the db-net.
     *
     * @param name The db-net name.
     * @param netSystem The shadow net system instance.
     */
    public ShadowDBNet(String name, ShadowNetSystem netSystem) {
        super(name, netSystem);
    }
}
