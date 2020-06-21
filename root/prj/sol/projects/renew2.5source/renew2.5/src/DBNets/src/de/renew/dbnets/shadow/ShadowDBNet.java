package de.renew.dbnets.shadow;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;

/**
 * The shadow (non-compiled) level representation of the db-net.
 *
 * @author Anton Rigin, National Research University - Higher School of Economics, Faculty of Computer Science,
 *         Master Degree Program "System and Software Engineering", the 1st year student.
 *         Term Project (Coursework) on the Topic
 *         "Reference and Data Semantic-Based Simulator of Petri Nets Extension with the Use of Renew Tool".
 *         HSE University, Moscow, Russia, 2019 - 2020.
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
