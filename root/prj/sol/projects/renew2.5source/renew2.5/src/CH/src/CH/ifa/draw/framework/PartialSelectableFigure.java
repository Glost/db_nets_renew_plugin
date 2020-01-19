package CH.ifa.draw.framework;

public interface PartialSelectableFigure {
    public boolean isModifierSelectable();

    public boolean isSelectableInRegion(int x, int y);
}