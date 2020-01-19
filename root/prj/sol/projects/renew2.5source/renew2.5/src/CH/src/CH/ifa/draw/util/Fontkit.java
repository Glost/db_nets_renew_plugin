package CH.ifa.draw.util;

public class Fontkit {
    private final static int BUFFERSIZE = 100; // number of cached Fonts
    private static FontDescriptor[] fontBuffer = new FontDescriptor[BUFFERSIZE];
    private static int next = 0;

    public static ExtendedFont getFont(String name, int style, int size) {
        FontDescriptor fd;
        for (int i = 0; i < BUFFERSIZE; ++i) {
            fd = fontBuffer[i];
            if (fd == null) {
                break;
            }
            if (fd.equals(name, style, size)) {
                return fd.font;
            }
        }
        fd = new FontDescriptor(name, style, size);
        fontBuffer[next] = fd;
        next = (next + 1) % BUFFERSIZE;
        return fd.font;
    }
}

class FontDescriptor {
    final String name;
    final int style;
    final int size;
    final ExtendedFont font;

    FontDescriptor(String name, int style, int size) {
        this.name = name;
        this.style = style;
        this.size = size;
        this.font = new ExtendedFont(name, style, size);
    }

    boolean equals(String name, int style, int size) {
        return this.style == style && this.size == size
               && this.name.equals(name);
    }
}