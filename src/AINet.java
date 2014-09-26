import static java.lang.StrictMath.*;
import static java.lang.StrictMath.signum;

/**
 * Created by antoshkaplus on 9/20/14.
 */
public class AINet {
    private static final double GOALIE_SPEED = 6;
    private static final double ANGLE_DEVIATION_FACTOR = 0.7;

    private final AILine netSegment;
    private final AILine goalieSegment;
    private final AILine preGoalieSegment;

    AINet(AILine netSegment, AILine goalieSegment) {
        this.netSegment = netSegment;
        this.goalieSegment = goalieSegment;
        double x = 2*goalieSegment.one.x - netSegment.one.x;
        preGoalieSegment = new AILine(new AIPoint(x, goalieSegment.one.y),
                                      new AIPoint(x, goalieSegment.two.y));

    }

    double getNetBottom() {
        return max(netSegment.one.y, netSegment.two.y);
    }

    double getNetTop() {
        return min(netSegment.one.y, netSegment.two.y);
    }

    double getNetX() {
        return netSegment.one.x;
    }

    double getGoalieX() {
        return goalieSegment.one.x;
    }

    AIPoint getNetCenter() {
        return netSegment.middle();
    }



    AILine getNetSegment() {
        return netSegment;
    }

    private boolean isScoreAngle(AIPoint bar_0, AIPoint bar_1,
                         AIPoint origin, double angle, double deviation) {
        double angle_0 = AI.orientAngle(angle,
                AI.orientAngle(AIPoint.difference(bar_0, origin)));
        double angle_1 = AI.orientAngle(angle,
                AI.orientAngle(AIPoint.difference(bar_1, origin)));
        double abs_0 = abs(angle_0);
        double abs_1 = abs(angle_1);
        return abs_0 >= deviation && abs_1 >= deviation &&
               abs_0 < PI/2 && abs_1 < PI/2 &&
               angle_0 * angle_1 < 0;

    }

    boolean isScoreAngle(AIPoint origin, double angle, double deviation) {
        double angleOne = AI.orientAngle(angle,
                AI.orientAngle(AIPoint.difference(netSegment.one, origin)));
        double angleTwo = AI.orientAngle(angle,
                AI.orientAngle(AIPoint.difference(netSegment.two, origin)));
        double absOne = abs(angleOne);
        double absTwo = abs(angleTwo);
        return absOne >= deviation && absTwo >= deviation &&
               absOne < PI/2 && absTwo < PI/2 &&
               angleOne * angleTwo < 0;
    }

    boolean isNearestScoreAngle(AIPoint origin, double angle, double deviation) {
        AIPoint near = netSegment.nearestPoint(origin);
        return isScoreAngle(near, getNetCenter(), origin, angle, deviation);
    }

    boolean isFarthestScoreAngle(AIPoint origin, double angle, double deviation) {
        AIPoint far = netSegment.farthestPoint(origin);
        return isScoreAngle(far, getNetCenter(), origin, angle, deviation);
    }

    /** returns -PI, PI orient orientAngle to strike
     deviation should be positive
     can return NaN easy*/
    double bestScoreAngle(AIPoint origin,
                          double deviation) {

        AIPoint bar = netSegment.farthestPoint(origin);
        double barAngle = AI.orientAngle(AIPoint.difference(bar, origin));
        // [-PI, PI], [-PI, PI], [0, PI]
        for (Double angle : new Double[]{AI.orientAngle(barAngle + deviation),
                                         AI.orientAngle(barAngle - deviation)}) {
            if (AI.isSegmentIntersectRay(netSegment, origin, angle)) {
                return angle;
            }
        }
        return Double.NaN;
    }

    // will strike right now
    // should consider SWING thing
    boolean canScoreStrike(AIHockeyist hockeyist) {
        AILine net = null;
        AIHockeyist goalie = null;

        // can control deviation by sufficient coefficient 0.5
        if (isScoreAngle(hockeyist.getLocation(),
                         hockeyist.getAngle(),
                         ANGLE_DEVIATION_FACTOR * hockeyist.getPuckAngleDeviation())) {



            double startingSpeed = hockeyist.getStrikePuckSpeed();
            AIPoint p = hockeyist.getPuckLocation();
            // here we can go better by checking angle nearer than current one
            return !canGoalieIntercept(p, AI.orientAngle(hockeyist.getAngle() + hockeyist.getPuckAngleDeviation()), startingSpeed);
        }
        return false;
    }


