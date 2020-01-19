package CH.ifa.draw.util;

import java.io.IOException;


public class UnknownTypeException extends IOException {
    private final String type;

    public UnknownTypeException(final String message, final String type) {
        super(message);
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}