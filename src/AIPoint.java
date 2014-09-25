import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;

import static java.lang.StrictMath.*;

class AIPoint implements Cloneable {
    static final double COMPUTATION_BIAS = 1e-7;
    static final AIPoint ZERO = new AIPoint(0, 0);

    double x;
    double y;

    AIPoint() {}
    AIPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    AIPoint(AIPoint point) {
        set(point.x, point.y);
    }

    double distance(AIPoint p) {
        double dx = x - p.x;
        double dy = y - p.y;
        return sqrt(dx * dx + dy * dy);
    }

    // for vectors, not locations
    double scalar() {
        return distance(ZERO);
    }

    double distance(double x, double y) {
        return distance(new AIPoint(x, y));
    }

    void scale(double d) {
        x *= d;
        y *= d;
    }

    void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }
    void translate(AIPoint p) {
        x += p.x;
        y += p.y;
    }

    void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    void set(AIPoint p) {
        x = p.x;
        y = p.y;
    }

    static AIPoint unit(AIPoint p) {
        double d = p.scalar();
        return new AIPoint(p.x/d, p.y/d);
    }
    static double dotProduct(AIPoint p_0, AIPoint p_1) {
        return p_0.x*p_1.x + p_0.y*p_1.y;
    }
    static AIPoint difference(AIPoint p_0, AIPoint p_1) {
        return new AIPoint(p_0.x - p_1.x, p_0.y - p_1.y);
    }
    static AIPoint sum(AIPoint p_0, AIPoint p_1) {
        return new AIPoint(p_0.x + p_1.x, p_0.y + p_1.y);
    }
    static AIPoint max(AIPoint p_0, AIPoint p_1) {
        return new AIPoint(Math.max(p_0.x, p_1.x), Math.max(p_0.y, p_1.y));
    }
    static AIPoint min(AIPoint p_0, AIPoint p_1) {
        return new AIPoint(Math.min(p_0.x, p_1.x), Math.min(p_0.y, p_1.y));
    }
    static AIPoint middle(AIPoint p_0, AIPoint p_1) {
        AIPoint mid = AIPoint.sum(p_0, p_1);
        mid.scale(0.5);
        return mid;
    }

    @Override
    public String toString() {
        return String.format("AIPoint: (%.3f, %.3f)", x, y);
    }

    public AIPoint nearestPoint(Collection<AIPoint> ps) {
        return Collections.min(ps, new DistanceComparator(this));
    }

    static class DistanceComparator implements Comparator<AIPoint> {
        AIPoint origin;

        DistanceComparator(AIPoint origin) {
            this.origin = origin;
        }

        @Override
        public int compare(AIPoint o1, AIPoint o2) {
            return (int)signum(origin.distance(o1) - origin.distance(o2));
        }
    }

}

