package CH.ifa.draw.framework;

public interface DrawingContext {
    public boolean isHighlighted(Figure figure);

    public boolean isVisible(Figure figure);

    public String expandMacro(String text);
}