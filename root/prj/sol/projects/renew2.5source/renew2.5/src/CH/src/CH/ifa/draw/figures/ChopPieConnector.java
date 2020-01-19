/*
 * @(#)ChopEllipseConnector.java 5.1
 *
 */
package CH.ifa.draw.figures;

import org.apache.log4j.Logger;

import CH.ifa.draw.framework.Figure;

import CH.ifa.draw.standard.ChopBoxConnector;

import CH.ifa.draw.util.Geom;

import java.awt.Point;
import java.awt.Rectangle;


/**
 * A ChopPieConnector is a modification of the {@link ChopEllipseConnector}
 * that locates a connection point by chopping the connection at the
 * ellipse defined by the figure's display box, unless the corresponding
 * arc segment is outside of the start/end angles of the pie figure.
 * <p>
 * </p>
 * Created: 13 Jul 2008
 * @author Michael Duvigneau
 **/
public class ChopPieConnector extends ChopBoxConnector {
    public static final Logger logger = Logger.getLogger(ChopPieConnector.class);

    /*
     * Serialization support.
     */


    //private static final long serialVersionUID = ;
    private PieFigure pieFig;

    public ChopPieConnector() {
    }

    public ChopPieConnector(PieFigure owner) {
        super(owner);
        this.pieFig = owner;
    }

    protected Point chop(Figure target, Rectangle source) {
        Rectangle r = target.displayBox();
        Point from = Geom.center(source);
        double angle_rad = Geom.pointToAngle(r, from)
                           + (r.intersection(source).equals(r) ? Math.PI : 0);

        // Compare angle on ellipse with angle of arc/pie.
        // however, the first is given in radian, the latter in degrees
        // however, this is possible only if we know our pie angles.
        if (pieFig != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Chopping for pieFig=" + pieFig
                             + ", given target=" + target + ", given source="
                             + source + ".");
            }
            double angle_deg = PieFigure.normalizeAngle(-Math.toDegrees(angle_rad));
            double chosenAngle = angle_deg;
            if (logger.isDebugEnabled()) {
                logger.debug("   Angles are: angle_rad=" + angle_rad
                             + ", angle_deg=" + angle_deg
                             + ", pieFig.getStartAngle()="
                             + pieFig.getStartAngle()
                             + ", pieFig.getEndAngle()=" + pieFig.getEndAngle()
                             + ".");
            }
            if (pieFig.getStartAngle() < pieFig.getEndAngle()) {
                logger.debug("   start < end");
                if (angle_deg < pieFig.getStartAngle()) {
                    logger.debug("   angle < start < end");
                    // We are outside the arc segment on the ellipse
                    // (angle between zero and start).
                    // Compute the nearest arc end point and return it.
                    if ((pieFig.getStartAngle() - angle_deg) < (angle_deg + 360
                                                                       - pieFig
                                                                                 .getEndAngle())) {
                        chosenAngle = pieFig.getStartAngle();
                    } else {
                        chosenAngle = pieFig.getEndAngle();
                    }
                } else if (pieFig.getEndAngle() < angle_deg) {
                    logger.debug("   start < end < angle");
                    // We are outside the arc segment on the ellipse
                    // (angle between end and zero).
                    // Compute the nearest arc end point and return it.
                    if ((pieFig.getStartAngle() + 360 - angle_deg) < (angle_deg
                                                                             - pieFig
                                                                                       .getEndAngle())) {
                        chosenAngle = pieFig.getStartAngle();
                    } else {
                        chosenAngle = pieFig.getEndAngle();
                    }
                }
            } else if ((pieFig.getStartAngle() > pieFig.getEndAngle())
                               && (pieFig.getStartAngle() > angle_deg)
                               && (angle_deg > pieFig.getEndAngle())) {
                logger.debug("   end < angle < start");
                // we are outside the arc segment on the ellipse
                // (angle between end and start)
                // Compute the nearest arc end point and return it.
                if ((pieFig.getStartAngle() - angle_deg) < (angle_deg
                                                                   - pieFig
                                .getEndAngle())) {
                    chosenAngle = pieFig.getStartAngle();
                } else {
                    chosenAngle = pieFig.getEndAngle();
                }
            }
            if (chosenAngle != angle_deg) {
                angle_rad = -Math.toRadians(chosenAngle);
                if (logger.isDebugEnabled()) {
                    logger.debug("   chosen angle is: deg=" + chosenAngle
                                 + ", rad=" + angle_rad + ".");
                }
            } else {
                logger.debug("   chosen angle is unchanged.");
            }
        }
        return Geom.ovalAngleToPoint(r, angle_rad);
    }
}