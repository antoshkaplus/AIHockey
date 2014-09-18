
import static java.lang.Math.*;
import static java.lang.Math.abs;
import static java.lang.StrictMath.sqrt;

/**
 * Created by antoshkaplus on 9/18/14.
 */
public final class AIGo {

    public static AIMove to(AIManager.AIHockeyist hockeyist, AIPoint target) {

    }

    public static AIPoint projectionOnSpeed(AIManager.AIHockeyist hockeyist, AIPoint target) {
        AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
        // projection target vector on speed vector
        AIPoint projection = AI.projection(targetVector, hockeyist.getSpeed());
        return projection;
    }

    public static double projectionOnSpeedScalar(AIManager.AIHockeyist hockeyist, AIPoint target) {
        AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
        // projection target vector on speed vector
        if (hockeyist.getSpeedScalar() < AI.COMPUTATION_BIAS) {
            return 0;
        }
        return AI.projectionScalar(targetVector, hockeyist.getSpeed());
    }

    // both values should be positive
    public static void adjustSpeedUp(double needSpeed, double haveSpeed, AIMove move) {
        double diff = needSpeed - haveSpeed;
        if (diff < 0) {
            // i'm too fast
            move.setSpeedUp(max(diff, -1));
        } else {
            move.setSpeedUp(min(diff, 1));
        }
        if (abs(move.getSpeedUp() - 1) > 1e-7) {
            int i = 0;
            ++i;
        }
    }


    public static AIMove goTo(AIManager.AIHockeyist hockeyist, AIPoint target) {
        AIMove move = new AIMove();
        // bad situation if speed equals to zero or we are in place

        AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
        if (AI.vectorLength(targetVector) < AI.COMPUTATION_BIAS) {
            return move;
        }
        if (hockeyist.getSpeedScalar() < AI.COMPUTATION_BIAS) {
            move.setSpeedUp(1);
            return move;
        }
        double turnAngle = hockeyist.angleTo(target);
        // don't need to consider if adjustSpeedUp gets negative values
        if (abs(turnAngle) > PI/2) {
            move.setSpeedUp(-1);
            return move;
        }




        double speedTargetAngle = AI.angle(hockeyist.getSpeed(), targetVector);

        // now we will work with projections
        AIFriction friction = AIFriction.getInstance();
        AIPoint hockeyistNextLocation = hockeyist.getNextLocation();
        AIPoint hockeyistNextVector = AIPoint.difference(target, hockeyistNextLocation);

        // how much time will need to turn ?
        // can use future hockeyist location
        double ticks = abs(turnAngle)/ AIManager.MAX_TURN_ANGLE_PER_TICK;
        AIFriction.DistanceSpeed distanceSpeed = friction.hockeyistAfterTicks(
                    ticks, hockeyist.getSpeedScalar());
        // how much will travel during ticks
        double distance = distanceSpeed.distance;
        // negative value is unacceptable
        if (distance < 0) throw new RuntimeException();
        // this can be negative
        double projectionValue = abs(projectionOnSpeedScalar(hockeyist, target));
        adjustSpeedUp(projectionValue, distance, move);
        // turn for future position

        move.setTurn(turnAngle);
        return move;
    }



    public static AIMove goToStop(AIManager.AIHockeyist hockeyist, AIPoint location) {
        AIMove move = new AIMove();
        AIFriction friction = AIFriction.getInstance();
        AIFriction.DistanceTicks distanceTicks = friction.hockeyistStopDistance(hockeyist.getSpeedScalar());
        double projectionValue = projectionOnSpeedScalar(hockeyist, location);
        adjustSpeedUp(projectionValue, distanceTicks.distance, move);
        move.setTurn(hockeyist.angleTo(location));
        return move;
    }


