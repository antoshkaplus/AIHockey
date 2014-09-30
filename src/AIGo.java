
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;
import static java.lang.Math.abs;
import static java.lang.StrictMath.sqrt;

/**
 * Created by antoshkaplus on 9/18/14.
 *
 *  don't think we will use use go to with angle procedure.
 *  usually it's important to strike after you are there
 *  but implementation is really complicated
 *
 *  probably every role should have instance of this shit
 */
public final class AIGo {
    // default one is best acceleration
    public enum GoType {
        BEST_ACCELERATION,
        MAX_ACCELERATION
    }

    private static final double DEFAULT_TARGET_RADIUS = 15;


    public static AIMove pursue(AIHockeyist hockeyist, AIUnit unit) {
        double ticks = hockeyist.ticksAheadTo(unit.getLocation());
        AIUnit.LocationTicks lt = unit.predictNextCollision();
        double ticksCollision = 1000000;
        if (lt != null) ticksCollision = lt.ticks;
        return toStop(hockeyist, unit.predictLocationAfter(min(ticksCollision, ticks)));
    }

    public static AIMove toAvoid(AIHockeyist hockeyist, AIPoint target) {
        AIManager manager = AIManager.getInstance();
        AIRectangle rink = manager.getRink();
        double maxDistance = 130;

        AIPuck puck = manager.getPuck();

        List<AIHockeyist> opponents = new ArrayList<AIHockeyist>();
        for (AIHockeyist opp : manager.getOpponents()) {
            if (puck.distanceTo(opp) < maxDistance) {
                opponents.add(opp);
            }
        }

        if (opponents.isEmpty()) {
            return to(hockeyist, target);
        }

        // greater score is better, how far puck we from both hockeyists
        double bestScore = 0;
        // from -3 to 3
        double bestTurn = 0;
        // from -1 to 1
        double bestAcceleration = 0;

        for (double acceleration : new double[]{-1, 0, 1}) {
            for (double turn : new double[]{-hockeyist.getMaxTurnPerTick(), 0, hockeyist.getMaxTurnPerTick()}) {
                AIHockeyist next = AIHockeyist.hockeyistNext(hockeyist, acceleration, turn);
                AIPoint p = next.getPuckLocation();

                if (manager.getRink().distanceToInsidePoint(p) < manager.getRink().distanceToInsidePoint(hockeyist.getPuckLocation()) && manager.getRink().distanceToInsidePoint(p) < AIHockeyist.RADIUS) continue;

                double score = 0;
                for (AIHockeyist opp : opponents) {
                    score += pow(p.distance(opp.getLocation()), 2);
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestTurn = turn;
                    bestAcceleration = acceleration;
                }
            }
        }

        AIMove m = new AIMove();
        m.setTurn(bestTurn);
        m.setSpeedUp(bestAcceleration);
        return m;
    }

    public static AIMove toAvoid2(AIHockeyist hockeyist, AIPoint target) {
        AIManager manager = AIManager.getInstance();
        AIRectangle rink = manager.getRink();
        double maxCurrentDistance = 300;
        // location of opponent in collision moment
        AIPoint opponentCollisionLocation = null;
        AIHockeyist opponent = null;
        double collisionTicks = Double.MAX_VALUE;
        for (AIHockeyist opp : manager.getOpponents()) {
            AIUnit.LocationTicks lt = opp.predictCollision(hockeyist);
            if (lt == null) continue;
            if (lt.ticks < collisionTicks && opp.distanceTo(hockeyist) < maxCurrentDistance) {
                collisionTicks = lt.ticks;
                opponentCollisionLocation = lt.location;
                opponent = opp;
            }
        }
        if (opponentCollisionLocation == null) return to(hockeyist, target);


        AIPoint v = AIPoint.difference(opponentCollisionLocation, hockeyist.getLocation());
        // perpendicular guys
        double scaleFactor = 2*AIHockeyist.RADIUS / v.scalar();
        AIPoint w_0 = new AIPoint(v.y, -v.x);
        w_0.scale(scaleFactor);
        w_0.translate(opponentCollisionLocation);
        AIPoint w_1 = new AIPoint(-v.y, v.x);
        w_1.scale(scaleFactor);
        w_1.translate(opponentCollisionLocation);
        if (rink.distanceToInsidePoint(w_0) < AIHockeyist.RADIUS &&
                rink.distanceToInsidePoint(w_1) > AIHockeyist.RADIUS) return to(hockeyist, w_1);
        if (rink.distanceToInsidePoint(w_1) < AIHockeyist.RADIUS &&
                rink.distanceToInsidePoint(w_0) > AIHockeyist.RADIUS) return to(hockeyist, w_0);
        if (hockeyist.distanceTo(w_0) < hockeyist.distanceTo(w_1)) {
            return to(hockeyist, w_0);
        } else {
            return to(hockeyist, w_1);
        }


    }



