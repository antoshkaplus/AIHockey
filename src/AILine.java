
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.sqrt;


/**
 * Created by antoshkaplus on 9/11/14.
 */

class AILine implements Cloneable {
    AIPoint one;
    AIPoint two;

    AILine() {
        one = new AIPoint();
        two = new AIPoint();
    }
    AILine(AIPoint one, AIPoint two) {
        this.one = one;
        this.two = two;
    }
    AILine(AIPoint center, double angle) {
        one = center;
        two = AIPoint.sum(center, AI.unit(angle));
    }
    AILine(double x_0, double y_0, double x_1, double y_1) {
        one = new AIPoint(x_0, y_0);
        two = new AIPoint(x_1, y_1);
    }

    void translate(double dx, double dy) {
        one.translate(dx, dy);
        two.translate(dx, dy);
    }

    AIPoint middle() {
        AIPoint mid = AIPoint.sum(one, two);
        mid.scale(0.5);
        return mid;
    }


    @Override
    protected Object clone() {
        AILine line = new AILine(new AIPoint(one), new AIPoint(two));
        return line;
    }

    private double determinant() {
        return one.x*two.y - one.y*two.x;
    }

    double fromPointDistance(AIPoint point) {
        AIPoint d = AIPoint.difference(two, one);
        AIPoint p = AIPoint.difference(one, point);
        return abs(d.x * p.y - d.y * p.x)/sqrt(AIPoint.dotProduct(d, d));
    }

    AIPoint intersection(AILine line) {
        double a = determinant();
        double b = line.determinant();
        AIPoint p_a = AIPoint.difference(one, two);
        AIPoint p_b = AIPoint.difference(line.one, line.two);
        double c = p_a.x * p_b.y - p_b.x * p_a.y;
        p_b.scale(a);
        p_a.scale(b);
        AIPoint res = AIPoint.difference(p_b, p_a);
        res.scale(1./c);
        return res;
    }

    AIPoint nearestPoint(AIPoint point) {
        double one = this.one.distance(point);
        double two = this.two.distance(point);
        return one < two ? this.one : this.two;
    }

    AIPoint farthestPoint(AIPoint point) {
        double one = this.one.distance(point);
        double two = this.two.distance(point);
        return one > two ? this.one : this.two;
    }

    AIPoint otherPoint(AIPoint point) {
        return one == point ? two : one;
    }

//    AILine perpendicularLine(AILine originalLine, AIPoint throughPoint) {
//         AIPoint.difference(originalLine.one, originalLine.two);
//    }

}