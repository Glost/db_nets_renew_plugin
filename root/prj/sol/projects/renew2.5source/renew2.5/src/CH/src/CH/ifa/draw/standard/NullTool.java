/*
 * @(#)Tool.java 5.1
 *
 */
package CH.ifa.draw.standard;

import CH.ifa.draw.framework.Tool;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


public class NullTool implements Tool {
    public static NullTool INSTANCE = new NullTool();

    private NullTool() {
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void mouseDown(MouseEvent e, int x, int y) {
    }

    public void mouseDrag(MouseEvent e, int x, int y) {
    }

    public void mouseUp(MouseEvent e, int x, int y) {
    }

    public void mouseMove(MouseEvent evt, int x, int y) {
    }

    public void keyDown(KeyEvent evt, int key) {
    }

    public void draw(Graphics g) {
    }
}