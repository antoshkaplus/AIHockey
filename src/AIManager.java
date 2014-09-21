
import model.*;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import static java.lang.StrictMath.*;

/**
 * Created by antoshkaplus on 9/10/14.
 */


public class AIManager {

    private static final double PUCK_FRICTION_FACTOR = 0.02;

    private static AIManager instance = null;

    private int currentTick = -1;

    private AIDataCollector dataCollector = new AIDataCollector();

    private Game game = null;
    private World world = null;


    private AIPuck puck = null;
    private Player he = null;
    private Player me = null;

    // don't count goalie
    private Collection<AIHockeyist> opponents;
    private Collection<AIHockeyist> teammates;

    private AIHockeyist puckOwner;
    private AIHockeyist hisGoalie;
    private AIHockeyist myGoalie;

    private AINet hisNet;
    private AINet myNet;

    private AILine hisNetSegment = null;
    private AILine hisGoalieSegment = null;
    private AILine myNetSegment = null;
    private AILine myGoalieSegment = null;
    private AILine hisPreGoalieSegment = null;
    private AILine myPreGoalieSegment = null;

    private AIPoint myNetCenter = null;
    private AIPoint hisNetCenter = null;

    private AIRectangle defenceZone;
    private AIRectangle neutralZone;
    private AIRectangle offensiveZone;

    private AIRectangle myNoScoreZone;
    private AIRectangle hisNoScoreZone;
    private AIRectangle myZone;
    private AIRectangle hisZone;

    private AIRectangle centralZone;
    private AIRectangle topZone;
    private AIRectangle bottomZone;

    private AIRectangle hisCentralZone;
    private AIRectangle myCentralZone;

    private AIRectangle rink;
    private AIPoint center;


    private AIFriction friction = AIFriction.getInstance();

    private AIDefendPuck defendPuck;
    private AITakePuck takePuck;
    private AISideStraightAttack sideStraightAttack;

    private AITest test;


    private Map<Long, AIMove> moves;


    private AIManager() {}