    public static AIMove to(AIHockeyist hockeyist, AIPoint target, GoType type) {
        switch (type) {
            case BEST_ACCELERATION:
                return BestAcceleration.to(hockeyist, target, DEFAULT_TARGET_RADIUS);
            case MAX_ACCELERATION:
                return MaxAcceleration.to(hockeyist, target);
            default: throw new RuntimeException();
        }
    }


    public static AIMove to(AIHockeyist hockeyist, AIPoint target) {
        AIManager manager = AIManager.getInstance();
        if (manager.getCurrentStrategy() instanceof AIMyPuckStrategy) {
            return MaxAcceleration.to(hockeyist, target);
        } else {
            return BestAcceleration.to(hockeyist, target, DEFAULT_TARGET_RADIUS);
        }
    }

    // considering that unit can move
    public static AIMove to(AIHockeyist hockeyist, AIUnit unit) {
        double ticks = hockeyist.ticksAheadTo(unit.getLocation());
        AIUnit.LocationTicks lt = unit.predictNextCollision();
        double ticksCollision = 1000000;
        if (lt != null) ticksCollision = lt.ticks;
        return to(hockeyist, unit.predictLocationAfter(min(ticksCollision, ticks)));
    }

    public static AIMove toStop(AIHockeyist hockeyist, AIPoint target) {
        return MaxAcceleration.toStop(hockeyist, target);
    }


    private static class MaxAcceleration {
        // choosing best acceleration to get speed for target
        public static AIMove to(AIHockeyist hockeyist, AIPoint target) {
            AIMove move = new AIMove();
            AIPoint positive = hockeyist.getNextLocation(1);
            AIPoint negative = hockeyist.getNextLocation(-1);
            AIPoint oldLocation = hockeyist.getLocation();
            if (positive.distance(target) < negative.distance(target)) {
                hockeyist.setLocation(positive);
                move.setTurn(hockeyist.angleTo(target));
                move.setSpeedUp(1);
            } else {
                hockeyist.setLocation(negative);
                move.setTurn(hockeyist.angleTo(target));
                move.setSpeedUp(-1);
            }
            hockeyist.setLocation(oldLocation);
            return move;
        }

        private static AIMove toStop(AIHockeyist hockeyist, AIPoint target) {
            AIPoint positive = hockeyist.getNextLocation(1);
            AIPoint negative = hockeyist.getNextLocation(-1);
            double distance = hockeyist.distanceTo(target);
            double speed = hockeyist.getSpeedScalar();
            AIFriction friction = AIFriction.getInstance();
            if (friction.hockeyistStopDistance(speed).distance - 3*AIHockeyist.RADIUS > distance) {
                // choosing one that decreases speed
                AIMove move = new AIMove();
                AIHockeyist h = new AIHockeyist(hockeyist);

                if (hockeyist.getNextSpeed(1).scalar() <
                        hockeyist.getNextSpeed(-1).scalar()) {
                    move.setSpeedUp(1);
                    h.setLocation(h.getNextLocation(1));
                } else {
                    move.setSpeedUp(-1);
                    h.setLocation(h.getNextLocation(-1));
                }
                move.setTurn(h.angleTo(target));
                return move;
            } else {
                return to(hockeyist, target);
            }
        }
    }

    // validity is really difficult to work with,
    // but hockeyist moves really effectively
    // like he don't break perpendiculars if needed
    // should work more about this one
    // work with some possible deviation between need and have projection
    private static class BestAcceleration {
        private static final AIFriction friction = AIFriction.getInstance();
        private static final int ITERATION_COUNT = 20;

