/**
 * Created by antoshkaplus on 9/13/14.
 */
public class AIRectangle {

    AIPoint origin = new AIPoint();
    AIPoint size = new AIPoint();

    AIRectangle() {}
    AIRectangle(double x, double y, double width, double height) {
        origin.set(x, y);
        size.set(width, height);
    }
    AIRectangle(AIPoint origin, AIPoint size) {
        this.origin = new AIPoint(origin);
        this.size = new AIPoint(size);
    }

    AIRectangle(AIRectangle rect) {
        origin = new AIPoint(rect.origin);
        size = new AIPoint(rect.size);
    }

    boolean isInside(AIPoint p) {
        return p.x >= origin.x && p.y >= origin.y &&
                p.x <= origin.x+size.x && p.y <= origin.y+size.y;
    }

    double getWidth() {
        return size.x;
    }

    double getHeight() {
        return size.y;
    }

    double getTop() {
        return origin.y;
    }

    double getBottom() {
        return origin.y + size.y;
    }
    double getLeft() {
        return origin.x;
    }
    double getRight() {
        return origin.x + size.x;
    }

    AIPoint getTopLeft() {
        return origin;
    }
    AIPoint getTopRight() {
        return new AIPoint(getRight(), getTop());
    }
    AIPoint getBottomLeft() {
        return new AIPoint(getLeft(), getBottom());
    }
    AIPoint getBottomRight() {
        return AIPoint.sum(origin, size);
    }

    static AIRectangle intersection(AIRectangle rect_0, AIRectangle rect_1) {
        AIRectangle r = new AIRectangle();
        r.origin = AIPoint.max(rect_0.origin, rect_1.origin);
        r.size.set(
                Math.min(rect_0.getRight(), rect_1.getRight()) - r.origin.x,
                Math.min(rect_0.getBottom(), rect_1.getBottom()) - r.origin.y);
        return r;
    }

//
//    struct Rectangle {
//        Rectangle() {}
//        Rectangle(const Point& origin, const Size& size)
//        : origin(origin), size(size) {}
//        Rectangle(Int x, Int y, Int width, Int height)
//        : origin(x, y), size(width, height) {}
//
//    void set(Int x, Int y, size_t width, size_t height) {
//        origin.set(x, y);
//        size.set(width, height);
//    }
//
//    void set(const Point& origin, const Point& diag) {
//        this->origin = origin;
//        size.set(diag.x - origin.x, diag.y - origin.y);
//    }
//
//    void swap() {
//        origin.swap();
//        size.swap();
//    }
//
//    size_t area() const {
//        return size.area();
//    }
//
//    size_t perimeter() const {
//        return size.perimeter();
//    }
//
//    bool isIntersect(const Rectangle& r) const {
//        return origin.x < r.origin.x + r.size.width  && origin.x + size.width  > r.origin.x &&
//                origin.y < r.origin.y + r.size.height && origin.y + size.height > r.origin.y;
//    }


}
