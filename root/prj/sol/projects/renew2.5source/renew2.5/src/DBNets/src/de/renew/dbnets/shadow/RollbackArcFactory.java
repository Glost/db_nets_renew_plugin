package de.renew.dbnets.shadow;

import de.renew.formalism.java.SimpleArcFactory;
import de.renew.net.arc.Arc;

public class RollbackArcFactory extends SimpleArcFactory {

    public RollbackArcFactory() {
        super(Arc.in, false);
    }
}
