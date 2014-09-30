
import model.Unit;

import java.util.*;

import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/11/14.
 */
public abstract class AIUnit {
    // bounds get from AIWorld

    private AIPoint speed = new AIPoint();
    private AIPoint location = new AIPoint();
    // this guy is oriented
    private double angle;

    protected AIUnit(Unit unit) {
        setLocation(unit.getX(), unit.getY());
        setSpeed(unit.getSpeedX(), unit.getSpeedY());
        setAngle(unit.getAngle());
    }

    protected AIUnit(AIUnit unit) {
        setLocation(unit.getLocation());
        setSpeed(unit.getSpeed());
        setAngle(unit.getAngle());
    }

    /** oriented orientAngle where should turn */
    double angleTo(double x, double y) {
        // returns -PI, PI
        double a = Math.atan2(y - location.y, x - location.x);
        return AI.orientAngle(angle, a);
    }
    double angleTo(AIPoint point) {
        return angleTo(point.x, point.y);
    }
    double angleTo(AIUnit u) {
        return angleTo(u.location);
    }
    double angleTo(double orientAngle) { return AI.orientAngle(getAngle(), orientAngle); }


    double distanceTo(double x, double y) { return location.distance(x, y); }
    double distanceTo(AIPoint point) {
        return location.distance(point);
    }
    double distanceTo(AIUnit unit) {
        return location.distance(unit.getLocation());
    }

    // in setters check shit

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    public void setLocation(AIPoint location) {
        this.location.set(location);
    }
    public void setLocation(double x, double y) { this.location.set(x, y); }
    public AIPoint getLocation() {
        return location;
    }

    public void setSpeed(AIPoint speed) {
        this.speed.set(speed);
    }
    public void setSpeed(double x, double y) { this.speed.set(x, y); }
    public double getSpeedScalar() {
        return AIPoint.ZERO.distance(speed);
    }

    public AIPoint getSpeed() {
        return speed;
    }

    public double getX() {
        return location.x;
    }

    public double getY() {
        return location.y;
    }

    public AIPoint nearestPoint(Collection<AIPoint> ps) {
        return Collections.min(ps, new AIPoint.DistanceComparator(getLocation()));
    }

    public AIPoint farthestPoint(Collection<AIPoint> ps) {
        return Collections.max(ps, new AIPoint.DistanceComparator(getLocation()));
    }

    public AIPoint nearestPoint(AILine netSegment) {
        double one = getLocation().distance(netSegment.one);
        double two = getLocation().distance(netSegment.two);
        return one > two ? netSegment.two : netSegment.one;
    }

    public AIPoint farthestPoint(AILine netSegment) {
        double one = getLocation().distance(netSegment.one);
        double two = getLocation().distance(netSegment.two);
        return one < two ? netSegment.two : netSegment.one;
    }

    public AIUnit nearestUnit(Collection<AIUnit> otherUnits) {
        return Collections.min(otherUnits, new DistanceComparator(getLocation()));
    }

    public static AIUnit farthestUnit(AIPoint source, Collection<AIUnit> units) {
        return Collections.max(units, new DistanceComparator(source));
    }

    public static AIUnit nearestUnit(AIPoint source, Collection<AIUnit> units) {
        return Collections.min(units, new DistanceComparator(source));
    }

    double getSpeedAngle() {
        return AI.orientAngle(getSpeed());
    }

    public AIPoint getNextLocation() {
        return AIPoint.sum(speed, location);
    }



    public AIPoint predictLocationAfter(double ticks) {
        AIPoint p = new AIPoint(getSpeed());
        p.scale(ticks/2);
        return AIPoint.sum(p, getLocation());
    }

    abstract double getRadius();


    // will use it to run away from it
    // will probably change something in AIHockeyist to support shift ticks
    public LocationTicks predictCollision(AIUnit unit) {
        AIPoint c_0 = this.getLocation();
        AIPoint c_1 = unit.getLocation();
        AIPoint v_0 = this.getSpeed();
        AIPoint v_1 = unit.getSpeed();
        double r_0 = this.getRadius();
        double r_1 = unit.getRadius();

        AIPoint p_d = AIPoint.difference(c_0, c_1);
        AIPoint v_d = AIPoint.difference(v_0, v_1);
        double pp = AIPoint.dotProduct(p_d, p_d);
        double pv = 2*AIPoint.dotProduct(p_d, v_d);
        double vv = AIPoint.dotProduct(v_d, v_d);
        double rr = Math.pow(r_0 + r_1, 2);
        double dd = pv*pv - 4*(pp-rr)*vv;
        if (dd < 0) return null;
        dd = Math.sqrt(dd);
        if (vv == 0) return null;
        double ticks = Math.min((-pv - dd)/(2*vv), (-pv + dd)/(2*vv));
        if (Double.isNaN(ticks)) {
            throw new RuntimeException();
        }
        if (ticks < 0) return null;
        return new LocationTicks(predictLocationAfter(ticks), ticks);
    }

    public LocationTicks predictRinkCollision() {
        AILine line = new AILine(getLocation(), getSpeedAngle());
        AIPoint loc = getLocation();
        AIManager manager = AIManager.getInstance();
        AIRectangle rink = manager.getRink();

        if (getSpeedScalar() < AI.COMPUTATION_BIAS) return null;
        for (AILine border : manager.getRinkBorders()) {

            AIPoint p = border.intersection(line);
            double d = p.distance(loc);
            double before = d - getRadius();
            double after = getRadius();
            AIPoint p_before = new AIPoint(loc);
            p_before.scale(after / d);
            AIPoint p_after = new AIPoint(p);
            p_after.scale(before / d);
            AIPoint want = AIPoint.sum(p_before, p_after);
            if (rink.isInside(want) && AI.angle(getSpeed(), AIPoint.difference(want, loc)) < PI / 2) {
                double ticks = distanceTo(want)/getSpeedScalar();
                return new LocationTicks(want, ticks);
            } else {
                continue;
            }
        }

        return null;
    }

    public LocationTicks predictNextCollision() {
        AIManager manager = AIManager.getInstance();
        LocationTicks minLt = null;
        double speed = getSpeedScalar();
        for (AIUnit u : manager.getPlayers()) {
            if (this == u) continue;
            LocationTicks lt = predictCollision(u);
            if (lt == null) continue;
            if (minLt == null || lt.ticks < minLt.ticks) {
                minLt = lt;
            }
        }
        LocationTicks lt = predictRinkCollision();
        if (lt != null) {
            if (minLt == null ||  lt.ticks < minLt.ticks) {
                minLt = lt;
            }
        }
        return minLt;
    }

    public boolean isMovingAwayFrom(AIPoint target) {
        AIPoint nextLocation = getNextLocation();
        return distanceTo(target) < nextLocation.distance(target) - AI.COMPUTATION_BIAS;
    }


    public static class LocationTicks {
        AIPoint location;
        double ticks;

        public LocationTicks(AIPoint location, double ticks) {
            this.location = location;
            this.ticks = ticks;
        }
    }

    private static class DistanceComparator implements Comparator<AIUnit> {
        AIPoint origin;

        DistanceComparator(AIPoint origin) {
            this.origin = origin;
        }

        @Override
        public int compare(AIUnit o1, AIUnit o2) {
            return (int)signum(o1.distanceTo(origin) - o2.distanceTo(origin));
        }
    }
}
