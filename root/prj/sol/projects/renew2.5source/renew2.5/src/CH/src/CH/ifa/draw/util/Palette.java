package CH.ifa.draw.util;

import CH.ifa.draw.standard.ToolButton;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JToolBar;


public class Palette {
    private JToolBar toolBar;

    public Palette(String title) {
        toolBar = new JToolBar();
        toolBar.setName(title);
        toolBar.setToolTipText(title);
        // toolBar.setBackground(Color.lightGray);
        // toolBar.setLayout(new PaletteLayout(2, new Point(2, 2), false));
        toolBar.setRollover(true);
        toolBar.setMargin(new Insets(0, 0, 0, 0));
    }

    public Component getComponent() {
        return toolBar;
    }

    public Component add(ToolButton c) {
        toolBar.add(c.button());
        return c.button();
    }
}