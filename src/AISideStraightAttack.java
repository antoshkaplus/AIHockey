import model.ActionType;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.signum;

/**
 * Created by antoshkaplus on 9/21/14.
 */
public class AISideStraightAttack implements AIRole {
    private List<AIPoint> middleLocations = new ArrayList<AIPoint>(2);
    private static final double LOCATION_RADIUS = 100;
    private static final int SWING_TICKS = 10;

    public AISideStraightAttack() {
        AIManager manager = AIManager.getInstance();
        AIPoint center = manager.getCenter();
        AIRectangle rink = manager.getRink();
        middleLocations.add(new AIPoint(center.x, rink.origin.y + 40));
        middleLocations.add(new AIPoint(center.x, rink.origin.y + rink.size.y - 40));

    }

    @Override
    public AIMove move(AIHockeyist hockeyist) {
        AIManager manager = AIManager.getInstance();
        AIRectangle hisCentralZone = manager.getHisCentralZone();
        AIRectangle hisNoScoreZone = manager.getHisNoScoreZone();
        AINet hisNet = manager.getHisNet();
        AIPoint center = manager.getCenter();
        AIRectangle myZone = manager.getMyZone();
        AIFriction friction = AIFriction.getInstance();
        AIRectangle hisZone = manager.getHisZone();
        int currentTick = manager.getCurrentTick();
        AINet myNet = manager.getMyNet();

        AIMove move = new AIMove();

        if (!manager.isPuckOwner(hockeyist) ||
                hisCentralZone.isInside(hockeyist.getLocation()) ||
                    hisNoScoreZone.isInside(hockeyist.getLocation())) {
            if (hockeyist.getLastAction() == ActionType.SWING) {
                move.setAction(ActionType.CANCEL_STRIKE);
                return move;
            } else {
                return AIGo.to(hockeyist, AIPoint.middle(center, myNet.getNetCenter()));
            }
        }
        AIPoint bar = hockeyist.farthestPoint(hisNet.getNetSegment());
        AIPoint middleLocation = hockeyist.nearestPoint(middleLocations);
        if (myZone.isInside(hockeyist.getLocation()) &&
                hockeyist.distanceTo(middleLocation) > LOCATION_RADIUS) {
            double d = friction.hockeyistStopDistance(hockeyist.getSpeedScalar()).distance;
            AIPoint p = new AIPoint(hockeyist.getLocation());
            AIPoint r = AIPoint.unit(hockeyist.getSpeed());
            r.scale(d);
            r.translate(p);
            if (r.distance(middleLocation) < LOCATION_RADIUS) {
                move.setTurn(hockeyist.angleTo(bar));
            } else {
                move = AIGo.to(hockeyist, middleLocation);
            }
            return move;
        }

        // should be positive
        double speedTurnAngle = AI.angle(hockeyist.getSpeed(), AI.unit(hockeyist.getAngle()));
        // those are oriented
        double scoreAngle = hisNet.bestScoreAngle(
                hockeyist.getNextLocation(0),
                hockeyist.getPuckAngleDeviation());
        double turnAngle = AI.orientAngle(hockeyist.getAngle(), scoreAngle);
        if (hockeyist.distanceTo(middleLocation) < LOCATION_RADIUS) {
            move.setTurn(turnAngle);
            if (abs(turnAngle) < AI.DEGREE &&
                    (hockeyist.getSpeed().scalar() < 1 || speedTurnAngle < AI.DEGREE)) {
                move.setSpeedUp(1);
            }
            return move;
        }

        if (hisZone.isInside(hockeyist.getLocation()) &&
                !hisNet.isScoreAngle(
                        hockeyist.getLocation(),
                        hockeyist.getAngle(),
                        hockeyist.getPuckAngleDeviation()/2)) {
            move.setValid(false);
            return move;
        }

        move.setSpeedUp(1);

        AILine lineBound = new AILine(hisNet.getNetSegment().nearestPoint(hockeyist.getLocation()), 0);
        AILine lineMove = new AILine(hockeyist.getLocation(), hockeyist.getAngle());
        AIPoint endPoint = lineBound.intersection(lineMove);
        double dist = hockeyist.distanceTo(endPoint);
        if (friction.hockeyistAfterTicks(SWING_TICKS + 3, hockeyist.getSpeedScalar()).distance > dist) {
            // time to swing
            move.setAction(ActionType.SWING);
        }
        // also should be able to have good angle... goalie can't stand a chance
        if (hockeyist.getLastAction() == ActionType.SWING &&
                currentTick - hockeyist.getLastActionTick() > SWING_TICKS &&
                    hisNet.canScoreStrike(hockeyist)) {
            move.setAction(ActionType.STRIKE);
        }
        return move;
    }
}
