package de.renew.util;

import java.io.IOException;
import java.io.ObjectInput;


/**
 * Internal class needed by {@link ClassSource}.
 *
 * @author Michael Duvigneau
 **/
public class ReloadableDeserializerImpl {
    public Object readObject(ObjectInput input)
            throws ClassNotFoundException, IOException {
        return input.readObject();
    }
}