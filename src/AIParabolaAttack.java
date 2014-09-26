import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;
import static java.lang.Math.min;
import static java.lang.Math.signum;

/**
 * Created by antoshkaplus on 9/24/14.
 *
 * going by parabola fire by pass or strike
 * there are exists two parabolas we should choose one
 *
 */
public class AIParabolaAttack implements AIRole {
    enum Orientation {
        BOTTOM,
        TOP
    }

    AIManager manager = AIManager.getInstance();

    long hockeyistId;
    AITurnAttack attack;
    Orientation orientation;

    // x = a * (y-d)^2 + c
    double d;
    double a;
    double c;

    static final double INDENT = AIHockeyist.RADIUS + 2*AIPuck.RADIUS;

    AIPoint enterLocation;
    AIPoint scoreLocation;

    AIParabolaAttack(long hockeyistId, Orientation orientation) {
        this.hockeyistId = hockeyistId;
        attack = new AITurnAttack(hockeyistId);
        init(orientation);
    }

    private void init(Orientation orientation) {
        AIPoint center = manager.getCenter();
        AIRectangle rink = manager.getRink();
        AINet net = manager.getHisNet();
        double yEnter, yScore;
        if (orientation == Orientation.BOTTOM) {
            yEnter = min(rink.getBottom() - INDENT, getBottomY(center.x));
            yScore = net.getNetBottom() + 2*AIPuck.RADIUS;
        } else {
            yEnter = max(rink.getTop() + INDENT, getTopY(center.x));
            yScore = net.getNetTop() - 2*AIPuck.RADIUS;
        }
        enterLocation = new AIPoint(center.x, yEnter);
        double dx = 2*AIHockeyist.RADIUS + 2*AIPuck.RADIUS + 170;
        double x = net.getGoalieX() + signum(net.getGoalieX() - net.getNetX()) * dx;
        scoreLocation = new AIPoint(x, yScore);
        AIPoint v = AIPoint.difference(scoreLocation,
                net.getNetSegment().farthestPoint(scoreLocation));
        double derivative = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v)).x;
        d = net.getNetCenter().y;
        a = derivative/(2*(scoreLocation.y - d));
        c = scoreLocation.x - a*pow(scoreLocation.y - d, 2);
    }


    double getX(double y) {
        return (y - d)*(y - d)*a + c;
    }

    double getY(double x, double sign) {
        return sign*sqrt((x - c)/a) + d;
    }
    // can return NaN
    double getTopY(double x) {
        AIRectangle rink = AIManager.getInstance().getRink();
        double t = getY(x, -1);
        return max(getY(x, -1), rink.getTop() + INDENT);
    }

    double getBottomY(double x) {
        AIRectangle rink = AIManager.getInstance().getRink();
        return min(getY(x, 1), rink.getBottom() - INDENT);
    }

    @Override
    public AIMove move() {
        AIMove m;
        m =  attack.move();
        if (m.isValid()) {
            return m;
        }
        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
        AIRectangle myZone = manager.getMyZone();
        AIRectangle hisNoScoreZone = manager.getHisNoScoreZone();
        AIRectangle centralZone = manager.getCentralZone();
        AINet hisNet = manager.getHisNet();
        AIPoint center = manager.getCenter();
        AIPoint netCenter = hisNet.getNetCenter();

        AIMove move = new AIMove();
        if (myZone.isInside(hockeyist.getLocation()) && hockeyist.distanceTo(enterLocation) > 40) {
            // would be find to have this with angle
            return AIGo.to(hockeyist, enterLocation);
        } else {
            // if hockeyist inside no score zone or to far from control point we should withdraw
            if (hisNoScoreZone.isInside(hockeyist.getLocation()) ||
                    centralZone.isInside(hockeyist.getLocation())) {
                return AIGo.to(hockeyist, manager.getCenter());
            }
            // later should reinitialize for future point
            double x = hockeyist.getX();
            double y = hockeyist.getY();
            double yBottom = getBottomY(x);
            double yTop = getTopY(x);
            AIPoint bar = hockeyist.farthestPoint(hisNet.getNetSegment());
            // using speed vector aren't quite working well
            // hisGoalie can go down...
            x = hockeyist.getX() - signum(center.x - netCenter.x) * 10;
            if (AI.isValueBetween(
                    netCenter.x,
                    scoreLocation.x,
                    hockeyist.getX())) {

                AIMove mm = AIGo.to(hockeyist, bar, AIGo.GoType.MAX_ACCELERATION);
                mm.setSpeedUp(mm.getSpeedUp()*0.5);
                return mm;
            }
            ArrayList<AIPoint> locations = new ArrayList<AIPoint>(2);
            locations.add(new AIPoint(x, getBottomY(x)));
            locations.add(new AIPoint(x, getTopY(x)));
            AIPoint aim = hockeyist.nearestPoint(locations);
            AIMove kk = AIGo.to(hockeyist, aim, AIGo.GoType.MAX_ACCELERATION);
            kk.setSpeedUp(kk.getSpeedUp()*0.5);
            return kk;
        }
    }

    //    @Override
