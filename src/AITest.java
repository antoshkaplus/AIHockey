import model.ActionType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.StrictMath.abs;

/**
 * Created by antoshkaplus on 9/21/14.
 */
public class AITest implements AIRole {

    public static final int HOCKEYIST_FRICTION = 1;
    public static final int PUCK_FRICTION = 2;

    private int test = 0;
    private boolean visitedOrigin = false;

    private BufferedWriter writer;

    AITest() {
        try {
            writer = new BufferedWriter(
                    new FileWriter("test_data.txt", false));
        } catch (IOException e) {
            System.out.println("I hate exceptions");
        }
    }


    public void setTest(int test) {
        visitedOrigin = false;
        this.test = test;
    }

    @Override
    public AIMove move() {
        AIHockeyist hockeyist = null;
        switch (test) {
            case HOCKEYIST_FRICTION:
                return onTestHockeyistFriction(hockeyist);
            case PUCK_FRICTION:
                return onTestPuckFriction(hockeyist);
            default:
                return new AIMove();
        }
    }


    AIMove onTestHockeyistFriction(AIHockeyist hockeyist) {
        AIManager manager = AIManager.getInstance();
        AIRectangle rink = manager.getRink();
        AIPoint center = manager.getCenter();

        AIMove move = new AIMove();
        if (!visitedOrigin) {
            double d = hockeyist.angleTo(rink.origin);
            move.setTurn(d);
            move.setSpeedUp(1);
            if (hockeyist.distanceTo(rink.origin) < 2*AIHockeyist.RADIUS) {
                move.setSpeedUp(0);
                if (hockeyist.getSpeedScalar() < 0.1) {
                    move.setTurn(hockeyist.angleTo(rink.getRight(), hockeyist.getY()));
                    if (hockeyist.angleTo(rink.getRight(), hockeyist.getY()) < AI.DEGREE) {
                        visitedOrigin = true;
                    }
                }
            }
        } else {
            move.setSpeedUp(1);
        }
        if (visitedOrigin && hockeyist.getX() > center.x) {
            move.setSpeedUp(0);
            if (hockeyist.getSpeedScalar() > 0.005) {
                System.out.println(String.format("%.2f,%.2f;", hockeyist.getSpeedScalar(), hockeyist.distanceTo(center.x, hockeyist.getY())));
            }
        }
        return move;
    }


    AIMove onTestPuckFriction(AIHockeyist hockeyist) {
        AIManager manager = AIManager.getInstance();
        AIRectangle rink = manager.getRink();
        int currentTick = manager.getCurrentTick();
        AIPuck puck = manager.getPuck();

        AIMove move = new AIMove();
        if (manager.isPuckOwner(hockeyist)) {
            if (hockeyist.distanceTo(rink.origin) < 3 * AIHockeyist.RADIUS) {
                if (hockeyist.getSpeedScalar() < 0.04) {
                    AIPoint target = new AIPoint(rink.getRight(), hockeyist.getY());
                    // AIPoint.sum(rink.origin, rink.size)
                    double angle = hockeyist.angleTo(target);
                    if (abs(angle) < 0.01) {
                        //move.setAction(ActionType.STRIKE);
                        if (hockeyist.getLastAction() == ActionType.SWING &&
                                currentTick - hockeyist.getLastActionTick() > 11) {
                            move.setAction(ActionType.STRIKE);
                        } else {
                            move.setAction(ActionType.SWING);
                        }
                    } else {
                        move.setTurn(angle);
                    }
                } else {
                    move.setSpeedUp(0);
                }
            } else {
                move = AIGo.to(hockeyist, rink.origin);
            }
        } else {
            if (puck.getSpeedScalar() > 1) return new AIMove();
            move = AIGo.to(hockeyist, puck.getLocation());
            if (hockeyist.isInStickRange(puck)) {
                move.setAction(ActionType.TAKE_PUCK);
            }
        }
        return move;
    }


    AIPoint startPoint = null;

    AIMove onTestAcceleration(AIHockeyist hockeyist) {
        AIManager manager = AIManager.getInstance();
        AIPoint source = manager.getRink().getTopLeft();
        AIPoint target = manager.getRink().getBottomRight();
        AIMove move = new AIMove();

        if (hockeyist.distanceTo(target) < 2.5*hockeyist.RADIUS) {
            visitedOrigin = false;
            return new AIMove();
        }

        if (!visitedOrigin) {
            if (hockeyist.distanceTo(source) < 2.5 * hockeyist.RADIUS) {
                    if (hockeyist.getSpeedScalar() < 0.1) {
                        if (hockeyist.angleTo(target) < AI.DEGREE) {
                            visitedOrigin = true;
                            startPoint = new AIPoint(hockeyist.getLocation());
                        } else {
                            move.setTurn(hockeyist.angleTo(target));
                        }
                    } else {
                        return move;
                    }
            } else {
                move = AIGo.to(hockeyist, source);
            }
        } else {
            try {
                writer.write(String.format("%.2f,%.2f;",
                        hockeyist.getSpeedScalar(),
                        startPoint.distance(hockeyist.getLocation())));
            } catch (IOException e) {
                System.out.println("I hate exceptions!");
            }
            move.setSpeedUp(1);
        }
        return move;
    }
}
