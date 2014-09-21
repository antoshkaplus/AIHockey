
import model.Unit;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/11/14.
 */
public class AIUnit {
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
        this.location = location;
    }
    public void setLocation(double x, double y) { this.location.set(x, y); }
    public AIPoint getLocation() {
        return location;
    }

    public void setSpeed(AIPoint speed) {
        this.speed = speed;
    }
    public void setSpeed(double x, double y) { this.speed = new AIPoint(x, y); }
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

    public static AIUnit farthestUnit(AIPoint source, Iterable<AIUnit> units) {
        double minDistance = Double.MIN_VALUE;
        double distance;
        AIUnit minUnit = null;
        for (AIUnit u : units) {
            if ((distance = u.distanceTo(source)) > minDistance) {
                minUnit = u;
                minDistance = distance;
            }
        }
        return minUnit;
    }

    static class DistanceComparator implements Comparator<AIUnit> {
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
