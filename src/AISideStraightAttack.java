import model.ActionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.signum;

import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/21/14.
 */
public class AISideStraightAttack implements AIRole {
    enum Orientation {
        BOTTOM,
        TOP
    }

    private long hockeyistId;
    private AIAttack attack;

    private AIManager manager = AIManager.getInstance();
    private AIPoint turnLocation;

    private static final double LOCATION_RADIUS = 70;
    private static final int HALF_SWING_TICKS = 10;
    private static final int FULL_SWING_TICKS = 20;

    public AISideStraightAttack(long hockeyistId, Orientation orientation) {
        this.hockeyistId = hockeyistId;
        attack = new AIAttack(hockeyistId);
        AIPoint center = manager.getCenter();
        AIRectangle rink = manager.getRink();
        if (orientation == Orientation.BOTTOM) {
            turnLocation = new AIPoint(
                    center.x,
                    rink.origin.y + rink.size.y - 50);
        } else {
            turnLocation = new AIPoint(
                    center.x,
                    rink.origin.y + 50);
        }
    }

    @Override
    public AIMove move() {
        AIHockeyist h = manager.getTeammate(hockeyistId);
        if (h != manager.getPuckOwner()) {
            throw new RuntimeException();
        }
        if (h.distanceTo(turnLocation) < LOCATION_RADIUS) {
            return onInTurnLocationZone();
        }
        if (manager.getMyZone().isInside(h.getLocation())) {
            return onInMyZone();
        }
        if (manager.getHisZone().isInside(h.getLocation())) {
            return onInHisZone();
        }
        throw new RuntimeException();
    }

    private AIMove onInTurnLocationZone() {
        AIMove m = new AIMove();
        AIHockeyist h = manager.getTeammate(hockeyistId);
        AINet net = manager.getHisNet();
        // should be positive
        double speedTurnAngle = abs(h.angleTo(h.getSpeedAngle()));
        // those are oriented
        double scoreAngle = net.bestScoreAngle(
                h.getNextLocation(1),
                h.getPuckAngleDeviation());
        double turnAngle = AI.orientAngle(h.getAngle(), scoreAngle);
        m.setTurn(turnAngle);
        if (abs(turnAngle) < AI.DEGREE &&
                (h.getSpeed().scalar() < 1 || speedTurnAngle < AI.DEGREE)) {
            m.setSpeedUp(1);
        } else {
            m.setSpeedUp(0);
        }
        return m;
    }

    private AIMove onInHisZone() {
        AINet net = manager.getHisNet();
        AIRectangle centralZone = manager.getCentralZone();
        AIRectangle noScoreZone = manager.getHisNoScoreZone();
        AIHockeyist h = manager.getTeammate(hockeyistId);
        AIMove m = attack.move();
        if (m.isValid()) {
            return m;
        }
        double scoreAngle = net.bestScoreAngle(h.getLocation(), 0.5*h.getPuckAngleDeviation());
        double a = abs(h.angleTo(scoreAngle));
        if (centralZone.isInside(h.getLocation()) ||
                noScoreZone.isInside(h.getLocation()) ||
                    a > PI/2) {
            return AIGo.to(h, manager.getCenter());
        }
        // some formula that will define how should speedup depending on
        // current speed, location and angle
        m = new AIMove();
        m.setTurn(h.angleTo(scoreAngle));
        if (a > 10 * AI.DEGREE) {
            m.setSpeedUp(-1);
            return m;
        }
        if (a > 5*AI.DEGREE) {
            m.setSpeedUp(1);
            return m;
        }
        m.setSpeedUp(1);
        return m;
    }