//    public AIMove move() {
//        AIMove m;
//        m =  attack.move();
//        if (m.isValid()) {
//            return m;
//        }
//        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
//        AIRectangle myZone = manager.getMyZone();
//        AIRectangle hisNoScoreZone = manager.getHisNoScoreZone();
//        AIRectangle centralZone = manager.getCentralZone();
//        AINet hisNet = manager.getHisNet();
//        AIPoint center = manager.getCenter();
//        AIPoint netCenter = hisNet.getNetCenter();
//
//        AIMove move = new AIMove();
//        if (myZone.isInside(hockeyist.getLocation())) {
//            // would be find to have this with angle
//            return AIGo.to(hockeyist, hockeyist.nearestPoint(enterLocations));
//        } else {
//            // if hockeyist inside no score zone or to far from control point we should withdraw
//            if (hisNoScoreZone.isInside(hockeyist.getLocation()) || centralZone.isInside(hockeyist.getLocation())) {
//                move.setValid(false);
//                return move;
//            }
//            // later should reinitialize for future point
//            double x = hockeyist.getX();
//            double y = hockeyist.getY();
//            double yBottom = getBottomY(x);
//            double yTop = getTopY(x);
//            AIPoint bar = hockeyist.farthestPoint(hisNet.getNetSegment());
//            // using speed vector aren't quite working well
//            // hisGoalie can go down...
//            x = hockeyist.getX() - signum(center.x - netCenter.x) * 10;
//            if (AI.isValueBetween(
//                    netCenter.x,
//                    scoreLocations.get(0).x,
//                    hockeyist.getX())) {
//                return AIGo.to(hockeyist, bar);
//            }
//            ArrayList<AIPoint> locations = new ArrayList<AIPoint>(2);
//            locations.add(new AIPoint(x, getBottomY(x)));
//            locations.add(new AIPoint(x, getTopY(x)));
//            AIPoint aim = hockeyist.nearestPoint(locations);
//            return AIGo.to(hockeyist, aim);
//        }
//
//    }


//    // enter points are same for both sides
//    // bottom goes first
//    List<AIPoint> enterLocations;
//    // depend on current game
//    List<AIPoint> scoreLocations;
//
//    AIParabolaAttack(long hockeyistId, Orientation orientation) {
//        this.hockeyistId = hockeyistId;
//        attack = new AIAttack(hockeyistId);
//        AIManager manager = AIManager.getInstance();
//        AIPoint center = manager.getCenter();
//        AIRectangle rink = manager.getRink();
//        AINet net = manager.getHisNet();
//        AILine hisNetSegment = net.getNetSegment();
//
//        enterLocations = new ArrayList<AIPoint>(2);
//        enterLocations.add(new AIPoint(
//                center.x,
//                min(rink.getBottom() - INDENT, getBottomY(center.x))));
//        enterLocations.add(new AIPoint(
//                center.x,
//                max(rink.getTop() + INDENT, getTopY(center.x))));
//
//        scoreLocations = new ArrayList<AIPoint>(2);
//        double dx = 2*AIHockeyist.RADIUS + 2*AIPuck.RADIUS + 170;
//        double x = net.getGoalieX() + signum(net.getGoalieX() - net.getNetX()) * dx;
//        // can play with this coefficients
//        scoreLocations.add(new AIPoint(x, net.getNetBottom() + 2*AIPuck.RADIUS));
//        scoreLocations.add(new AIPoint(x, net.getNetTop() - 2*AIPuck.RADIUS));
//
//        List<AIPoint> netList = new ArrayList<AIPoint>(2);
//        // need farthest one for current
//        netList.add(hisNetSegment.nearestPoint(scoreLocations.get(1)));
//        netList.add(hisNetSegment.nearestPoint(scoreLocations.get(0)));
//        AIPoint v_0 = AIPoint.difference(scoreLocations.get(0), netList.get(0));
//        AIPoint v_1 = AIPoint.difference(scoreLocations.get(1), netList.get(1));
//
//        // derivatives should be opposite to each other
//        double derivative_0 = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v_0)).x;
//        double derivative_1 = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v_1)).x;
//
//        if (abs(derivative_0 + derivative_1) > AI.COMPUTATION_BIAS) {
//            throw new RuntimeException("Derivatives should be equal");
//        }
//
//        // coefficients should work for either side
//        d = (scoreLocations.get(0).y + scoreLocations.get(1).y)/2;
//        a = derivative_0/(2*(scoreLocations.get(0).y - d));
//        c = scoreLocations.get(0).x - a*pow(scoreLocations.get(0).y - d, 2);
//    }
}
