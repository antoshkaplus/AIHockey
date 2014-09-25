import model.Puck;

/**
 * Created by antoshkaplus on 9/11/14.
 */
public class AIPuck extends AIUnit {

    public static final double RADIUS = 20;
    public boolean hasOwner = false;

    AIPuck(Puck puck) {
        super(puck);
        hasOwner = puck.getOwnerPlayerId() != -1;
    }

    @Override
    public double getRadius() {
        return RADIUS;
    }

    @Override
    public AIPoint predictLocationAfter(double ticks) {
        if (hasOwner) {
            AIManager manager = AIManager.getInstance();
            AIHockeyist h = manager.getPuckOwner();
            AIPoint oldLocation = h.getLocation();
            h.setLocation(h.predictLocationAfter(ticks));
            AIPoint r = h.getPuckLocation();
            h.setLocation(oldLocation);
            return r;
        }
        return super.predictLocationAfter(ticks);
    }

    @Override
    public LocationTicks predictCollision(AIUnit unit) {
        if (hasOwner) {
            AIManager manager = AIManager.getInstance();
            return manager.getPuckOwner().predictCollision(unit);
        }
        return super.predictCollision(unit);
    }

    @Override
    public LocationTicks predictNextCollision() {
        if (hasOwner) {
            AIManager manager = AIManager.getInstance();
            return manager.getPuckOwner().predictNextCollision();
        }
        return super.predictNextCollision();
    }

    @Override
    public LocationTicks predictRinkCollision() {
        if (hasOwner) {
            AIManager manager = AIManager.getInstance();
            return manager.getPuckOwner().predictRinkCollision();
        }
        return super.predictRinkCollision();
    }

    @Override
    public double getAngle() {
        throw new RuntimeException("fuck you");
    }
}
