
import model.Unit;

import java.awt.*;

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
    private double radius;

    protected AIUnit(double radius) {
        this.radius = radius;
    }
    protected AIUnit(Unit unit) {
        setLocation(unit.getX(), unit.getY());
        setSpeed(unit.getSpeedX(), unit.getSpeedY());
        setAngle(unit.getAngle());
        radius = unit.getRadius();
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

    double orientAngle(AIPoint point) {
        double d = angleTo(point);
        return AI.orientAngle(getAngle() + d);
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

    public double getRadius() {
        return radius;
    }

    public double getX() {
        return location.x;
    }

    public double getY() {
        return location.y;
    }
}