    public void initialize(Game game) {
        // also init his goalie // he won't change
        this.game = game;
        rink = new AIRectangle(
                game.getRinkLeft(),
                game.getRinkTop(),
                game.getRinkRight() - game.getRinkLeft(),
                game.getRinkBottom() - game.getRinkTop());
        center = new AIPoint(rink.origin.x + rink.size.x/2, rink.origin.y + rink.size.y/2);

        hisNetSegment = new AILine(he.getNetFront(),
                he.getNetBottom(),
                he.getNetFront(),
                he.getNetTop());
        hisGoalieSegment = new AILine(
                hisGoalie.getX(),
                he.getNetBottom() - AIHockeyist.RADIUS,
                hisGoalie.getX(),
                he.getNetTop() + AIHockeyist.RADIUS);

        hisNetCenter = new AIPoint(
                he.getNetFront(),
                (game.getRinkTop() + game.getRinkBottom())/2);

        myNetSegment = new AILine(me.getNetFront(),
                me.getNetBottom(),
                me.getNetFront(),
                me.getNetTop());
        myGoalieSegment = new AILine(
                myGoalie.getX(),
                me.getNetBottom() - AIHockeyist.RADIUS,
                myGoalie.getX(),
                me.getNetTop() + AIHockeyist.RADIUS);
        myNetCenter = new AIPoint(
                me.getNetFront(),
                (game.getRinkTop() + game.getRinkBottom())/2);

        double widthNoScoreZone = 2*AIHockeyist.RADIUS + 2*AIHockeyist.RADIUS;
        AIRectangle leftNoScoreZone = new AIRectangle(
                game.getRinkLeft(),
                rink.origin.y,
                widthNoScoreZone,
                rink.size.y);
        AIRectangle rightNoScoreZone = new AIRectangle(
                game.getRinkRight() - widthNoScoreZone,
                rink.origin.y,
                widthNoScoreZone,
                rink.size.y);
        AIRectangle leftZone = new AIRectangle(
                rink.origin.x,
                rink.origin.y,
                rink.size.x/2,
                rink.size.y);
        AIRectangle rightZone = new AIRectangle(
                rink.origin.x + rink.size.x/2,
                rink.origin.y,
                rink.size.x/2,
                rink.size.y);

        hisPreGoalieSegment = (AILine)hisGoalieSegment.clone();
        myPreGoalieSegment = (AILine)myGoalieSegment.clone();
        if (he.getNetFront() == game.getRinkLeft()) {
            hisNoScoreZone = leftNoScoreZone;
            myNoScoreZone = rightNoScoreZone;
            hisZone = leftZone;
            myZone = rightZone;
            hisPreGoalieSegment.translate(AIHockeyist.RADIUS, 0);
            myPreGoalieSegment.translate(-AIHockeyist.RADIUS, 0);
        } else {
            hisNoScoreZone = rightNoScoreZone;
            myNoScoreZone = leftNoScoreZone;
            hisZone = rightZone;
            myZone = leftZone;
            hisPreGoalieSegment.translate(-AIHockeyist.RADIUS, 0);
            myPreGoalieSegment.translate(AIHockeyist.RADIUS, 0);
        }

        // goalie zone...
        centralZone = new AIRectangle(
                rink.origin.x,
                he.getNetTop(),
                rink.size.x,
                he.getNetBottom() - he.getNetTop());

        myCentralZone = AIRectangle.intersection(centralZone, myZone);
        hisCentralZone = AIRectangle.intersection(centralZone, hisZone);

        AIRectangle left = new AIRectangle(rink.origin, rink.size);
        left.size.x = rink.size.x/3;
        AIRectangle right = new AIRectangle(
                rink.origin.x + 2*rink.size.x/3,
                rink.origin.y, rink.size.x/3, rink.size.y);
        neutralZone = new AIRectangle(
                rink.origin.x + rink.size.x/3,
                rink.origin.y,
                rink.size.x/3,
                rink.size.y);
        if (he.getNetFront() == game.getRinkLeft()) {
            defenceZone = right;
            offensiveZone = left;
        } else {
            defenceZone = left;
            offensiveZone = right;
        }

        hisNet = new AINet(hisNetSegment, hisGoalieSegment);
        myNet = new AINet(myNetSegment, myGoalieSegment);

        moves = new HashMap<Long, AIMove>();

        defendPuck = new AIDefendPuck();
        takePuck = new AITakePuck();
        sideStraightAttack = new AISideStraightAttack();
    }

    public boolean isInitialized() {
        return game != null;
    }

