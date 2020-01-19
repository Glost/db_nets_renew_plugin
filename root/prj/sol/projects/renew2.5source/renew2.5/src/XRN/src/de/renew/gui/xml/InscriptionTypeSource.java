package de.renew.gui.xml;

import de.renew.shadow.ShadowCompilerFactory;
import de.renew.shadow.ShadowNet;
import de.renew.shadow.ShadowNetSystem;


public abstract class InscriptionTypeSource {
    private ShadowCompilerFactory compilerFactory;

    public InscriptionTypeSource(ShadowCompilerFactory compilerFactory) {
        this.compilerFactory = compilerFactory;
    }

    public String getType(String text, boolean special) {
        String type = null;
        try {
            // Try to determine the inscription
            // type in an empty shadow net.
            // This should return the same type,
            // but it runs faster and does not need
            // access to the declaration node.
            ShadowNet emptyNet = new ShadowNet("",
                                               new ShadowNetSystem(compilerFactory));

            type = calculateType(text, special, emptyNet);
        } catch (Exception e) {
            // No inscription type can be determined.
            return null;
        }
        return type;
    }

    public abstract String calculateType(String text, boolean special,
                                         ShadowNet net)
            throws Exception;
}