    // pass power is always 1
    boolean canScorePass(AIHockeyist hockeyist, double passAngle) {
        if (!hockeyist.isPassAngle(passAngle)) return false;
        double angle = AI.orientAngle(hockeyist.getAngle() + passAngle);
        // like previous method
        if (isScoreAngle(hockeyist.getLocation(), angle,
                         ANGLE_DEVIATION_FACTOR * hockeyist.getPuckAngleDeviation())) {
            // good angle
            double startingSpeed = hockeyist.getPassPuckSpeed(1, passAngle);
            hockeyist.setAngle(angle);
            AIPoint p = hockeyist.getPuckLocation();
            hockeyist.setAngle(AI.orientAngle(angle - passAngle));
            return !canGoalieIntercept(p, angle, startingSpeed);
        }
        return false;
    }


    boolean canGoalieIntercept(AIPuck puck) {
        return canGoalieIntercept(
                puck.getLocation(),
                puck.getSpeedAngle(),
                puck.getSpeedScalar());
    }

    // put inside puck info
    // should be careful with ticks - goalie can get better values
    // goalie is behind on one tick
    // i won't check if angle inside segment
    boolean canGoalieIntercept(AIPoint origin,
                               double angle,
                               double startSpeed) {

        AIFriction friction = AIFriction.getInstance();
        AIManager manager = AIManager.getInstance();
        AILine puckLine = new AILine(origin, angle);
        // lets compute goalie location
        AIPoint goalieLocation;
        if (manager.getCentralZone().isInside(origin)) {
            goalieLocation = new AIPoint(goalieSegment.one.x, origin.y);
        } else {
            goalieLocation = goalieSegment.nearestPoint(origin);
        }
        // can't do anything if he is already on line
        if (puckLine.fromPointDistance(goalieLocation) <
                AIPuck.RADIUS + AIHockeyist.RADIUS) return true;
        // before going into goalie zone
        if (!AI.isValueBetween(
                goalieSegment.one.y,
                goalieSegment.two.y,
                origin.y)) {
            // correct start speed and location
            AIPoint point = goalieSegment.nearestPoint(origin);
            AILine line = new AILine(point, 0);
            point = line.intersection(puckLine);
            startSpeed = friction.puckAfterDistance(origin.distance(point), startSpeed).speed;
            // reassigning variables
            origin = point;
            goalieLocation.set(goalieSegment.nearestPoint(origin));
        }
        // first consider goalie line as most dangerous
        AIPoint pointGoalie = puckLine.intersection(goalieSegment);
        double ticks = friction.puckAfterDistance(origin.distance(pointGoalie), startSpeed).ticks;
        // need future goalie location
        double goalieY = goalieLocation.y + ticks * GOALIE_SPEED * signum(pointGoalie.y - goalieLocation.y);
        double pointGoalieDistance = pointGoalie.distance(goalieLocation.x, goalieY);
        if (pointGoalieDistance < AIPuck.RADIUS + AIHockeyist.RADIUS ||
                !AI.isValueBetween(goalieSegment.one.y, goalieSegment.two.y, goalieY)) return true;
        // now consider pre goalie line
        AIPoint pointPre = puckLine.intersection(preGoalieSegment);
        ticks = friction.puckAfterDistance(origin.distance(pointPre), startSpeed).ticks;
        // need future goalie location
        double yPre = goalieLocation.y + ticks * GOALIE_SPEED * signum(pointPre.y - goalieLocation.y);
        if (pointPre.distance(goalieLocation.x, yPre) < AIPuck.RADIUS + AIHockeyist.RADIUS ||
                !AI.isValueBetween(goalieSegment.one.y, goalieSegment.two.y, goalieY)) return true;
        // could consider post goalie line location
        return false;
    }


}
