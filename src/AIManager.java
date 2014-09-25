
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
    private Collection<AIHockeyist> players;

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
    private AIRectangle offenceZone;

    private AIRectangle myNoScoreZone;
    private AIRectangle hisNoScoreZone;
    private AIRectangle myZone;
    private AIRectangle hisZone;

    private AIRectangle centralZone;
    private AIRectangle topZone;
    private AIRectangle bottomZone;

    private AIRectangle hisCentralZone;
    private AIRectangle myCentralZone;

    private AILine[] rinkBorders;

    private AIRectangle rink;
    private AIPoint center;


    private AIFriction friction = AIFriction.getInstance();

    private AIDefendPuck defendPuck;
    private AITakePuck takePuck;
    private AISideStraightAttack sideStraightAttack;
    private AIDefendNet defendNet;
    private AIInterceptPuck interceptPuck;

    private AITest test;
    // initialized as true


    private Map<Long, AIMove> moves;
    private Map<Long, AIRole> roles;

    private AIManager() {}

    private AIMyPuckStrategy myPuckStrategy;
    private AIHisPuckStrategy hisPuckStrategy;
    private AINeuralPuckStrategy neuralPuckStrategy;
    private AIStrategy currentStrategy;

    public void initialize(Game game) {
        // also init his goalie // he won't change
        this.game = game;
        rink = new AIRectangle(
                game.getRinkLeft(),
                game.getRinkTop(),
                game.getRinkRight() - game.getRinkLeft(),
                game.getRinkBottom() - game.getRinkTop());
        center = new AIPoint(rink.origin.x + rink.size.x/2, rink.origin.y + rink.size.y/2);

        rinkBorders = new AILine[]{
            new AILine(rink.getTopLeft(), rink.getTopRight()),
            new AILine(rink.getTopRight(), rink.getBottomRight()),
            new AILine(rink.getBottomRight(), rink.getBottomLeft()),
            new AILine(rink.getBottomLeft(), rink.getTopLeft())
        };

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
            offenceZone = left;
        } else {
            defenceZone = left;
            offenceZone = right;
        }

        hisNet = new AINet(hisNetSegment, hisGoalieSegment);
        myNet = new AINet(myNetSegment, myGoalieSegment);

        moves = new HashMap<Long, AIMove>();
        roles = new HashMap<Long, AIRole>();

        myPuckStrategy = new AIMyPuckStrategy();
        hisPuckStrategy = new AIHisPuckStrategy();
        neuralPuckStrategy = new AINeuralPuckStrategy();
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

    public AIHockeyist getTeammate(long id) {
        for (AIHockeyist h : teammates) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
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
        List<AIHockeyist> players = new ArrayList<AIHockeyist>();
        hisGoalie = null;
        myGoalie = null;
        puckOwner = null;
        //System.out.println(world.getHockeyists().length);
        for (Hockeyist hock : world.getHockeyists()) {
            AIHockeyist aiHock = new AIHockeyist(hock);
            if (hock.getType() != HockeyistType.GOALIE) players.add(aiHock);
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
        this.players = players;

        if (!isInitialized()) {
            // initialize some constants only once if needed
            // goalie should be already ready
            initialize(game);
            currentStrategy = neuralPuckStrategy;
            currentStrategy.init();
        }

        boolean didChange = false;
        if (puckOwner == null) {
            if (!(currentStrategy instanceof AINeuralPuckStrategy)) {
                currentStrategy = neuralPuckStrategy;
                didChange = true;
            }
        } else {
            if (puckOwner.isTeammate() && !(currentStrategy instanceof AIMyPuckStrategy)) {
                currentStrategy = myPuckStrategy;
                didChange = true;
            }
            if (puckOwner.isOpponent() && !(currentStrategy instanceof AIHisPuckStrategy)) {
                currentStrategy = hisPuckStrategy;
                didChange = true;
            }
        }
        if (didChange) {
            currentStrategy.init();
        }
        currentStrategy.update();
    }

    public Move getMove(long id) {
        AIMove m = currentStrategy.getMove(id);
        AIHockeyist h = getTeammate(id);
        // anti swing protection
        if (h != puckOwner && h.getLastAction() == ActionType.SWING) {
            m.setAction(ActionType.CANCEL_STRIKE);
        }
        return m;
    }

    public static AIManager getInstance() {
        if (instance == null) {
            instance = new AIManager();
        }
        return instance;
    }

    public boolean isNeutralPuck() {
        return puckOwner == null;
    }

    public boolean isMyPuck() {
        return puckOwner != null && puckOwner.isTeammate();
    }

    public boolean isHisPuck() {
        return puckOwner != null && !puckOwner.isTeammate();
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

    Collection<AIHockeyist> getPlayers() { return players; }

    public AIRectangle getMyZone() {
        return myZone;
    }

    public AIRectangle getHisZone() {
        return hisZone;
    }

    public AIRectangle getOffenceZone() {
        return offenceZone;
    }

    public AIRectangle getDefenceZone() {
        return defenceZone;
    }

    public boolean isInMyScoreZone(AIPoint point) {
        return myZone.isInside(point)
                && !myNoScoreZone.isInside(point)
                && !centralZone.isInside(point);
    }

    public boolean isInMyScoreZone(AIUnit unit) {
        return isInMyScoreZone(unit.getLocation());
    }

    public AIHockeyist getPuckOwner() {
        return puckOwner;
    }

    private void correctMove(AIHockeyist hockeyist, AIMove move) {
        List<AIPoint> evade = new ArrayList<AIPoint>();
        for (AIHockeyist opp : opponents) {
            AIUnit.LocationTicks lt = hockeyist.predictCollision(opp);
            AIPoint p = lt == null ? null : lt.location;
            if (p != null) {
                evade.add(p);
            }
        }
        double length = 0;
        for (AIPoint e : evade) {
            // this is vector
            AIPoint r = AIPoint.difference(hockeyist.getLocation(), e);
            length += r.scalar();
        }
        length *= 2;

        double oldAngle = hockeyist.getAngle();
        double[] turnAngles = {
            move.getTurn(), 0,
            -hockeyist.getMaxTurnPerTick(),
            hockeyist.getMaxTurnPerTick()
        };
        for (double turn : turnAngles) {
            double angle = AI.orientAngle(oldAngle + turn);
            hockeyist.setAngle(angle);
            AIPoint puckLocation = hockeyist.getPuckLocation();
            boolean bad = false;
            for (AIHockeyist opp : opponents) {
                if (opp.isInStickRange(puckLocation)) { //&& opp.distanceTo(puckLocation) < 80 ) {
                    bad = true;
                    break;
                }
            }
            if (!bad) {
                move.setTurn(turn);
                break;
            }
        }
        hockeyist.setAngle(oldAngle);
    }

    public boolean canOpponentIntercept(AIHockeyist hockeyist) {
        return canOpponentIntercept(hockeyist.getLocation(), hockeyist.getAngle());
    }


    public boolean canOpponentIntercept(AIPoint origin, double angle) {
        AILine line = new AILine(origin, angle);
        for (AIHockeyist opp : opponents) {
            if (line.fromPointDistance(opp.getLocation()) < AIPuck.RADIUS + AIHockeyist.RADIUS &&
                    AI.isValueBetween(origin.x, opp.getLocation().x, cos(angle) + origin.x)) {
                return true;
            }
        }
        return false;
    }

    public boolean canOpponentInterrupt(AIPoint origin, double ticks) {
        for (AIHockeyist opp : opponents) {
            if (opp.ticksAheadTo(origin) < ticks) {
                return true;
            }
        }
        return false;
    }

    boolean canHeScore() {
        return myNet.isScoreAngle(puck.getLocation(), puck.getSpeedAngle(), 0.01) &&
               myNet.canGoalieIntercept(puck);
    }

    public AILine[] getRinkBorders() {
        return rinkBorders;
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
//            if (offenceZone.isInside(puck.getLocation())) {
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