        private static boolean isBestValid(AIPoint speed, AIPoint acceleration, AIPoint targetVector, double deviation) {
            AIPoint futureSpeed = AIPoint.sum(speed, acceleration);
            double d = AI.projectionScalar(targetVector, futureSpeed);
            AILine line = new AILine(AIPoint.ZERO, futureSpeed);
            if (line.fromPointDistance(targetVector) < deviation) {
                return true;
            }
            if (d < 0) {
                return true;
            }
            AIFriction.DistanceTicks dt = friction.hockeyistStopDistance(futureSpeed.scalar());
            /* projection on speed vector should be negative */
            double projection = AI.projectionScalar(acceleration, futureSpeed);
            if (d - dt.distance + targetVector.scalar() > 0 || projection < 0) {
                return true;
            }
            return false;
        }

        // deviation in units NOT degrees
        public static AIMove to(AIHockeyist hockeyist, AIPoint target, double deviation) {
            AIMove move = new AIMove();
            double hockeyistAngle = hockeyist.getAngle();

            AIPoint accelerationOne = AI.unit(hockeyistAngle);
            accelerationOne.scale(hockeyist.getMaxSpeedUp());
            AIPoint accelerationTwo = AI.unit(hockeyistAngle);
            accelerationTwo.scale(-hockeyist.getMaxSlowDown());
            AIPoint accelerationMid = AIPoint.sum(accelerationOne, accelerationTwo);
            accelerationMid.scale(1./2);

            AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
            AIPoint bestAcceleration = null;
            AIPoint speed = hockeyist.getSpeed();

            for (int i = 0; i < ITERATION_COUNT; ++i) {
                AIPoint futureSpeedOne = AIPoint.sum(hockeyist.getSpeed(), accelerationOne);
                AIPoint futureSpeedTwo = AIPoint.sum(hockeyist.getSpeed(), accelerationTwo);
                // now lets compute who's better

                double distanceOne = AIPoint.difference(targetVector, futureSpeedOne).scalar();
                double distanceTwo = AIPoint.difference(targetVector, futureSpeedTwo).scalar();

//                AIPoint pp = futureSpeedOne.unitVector();
//                pp.scale(friction.hockeyistStopDistance(futureSpeedOne.scalar()).distance);
//                distanceOne = targetVector.distance(pp);
//                pp = futureSpeedTwo.unitVector();
//                pp.scale(friction.hockeyistStopDistance(futureSpeedTwo.scalar()).distance);
//                distanceTwo = targetVector.distance(pp);


                if (distanceOne < distanceTwo) {
                    // one is better
                    // is BANNED ?
                    if (isBestValid(speed, accelerationOne, targetVector, deviation)) {
                        bestAcceleration = accelerationOne;
                        break;
                    }
                    if (isBestValid(speed, accelerationMid, targetVector, deviation)) {
                        accelerationTwo = accelerationMid;
                    } else {
                        accelerationOne = accelerationMid;
                    }
                } else {
                    // two is better
                    if (isBestValid(speed, accelerationTwo, targetVector, deviation)) {
                        bestAcceleration = accelerationTwo;
                        break;
                    }
                    if (isBestValid(speed, accelerationMid, targetVector, deviation)) {
                        accelerationOne = accelerationMid;
                    } else {
                        accelerationTwo = accelerationMid;
                    }
                }
                accelerationMid = AIPoint.sum(accelerationOne, accelerationTwo);
                accelerationMid.scale(1./2);
            }
            if (bestAcceleration == null) {
                // take one which is valid
                if (isBestValid(speed, accelerationOne, targetVector, deviation)) {
                    bestAcceleration = accelerationOne;
                } else if (isBestValid(speed, accelerationTwo, targetVector, deviation)) {
                    bestAcceleration = accelerationTwo;
                } else {
                    bestAcceleration = new AIPoint(0, 0);
                }
            }
            // now need to manage make acceleration back to factor
            double s = AI.projectionScalar(bestAcceleration, AI.unit(hockeyistAngle));
            double a = 0;
            if (s > 0) {
                a = s/hockeyist.getMaxSpeedUp();
            } else {
                a = s/hockeyist.getMaxSlowDown();
            }
            move.setSpeedUp(a);
            AIPoint futureSpeed = AIPoint.sum(hockeyist.getSpeed(), bestAcceleration);
            AIPoint p = hockeyist.getLocation();
            hockeyist.setLocation(AIPoint.sum(futureSpeed, p));
            move.setTurn(hockeyist.angleTo(target));
            hockeyist.setLocation(p);
            return move;
        }