    public AIRectangle getCentralZone() {
        return centralZone;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public AIRectangle getRink() {
        return rink;
    }

    public AIPuck getPuck() {
        return puck;
    }

    public AIPoint getCenter() {
        return center;
    }

    public AINet getHisNet() { return hisNet; }

    public AINet getMyNet() { return myNet; }

    public AIRectangle getHisCentralZone() {
        return hisCentralZone;
    }

    public AIRectangle getHisNoScoreZone() {
        return hisNoScoreZone;
    }

    // update move for each guy
    // probably put inside world and game
    public void update(World world, Game game) {
        if (world.getTick() == currentTick) {
            // already did all updations
            return;
        }
        currentTick = world.getTick();

        this.he = world.getOpponentPlayer();
        this.me = world.getMyPlayer();
        this.puck = new AIPuck(world.getPuck());

        List<AIHockeyist> opponents = new ArrayList<AIHockeyist>();
        List<AIHockeyist> teammates = new ArrayList<AIHockeyist>();
        hisGoalie = null;
        myGoalie = null;
        puckOwner = null;
        //System.out.println(world.getHockeyists().length);
        for (Hockeyist hock : world.getHockeyists()) {
            AIHockeyist aiHock = new AIHockeyist(hock);
            if (hock.isTeammate()) {
                if (hock.getType() == HockeyistType.GOALIE) {
                    myGoalie = aiHock;
                } else {
                    teammates.add(aiHock);
                }
            } else {
                if (hock.getType() == HockeyistType.GOALIE) {
                    hisGoalie = aiHock;
                } else {
                    opponents.add(aiHock);
                }
            }
            if (hock.getId() == world.getPuck().getOwnerHockeyistId()) {
                puckOwner = aiHock;
            }
        }
        this.opponents = opponents;
        this.teammates = teammates;
        if (!isInitialized()) {
            // initialize some constants only once if needed
            // goalie should be already ready
            initialize(game);
        }

        updateTeammates();
    }

    private void updateTeammates() {
        for (AIHockeyist hockeyist : teammates) {
            AIMove move;
            if (puckOwner == null) {
                move = takePuck.move(hockeyist);
            } else if (puckOwner.isTeammate()) {
                if (puckOwner == hockeyist) {
                    move = sideStraightAttack.move(hockeyist);
                    if (!move.isValid()) {
                        move = AIGo.to(hockeyist, AIPoint.middle(center, myNet.getNetCenter()));
                    }
                } else {
                    move = defendPuck.move(hockeyist);
                }
            } else {
                move = defendPuck.move(hockeyist);
            }
            // protection from permanent SWING
            if (hockeyist != puckOwner && hockeyist.getLastAction() == ActionType.SWING) {
                move.setAction(ActionType.CANCEL_STRIKE);
            }
            moves.put(hockeyist.getId(), move);
        }
        //dataCollector.collectPuckData(puck, puckOwner);
    }

    public Move move(long id) {
        return moves.get(id);
    }

    public static AIManager getInstance() {
        if (instance == null) {
            instance = new AIManager();
        }
        return instance;
    }



    public boolean isPuckOwner(AIHockeyist hockeyist) {
        return hockeyist == puckOwner;
    }

    Collection<AIHockeyist> getOpponents() {
        return opponents;
    }

    Collection<AIHockeyist> getTeammates() {
        return teammates;
    }

    public AIRectangle getMyZone() {
        return myZone;
    }

    public AIRectangle getHisZone() {
        return hisZone;
    }

//
//    class PuckAfterDistance {
//        PuckAfterDistance(double ticks, double speed) {
//            this.ticks = ticks;
//            this.speed = speed;
//        }
//        double ticks;
//        double speed;
//    }
//
//    // should return speed and number of ticks
//    PuckAfterDistance puckAfterDistance(double distance, double startSpeed) {
//        double D = startSpeed*startSpeed - 2*distance*PUCK_FRICTION_FACTOR;
//        if (D < 0) return null;
//        double t_0 = (-startSpeed - sqrt(D))/(-PUCK_FRICTION_FACTOR);
//        double t_1 = (-startSpeed + sqrt(D))/(-PUCK_FRICTION_FACTOR);
//        // t_0 is when he goes back
//        return new PuckAfterDistance(t_1, startSpeed - PUCK_FRICTION_FACTOR*t_1);
//    }

//    // should make big table of values. like distance of stadium
//    // [distance, speed]
//    // also would be good to return end speed
//    double puckTime(double distance, double startSpeed) {
//        return distance/startSpeed;
//    }

//    // should be a table
//    double hockeyistTime(double distance, double startSpeed) {
//        return distance/startSpeed;
//    }

//    // how much distance will run before stopping
//    double stopDistance(
//            double startSpeed,
//            double negativeAcceleration) {
//        double t = -startSpeed/negativeAcceleration;
//        if (t < 0) throw new RuntimeException("");
//        return startSpeed*t + negativeAcceleration*t*t/2;
//    }

//    private class SwingStrikeScore {

//
//        AIMove move(AIHockeyist hockeyist) {

//    }


//    private class StrikeScore {
//
//        AIMove move(AIHockeyist hockeyist) {
//            AIMove move = new AIMove();
//            if (puckOwner != hockeyist) {
//                move.setValid(false);
//                return move;
//            }
//            if (canScoreStrike(hockeyist)) {
//                move.setAction(ActionType.STRIKE);
//                return move;
//            }
//            move.setValid(false);
//            return move;
//        }
//
//    }


//    private class PassScore {
//
//        AIMove move(AIHockeyist hockeyist) {
//            AIMove move = new AIMove();
//            if (puckOwner != hockeyist) {
//                move.setValid(false);
//                return move;
//            }
//            AIPoint bar = farthestBar(hockeyist, hisNetSegment);
//            // 2 degrees
//            double angleForGoalie = scoreAngle(bar,
//                    otherBar(hisNetSegment, bar),
//                    hockeyist.getLocation(),
//                    2*ANGLE_NATURAL_DEVIATION);//2*ANGLE_DEVIATION);
//            double barAngle = AI.orientAngle(AIPoint.difference(bar, hockeyist.getLocation()));
//            double a = AI.orientAngle(barAngle, angleForGoalie);
//            double angleForHockeyist = AI.orientAngle(barAngle + a/2);
//            double oneAngle = AI.orientAngle(hockeyist.getAngle() - PASS_ANGLE_BIAS);
//            double twoAngle = AI.orientAngle(hockeyist.getAngle() + PASS_ANGLE_BIAS);
//            if (AI.isAngleBetween(oneAngle, twoAngle, angleForHockeyist)) {
//                double hockAngle = hockeyist.getAngle();
//                double passAngle = AI.orientAngle(hockAngle, angleForHockeyist);
//                // won't adjust puck location in pass time because of frictional force
//                if (!canGoalieIntercept(hisGoalie,
//                        hockeyist.getLocation(), angleForGoalie, passPuckSpeed(hockeyist, 1, passAngle))) {
//                    move.setPassPower(1);
//                    move.setPassAngle(passAngle);
//                    move.setAction(ActionType.PASS);
//                    return move;
//                }
//            }
//            move.setValid(false);
//            return move;
//        }
//    }


//    private class Parabola {
//        // x = a * (y-d)^2 + c
//        double d;
//        double a;
//        double c;
//
//        static final double INDENT = HOCKEYIST_RADIUS + 2*PUCK_RADIUS;
//
//        // enter points are same for both sides
//        // bottom goes first
//        List<AIPoint> enterLocations;
//        // depend on current game
//        List<AIPoint> scoreLocations;
//
//        Parabola() {
//            enterLocations = new ArrayList<AIPoint>(2);
//            enterLocations.add(new AIPoint(
//                    center.x,
//                    min(game.getRinkBottom() - INDENT, getBottomY(center.x))));
//            enterLocations.add(new AIPoint(
//                    center.x,
//                    max(game.getRinkTop() + INDENT, getTopY(center.x))));
//
//            scoreLocations = new ArrayList<AIPoint>(2);
//            double dx = 2*HOCKEYIST_RADIUS + 2*puck.getRadius() + 170;
//            double x = hisGoalie.getX() + signum(hisGoalie.getX() - he.getNetFront()) * dx;
//            // can play with this coefficients
//            scoreLocations.add(new AIPoint(x, he.getNetBottom() + 2*PUCK_RADIUS));
//            scoreLocations.add(new AIPoint(x, he.getNetTop() - 2*PUCK_RADIUS));
//
//
//            List<AIPoint> net = new ArrayList<AIPoint>(2);
//            // need farthest one for current
//            net.add(nearestBar(scoreLocations.get(1), hisNetSegment));
//            net.add(nearestBar(scoreLocations.get(0), hisNetSegment));
//
//            AIPoint v_0 = AIPoint.difference(scoreLocations.get(0), net.get(0));
//            AIPoint v_1 = AIPoint.difference(scoreLocations.get(1), net.get(1));
//
//            // derivatives should be opposite to each other
//            double derivative_0 = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v_0)).x;
//            double derivative_1 = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v_1)).x;
//
//            if (abs(derivative_0 + derivative_1) > AI.COMPUTATION_BIAS) {
//                throw new RuntimeException("Derivatives should be equal");
//            }
//
//            // coefficients should work for either side
//            d = (scoreLocations.get(0).y + scoreLocations.get(1).y)/2;
//            a = derivative_0/(2*(scoreLocations.get(0).y - d));
//            c = scoreLocations.get(0).x - a*pow(scoreLocations.get(0).y - d, 2);
//        }
//
//        double getX(double y) {
//            return (y - d)*(y - d)*a + c;
//        }
//
//        double getY(double x, double sign) {
//            return sign*sqrt((x - c)/a) + d;
//        }
//        // can return NaN
//        double getTopY(double x) {
//            return max(getY(x, -1), game.getRinkTop() + INDENT);
//        }
//
//        double getBottomY(double x) {
//            return min(getY(x, 1), game.getRinkBottom() - INDENT);
//        }
//
//        AIMove move(AIHockeyist hockeyist) {
//            AIMove move = new AIMove();
//            if (myZone.isInside(hockeyist.getLocation())) {
//                // would be find to have this with angle
//                return AIGo.to(hockeyist, nearest(hockeyist, enterLocations));
//            } else {
//                // if hockeyist inside no score zone or to far from control point we should withdraw
//                if (hisNoScoreZone.isInside(hockeyist.getLocation()) || centralZone.isInside(hockeyist.getLocation())) {
//                    move.setValid(false);
//                    return move;
//                }
//                // later should reinitialize for future point
//                double x = hockeyist.getX();
//                double y = hockeyist.getY();
//                double yBottom = getBottomY(x);
//                double yTop = getTopY(x);
//                AIPoint bar = farthestBar(hockeyist, hisNetSegment);
//                // using speed vector aren't quite working well
//                // hisGoalie can go down...
//                x = hockeyist.getX() - signum(center.x - hisNetCenter.x) * 10;
//                if (AI.isValueBetween(
//                        hisNetCenter.x,
//                        scoreLocations.get(0).x,
//                        hockeyist.getX())) {
//                    return AIGo.to(hockeyist, bar);
//                }
//                ArrayList<AIPoint> locations = new ArrayList<AIPoint>(2);
//                locations.add(new AIPoint(x, getBottomY(x)));
//                locations.add(new AIPoint(x, getTopY(x)));
//                AIPoint aim = nearest(hockeyist, locations);
//                return AIGo.to(hockeyist, aim);
//            }
//
//        }
//    }

//    private class CentralDefender {
//        AIMove onOffence(AIHockeyist hockeyist) {
//            double angle = AI.orientAngle(AIPoint.difference(puck.getLocation(), center));
//            return AIGo.to.goToStop(
//                    hockeyist,
//                    center,
//                    angle);
//        }
//
//        AIMove onFreePack(AIHockeyist hockeyist) {
//            if (hisNoScoreZone.isInside(puck.getLocation())) {
//                double angle = AI.orientAngle(AIPoint.difference(puck.getLocation(), center));
//                return AIGo.to.goToStop(
//                        hockeyist,
//                        center,
//                        angle);
//            } else {
//                return role.onFreePuck();
//            }
//        }
//
//        AIMove onDefence(AIHockeyist hockeyist) {
//            if (offensiveZone.isInside(puck.getLocation())) {
//                double angle = AI.orientAngle(AIPoint.difference(puck.getLocation(), center));
//                return AIGo.to.goToStop(
//                        hockeyist,
//                        center,
//                        angle);
//            } else {
//                return role.onDefence();
//            }
//        }
//    }



}