    // orientAngle should be oriented
    private AIMove moveTo(AIManager.AIHockeyist hockeyist, AIPoint location, double angle) {
        AIMove move = new AIMove();
        // this is also a constant that should depend on distance
        AIPoint v = AIPoint.difference(location, hockeyist.getLocation());
        if (AI.angle(hockeyist.getSpeed(), v) < 0.04) {
            double curAngle = hockeyist.getAngle();
            double turnTickCount = StrictMath.abs(AI.orientAngle(curAngle, angle))/AIManager.MAX_TURN_ANGLE_PER_TICK;
            double d = AIPoint.ZERO.distance(v);
            double tickCount = 0;// hockeyistTime(d, hockeyist.getSpeedScalar());
            if (turnTickCount + 10 < tickCount) {
                move.setSpeedUp(1);
            } else if (turnTickCount > tickCount + 10) {
                move.setSpeedUp(-1);
            } else {
                move.setSpeedUp(0);
            }
            move.setTurn(AI.orientAngle(hockeyist.getAngle(), angle));
        } else {
            move = moveTo(hockeyist, location);
        }
        return move;
    }

    private AIMove moveTo(AIManager.AIHockeyist hockeyist, AIPoint location) {
        AIMove move = new AIMove();
        double orientAngle = hockeyist.angleTo(location);
        AIPoint acceleration;
        acceleration = AI.unitVector(hockeyist.getAngle());
        acceleration.scale(AIManager.SPEED_UP_FACTOR);
        AIPoint positive = AIPoint.sum(hockeyist.getLocation(), AIPoint.sum(hockeyist.getSpeed(), acceleration));
        acceleration = AI.unitVector(hockeyist.getAngle());
        acceleration.scale(-AIManager.SPEED_DOWN_FACTOR);
        AIPoint negative = AIPoint.sum(hockeyist.getLocation(), AIPoint.sum(hockeyist.getSpeed(), acceleration));

        move.setSpeedUp( positive.distance(location) < negative.distance(location) ? 1 : -1);
        double angle = StrictMath.abs(orientAngle);
        // first move then turn...

        move.setTurn(orientAngle);
        return move;
    }

    private AIMove moveToStop(AIManager.AIHockeyist hockeyist, AIPoint location) {
        AIPoint hockLocation = hockeyist.getLocation();
        double d = hockeyist.distanceTo(location);
        double v = sqrt(2*d*AIManager.SPEED_DOWN_FACTOR);
        if (v < hockeyist.getSpeedScalar() || hockeyist.distanceTo(location) <= AIManager.HOCKEYIST_RADIUS+2) {
            AIMove move = new AIMove();
            // should get v as soon as possible
            AIPoint speed = hockeyist.getSpeed();
            AIPoint positive = AI.unitVector(hockeyist.getAngle());
            positive.scale(AIManager.SPEED_UP_FACTOR);
            AIPoint negative = AI.unitVector(hockeyist.getAngle());
            negative.scale(-AIManager.SPEED_DOWN_FACTOR);
            if (AIPoint.sum(speed, positive).scalar() < AIPoint.sum(speed, negative).scalar()) {
                move.setSpeedUp(AIManager.SPEED_UP_FACTOR);
            } else {
                move.setSpeedUp(-AIManager.SPEED_DOWN_FACTOR);
            }
            move.setTurn(hockeyist.angleTo(location));
            //System.out.println(hockeyist.getSpeedScalar() + " " + v +  " " + d);
            return move;
        } else {
            return moveTo(hockeyist, location);
        }
    }

    private AIMove moveToStop(AIManager.AIHockeyist hockeyist, AIPoint location, double angle) {
        AIMove move = new AIMove();
        if (hockeyist.getSpeedScalar() < 1 && hockeyist.distanceTo(location) <= AIManager.HOCKEYIST_RADIUS+2) {
            move.setTurn(AI.orientAngle(hockeyist.getAngle(), angle));
        } else {
            move = moveToStop(hockeyist, location);
        }
        return move;
    }


}