        AIMove toStop(AIHockeyist hockeyist, AIPoint target, double deviation) {
            return null;
        }
    }

//
//    public static AIPoint projectionOnSpeed(AIManager.AIHockeyist hockeyist, AIPoint target) {
//        AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
//        // projection target vector on speed vector
//        AIPoint projection = AI.projection(targetVector, hockeyist.getSpeed());
//        return projection;
//    }
//
//    public static double projectionOnSpeedScalar(AIManager.AIHockeyist hockeyist, AIPoint target) {
//        AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
//        // projection target vector on speed vector
//        if (hockeyist.getSpeedScalar() < AI.COMPUTATION_BIAS) {
//            return 0;
//        }
//        return AI.projectionScalar(targetVector, hockeyist.getSpeed());
//    }
//
//    // both values should be positive
//    public static void adjustSpeedUp(double needSpeed, double haveSpeed, AIMove move) {
//        double diff = needSpeed - haveSpeed;
//        if (diff < 0) {
//            // i'm too fast
//            move.setSpeedUp(max(diff, -1));
//        } else {
//            move.setSpeedUp(min(diff, 1));
//        }
//        if (abs(move.getSpeedUp() - 1) > 1e-7) {
//            int i = 0;
//            ++i;
//        }
//    }
//
//
//    public static AIMove goTo(AIManager.AIHockeyist hockeyist, AIPoint target) {
//        AIMove move = new AIMove();
//        // bad situation if speed equals to zero or we are in place
//
//        AIPoint targetVector = AIPoint.difference(target, hockeyist.getLocation());
//        if (AI.vectorLength(targetVector) < AI.COMPUTATION_BIAS) {
//            return move;
//        }
//        if (hockeyist.getSpeedScalar() < AI.COMPUTATION_BIAS) {
//            move.setSpeedUp(1);
//            return move;
//        }
//        double turnAngle = hockeyist.angleTo(target);
//        // don't need to consider if adjustSpeedUp gets negative values
//        if (abs(turnAngle) > PI/2) {
//            move.setSpeedUp(-1);
//            return move;
//        }
//
//
//
//
//        double speedTargetAngle = AI.angle(hockeyist.getSpeed(), targetVector);
//
//        // now we will work with projections
//        AIFriction friction = AIFriction.getInstance();
//        AIPoint hockeyistNextLocation = hockeyist.getNextLocation();
//        AIPoint hockeyistNextVector = AIPoint.difference(target, hockeyistNextLocation);
//
//        // how much time will need to turn ?
//        // can use future hockeyist location
//        double ticks = abs(turnAngle)/ AIManager.MAX_TURN_ANGLE_PER_TICK;
//        AIFriction.DistanceSpeed distanceSpeed = friction.hockeyistAfterTicks(
//                    ticks, hockeyist.getSpeedScalar());
//        // how much will travel during ticks
//        double distance = distanceSpeed.distance;
//        // negative value is unacceptable
//        if (distance < 0) throw new RuntimeException();
//        // this can be negative
//        double projectionValue = abs(projectionOnSpeedScalar(hockeyist, target));
//        adjustSpeedUp(projectionValue, distance, move);
//        // turn for future position
//
//        move.setTurn(turnAngle);
//        return move;
//    }
//
//
//
//    public static AIMove goToStop(AIManager.AIHockeyist hockeyist, AIPoint location) {
//        AIMove move = new AIMove();
//        AIFriction friction = AIFriction.getInstance();
//        AIFriction.DistanceTicks distanceTicks = friction.hockeyistStopDistance(hockeyist.getSpeedScalar());
//        double projectionValue = projectionOnSpeedScalar(hockeyist, location);
//        adjustSpeedUp(projectionValue, distanceTicks.distance, move);
//        move.setTurn(hockeyist.angleTo(location));
//        return move;
//    }
//

}
