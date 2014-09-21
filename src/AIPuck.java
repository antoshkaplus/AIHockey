import model.Puck;

/**
 * Created by antoshkaplus on 9/11/14.
 */
public class AIPuck extends AIUnit {

    public static final double RADIUS = 20;

    AIPuck(Puck puck) {
        super(puck);
    }


    @Override
    public double getAngle() {
        throw new RuntimeException("fuck you");
    }
}
