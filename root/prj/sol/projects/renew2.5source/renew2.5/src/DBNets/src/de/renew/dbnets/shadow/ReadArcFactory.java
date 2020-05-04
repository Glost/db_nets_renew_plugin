package de.renew.dbnets.shadow;

import de.renew.formalism.java.SimpleArcFactory;
import de.renew.net.arc.Arc;

public class ReadArcFactory extends SimpleArcFactory {

    public ReadArcFactory() {
        super(Arc.in, false);
    }
}
