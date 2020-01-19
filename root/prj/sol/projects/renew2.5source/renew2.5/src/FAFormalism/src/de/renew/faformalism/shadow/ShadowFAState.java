package de.renew.faformalism.shadow;

import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNode;


public class ShadowFAState extends ShadowNode {
    public static org.apache.log4j.Logger logger = org.apache.log4j.Logger
                                                       .getLogger(ShadowFAState.class);

    // State types
    public static final int NULL = 0;
    public static final int START = 1;
    public static final int END = 2;
    public static final int STARTEND = 3;
    public int stateType;

    public ShadowFAState(ShadowNet shadowNet) {
        super(shadowNet);
    }

    @Override
    public void discard() {
        logger.debug("discard() called by " + this);
        super.discard();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}