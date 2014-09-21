import model.ActionType;
import model.Hockeyist;

import static java.lang.StrictMath.*;
import static java.lang.StrictMath.cos;

/**
 * Created by antoshkaplus on 9/20/14.
 */
public class AIHockeyist extends AIUnit {

    public static final double RADIUS = 30;
    public static double PUCK_BINDING_RANGE = 55;

    private static final double MAX_TURN_ANGLE_PER_TICK = 3 * PI / 180;

    private static final double ACCESS_DISTANCE = 120;
    private static final double ACCESS_ANGLE_BIAS = PI/12;

    private static final double PUCK_ANGLE_STANDARD_DEVIATION = 2 * AI.DEGREE;
    // how much can turn for the pass
    private static final double PASS_ANGLE_BIAS = PI/3;


    private static final double SPEED_UP_FACTOR = 0.116;
    private static final double SPEED_DOWN_FACTOR = 0.069;


    private long id;
    private boolean teammate;
    private ActionType lastAction;
    private int lastActionTick;
    private int swingTicks;


    AIHockeyist(Hockeyist hockeyist) {
        super(hockeyist);
        id = hockeyist.getId();
        teammate = hockeyist.isTeammate();
        lastAction = hockeyist.getLastAction();
        Integer lastActionTick = hockeyist.getLastActionTick();
        this.lastActionTick = lastActionTick == null ? -1 : lastActionTick;
        swingTicks = hockeyist.getSwingTicks();
    }

    public boolean isTeammate() {
        return teammate;
    }

    public long getId() {
        return id;
    }

    public boolean isInStickRange(AIUnit unit) {
        return distanceTo(unit) < ACCESS_DISTANCE && abs(angleTo(unit)) < ACCESS_ANGLE_BIAS;
    }

    public ActionType getLastAction() {
        return lastAction;
    }

    public Integer getLastActionTick() {
        return lastActionTick;
    }

    public double getPuckAngleDeviation() {
        return PUCK_ANGLE_STANDARD_DEVIATION;
    }

    // if he would have puck in hands where would it be
    public AIPoint getPuckLocation() {
        AIPoint p = AI.unit(getAngle());
        p.scale(PUCK_BINDING_RANGE);
        p.translate(getLocation());
        return p;
    }

    double getStrikePuckSpeed() {
        int ticks = 0;
        if (getLastAction() == ActionType.SWING) {
            // only 20 effective ticks
            ticks = min(20, swingTicks);
        }
        double speed = AIPoint.ZERO.distance(getSpeed());
        return 20*(0.75 + 0.25*ticks/20) + speed*cos(getAngle() - AI.orientAngle(getSpeed()));
    }

    double getPassPuckSpeed(double passPower, double passAngle) {
        return 15 * passPower + getSpeedScalar() *
                cos(getAngle() + passAngle - AI.orientAngle(getSpeed()));
    }

    // this will change by formula
    boolean isInPassRange(double angle) {
        double a = getAngle();
        return AI.isAngleBetween(
                AI.orientAngle(a - PASS_ANGLE_BIAS),
                AI.orientAngle(a + PASS_ANGLE_BIAS),
                angle);
    }

    double getMaxSpeedUp() {
        return SPEED_UP_FACTOR;
    }

    double getMaxSlowDown() {
        return SPEED_DOWN_FACTOR;
    }

    // [-1, 1]
    AIPoint getNextLocation(double speedUp) {
        AIPoint acceleration = AI.unit(getAngle());
        acceleration.scale(speedUp * (speedUp < 0 ? getMaxSlowDown() : getMaxSpeedUp()));
        return AIPoint.sum(getLocation(), AIPoint.sum(getSpeed(), acceleration));
    }

    boolean isPassAngle(double passAngle) {
        return AI.isValueBetween(-PASS_ANGLE_BIAS, passAngle, PASS_ANGLE_BIAS);
    }
}

