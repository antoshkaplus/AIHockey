/**
 * Created by antoshkaplus on 9/11/14.
 */

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.StrictMath.*;

/** a lot of global shit here
 * probably some util functions */

public class AI {
    public static final double COMPUTATION_BIAS = 1e-7;


    public static AIPoint unitVector(double angle) {
        return new AIPoint(cos(angle), sin(angle));
    }


    // if oriented orientAngle needed please use world orientedAngle

    // three point orientAngle
    // orientAngle between [0, PI]
    public static double angle(AIPoint p_one, AIPoint p_center, AIPoint p_two) {
        double a = p_one.distance(p_center);
        double b = p_two.distance(p_center);
        double c = p_one.distance(p_two);
        // 0, PI
        return Math.acos((a * a + b * b - c * c) / (2 * a * b));
    }

    // oriented orientAngle
    public static double orientAngle(AIPoint p) {
        return atan2(p.y, p.x);
    }

    /** where should turn */
    public static double orientAngle(double source, double target) {
        target -= source;
        if (target < -PI) target += 2*PI;
        if (target >  PI) target -= 2*PI;
        return target;
    }

    // orientAngle between two vectors [0, PI]
    public static double angle(AIPoint v_one, AIPoint v_two) {
        return angle(v_one, AIPoint.ZERO, v_two);
    }

    // get in some orientAngle [0 2PI] or negative
    // return orientAngle between -PI, PI
    public static double orientAngle(double angle) {
        if (angle > PI) {
            return angle - 2*PI;
        }
        if (angle < -PI) {
            return angle + 2*PI;
        }
        return angle;
    }

    public static boolean isSegmentIntersectedByRay(AILine segment, AIPoint center, double angle) {
        // orientAngle line
        AILine a = new AILine(center, angle);
        AIPoint p = segment.intersection(a);
        return isBetween(segment.one, segment.two, p);
    }

    public static boolean isBetween(AIPoint one, AIPoint two, AIPoint between) {
        double a = one.distance(two);
        double b = between.distance(one) + between.distance(two);
        return abs(a - b) < COMPUTATION_BIAS;
    }

    public static AIPoint nearestPoint(AIPoint source, Iterable<AIPoint> ps) {
        AIPoint minP = null;
        double min = Double.MAX_VALUE, maybeMin;
        for (AIPoint p : ps) {
            if ((maybeMin = p.distance(source)) < min) {
                min = maybeMin;
                minP = p;
            }
        }
        return minP;
    }

    public static boolean isValueBetween(double one, double two, double between) {
        return (one <= between && between <= two) || (one >= between && between >= two);
    }

    /** angles should be  */
    public static boolean isAngleBetween(double one, double two, double between) {
        if (!isOriented(one) || !isOriented(two) || !isOriented(between)) throw new RuntimeException("not oriented");
        double between_ = orientAngle(between - one);
        double two_ = orientAngle(two - one);
        return isValueBetween(0, two_, between_);
    }

    public static boolean isOriented(double angle) {
        return isValueBetween(-PI, PI, angle);
    }

}