    private AIMove onInMyZone() {
        AIMove m = new AIMove();
        AIHockeyist h = manager.getTeammate(hockeyistId);
        AIFriction f = AIFriction.getInstance();
        AINet net = manager.getHisNet();

        double bestAngle = net.bestScoreAngle(h.getLocation(), 0.7*h.getPuckAngleDeviation());

        AILine speedLine = new AILine(h.getLocation(), h.getSpeedAngle());
        if (speedLine.fromPointDistance(turnLocation) < 20 &&
            f.hockeyistStopDistance(h.getSpeedScalar()).distance > h.distanceTo(turnLocation)) {

            m.setTurn(h.angleTo(bestAngle));
            if (h.angleTo(bestAngle) < AI.DEGREE) {
                m.setSpeedUp(1);
                return m;
            }
            // else do nothing, well
            m = AIGo.to(h, turnLocation);
            return m;
        } else {
            return AIGo.to(h, turnLocation);
        }
    }


//    private List<AIPoint> middleLocations = new ArrayList<AIPoint>(2);


//    public AISideStraightAttack(long hockeyistId, Orientation orientation) {
//        super(hockeyistId);
//        AIManager manager = AIManager.getInstance();
//        AIPoint center = manager.getCenter();
//        AIRectangle rink = manager.getRink();
//        middleLocations.add(new AIPoint(center.x, rink.origin.y + 40));
//        middleLocations.add(new AIPoint(center.x, rink.origin.y + rink.size.y - 40));
//    }
//
//    @Override
//    public AIMove move() {
//        AIManager manager = AIManager.getInstance();
//        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
//        AIRectangle hisCentralZone = manager.getHisCentralZone();
//        AIRectangle hisNoScoreZone = manager.getHisNoScoreZone();
//        AINet hisNet = manager.getHisNet();
//        AIPoint center = manager.getCenter();
//        AIRectangle myZone = manager.getMyZone();
//        AIFriction friction = AIFriction.getInstance();
//        AIRectangle hisZone = manager.getHisZone();
//        int currentTick = manager.getCurrentTick();
//        AINet myNet = manager.getMyNet();
//
//        AIMove move = new AIMove();
//
//        if (!manager.isPuckOwner(hockeyist)) {
//            move.setValid(false);
//            return move;
//        }
//
//        if (hisNet.canScoreStrike(hockeyist)
//                && (!manager.canOpponentIntercept(hockeyist.getLocation(), hockeyist.getAngle())
//                        || (hockeyist.getLastAction() == ActionType.SWING &&
//                            currentTick - hockeyist.getLastActionTick() >= FULL_SWING_TICKS))) {
//            hisNet.canScoreStrike(hockeyist);
//            move.setAction(ActionType.STRIKE);
//            return move;
//        }
//
//        AIHockeyist swingHockeyist = AIHockeyist.hockeyistAfterSwingTicks(hockeyist, HALF_SWING_TICKS);
//        if (hisNet.canScoreStrike(swingHockeyist)
//                && !manager.canOpponentIntercept(swingHockeyist.getLocation(), hockeyist.getAngle())
//                && !manager.canOpponentInterrupt(swingHockeyist.getLocation(), HALF_SWING_TICKS/3)) {
//            move.setAction(ActionType.SWING);
//            return move;
//        }
//
//        AIHockeyist fullSwingHockeyist = AIHockeyist.hockeyistAfterSwingTicks(hockeyist, FULL_SWING_TICKS);
//        if (hisNet.canScoreStrike(fullSwingHockeyist)
//                && manager.canOpponentIntercept(fullSwingHockeyist.getLocation(), hockeyist.getAngle())
//                && !manager.canOpponentInterrupt(fullSwingHockeyist.getLocation(), FULL_SWING_TICKS/3)) {
//            move.setAction(ActionType.SWING);
//            return move;
//        }
//
//        if (hockeyist.getLastAction() == ActionType.SWING
//                && manager.getCurrentTick() - hockeyist.getLastActionTick() > HALF_SWING_TICKS
//                && !hisNet.isFarthestScoreAngle(
//                        hockeyist.getLocation(),
//                        hockeyist.getAngle(),
//                        hockeyist.getPuckAngleDeviation()/2)) {
//            move.setAction(ActionType.CANCEL_STRIKE);
//            return move;
//        }
//
//        if (hisCentralZone.isInside(hockeyist.getLocation()) ||
//                    hisNoScoreZone.isInside(hockeyist.getLocation())) {
//            if (hockeyist.getLastAction() == ActionType.SWING) {
//                move.setAction(ActionType.CANCEL_STRIKE);
//                return move;
//            } else {
//                return AIGo.to(hockeyist, AIPoint.middle(center, myNet.getNetCenter()));
//            }
//        }
//
//        AIPoint bar = hockeyist.farthestPoint(hisNet.getNetSegment());
//        AIPoint middleLocation = hockeyist.nearestPoint(middleLocations);
//        if (myZone.isInside(hockeyist.getLocation()) &&
//                hockeyist.distanceTo(middleLocation) > LOCATION_RADIUS) {
//            double d = friction.hockeyistStopDistance(hockeyist.getSpeedScalar()).distance;
//            AIPoint p = new AIPoint(hockeyist.getLocation());
//            AIPoint r = AIPoint.unit(hockeyist.getSpeed());
//            r.scale(d);
//            r.translate(p);
//            // should use line
//            if (r.distance(middleLocation) < LOCATION_RADIUS) {
//                move.setTurn(hockeyist.angleTo(bar));
//            } else {
//                move = AIGo.to(hockeyist, middleLocation);
//            }
//            move = AIGo.to(hockeyist, middleLocation);
//            return move;
//        }
//
//        // should be positive
//        double speedTurnAngle = AI.angle(hockeyist.getSpeed(), AI.unit(hockeyist.getAngle()));
//        // those are oriented
//        double scoreAngle = hisNet.bestScoreAngle(
//                hockeyist.getNextLocation(1),
//                hockeyist.getPuckAngleDeviation());
//        double turnAngle = AI.orientAngle(hockeyist.getAngle(), scoreAngle);
//        if (hockeyist.distanceTo(middleLocation) < LOCATION_RADIUS) {
//            move.setTurn(turnAngle);
//            if (abs(turnAngle) < AI.DEGREE &&
//                    (hockeyist.getSpeed().scalar() < 1 || speedTurnAngle < AI.DEGREE)) {
//                move.setSpeedUp(1);
//            }
//            move.setSpeedUp(0.5);
//            return move;
//        }
//
//        move.setTurn(turnAngle);
//        move.setSpeedUp(1);
//
//        if (hisZone.isInside(hockeyist.getLocation()) &&
//                !hisNet.isFarthestScoreAngle(
//                        hockeyist.getLocation(),
//                        hockeyist.getAngle(),
//                        0)) {
//            move.setSpeedUp(-1);
//            return move;
//        }
//
//        return move;
//    }
}
