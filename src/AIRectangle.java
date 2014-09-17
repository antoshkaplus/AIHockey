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
        this.origin = (AIPoint)origin.clone();
        this.size = (AIPoint)size.clone();
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
