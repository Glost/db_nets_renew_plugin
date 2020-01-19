package CH.ifa.draw.io;

public class ImageFileFilter extends CombinationFileFilter {
    public ImageFileFilter() {
        super("Image File (png, jpg, gif)");
        add(new PNGFileFilter());
        add(new JPGFileFilter());
        add(new GIFFileFilter());
    }

    public class JPGFileFilter extends CombinationFileFilter {
        public JPGFileFilter() {
            super("JPEG File Filter");
            add(new SimpleFileFilter("jpg",
                                     " Joint Photographic Experts Group File"));
            add(new SimpleFileFilter("jpeg",
                                     " Joint Photographic Experts Group File"));
        }
    }

    public class GIFFileFilter extends SimpleFileFilter {
        public GIFFileFilter() {
            super("gif", "Graphics Interchange Format File");
        }
    }
}