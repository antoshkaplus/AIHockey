
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
    public static final double HOCKEYIST_RADIUS = 30;
    public static final double GOALIE_SPEED = 6;
    public static final double PUCK_RADIUS = 20;
    public static final double SPEED_UP_FACTOR = 0.116;
    public static final double SPEED_DOWN_FACTOR = 0.069;
    //
    public static final double DEGREE = PI / 180;
    public static final double ANGLE_DEVIATION = 2 * DEGREE;
    public static final double ACCESS_DISTANCE = 120;
    public static final double ACCESS_ANGLE_BIAS = PI/12;
    // how much can turn for the pass
    public static final double PASS_ANGLE_BIAS = PI/3;
    public static final double MAX_TURN_ANGLE_PER_TICK = 3 * PI / 180;



    private static final double PUCK_FRICTION_FACTOR = 0.02;

    static AIManager instance = null;

    int currentTick = -1;

    AIDataCollector dataCollector = new AIDataCollector();

    Game game = null;
    World world = null;


    AIPuck puck = null;
    Player he = null;
    Player me = null;

    // don't count goalie
    Iterable<AIHockeyist> opponents;
    Iterable<AIHockeyist> teammates;

    AIHockeyist puckOwner;
    AIHockeyist hisGoalie;
    AIHockeyist myGoalie;

    AILine hisNetSegment = null;
    AILine hisGoalieSegment = null;
    AILine myNetSegment = null;
    AILine myGoalieSegment = null;
    AILine hisPreGoalieSegment = null;
    AILine myPreGoalieSegment = null;



    AIPoint myNetCenter = null;
    AIPoint hisNetCenter = null;


    AIRectangle defenceZone;
    AIRectangle neutralZone;
    AIRectangle offensiveZone;

    AIRectangle myNoScoreZone;
    AIRectangle hisNoScoreZone;
    AIRectangle myZone;
    AIRectangle hisZone;

    AIRectangle centralZone;
    AIRectangle topZone;
    AIRectangle bottomZone;



    AIRectangle rink;
    AIPoint center;

    // strategies
    Parabola parabola;
    PassScore passScore;
    StrikeScore strikeScore;
    CentralDefender centralDefender;
    SwingStrikeScore swingStrikeScore;

    Test test;
    AIRole role;
    Map<Long, AIMove> moves;

    AIManager() {}

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
                he.getNetBottom() - hisGoalie.getRadius(),
                hisGoalie.getX(),
                he.getNetTop() + hisGoalie.getRadius());

        hisNetCenter = new AIPoint(
                he.getNetFront(),
                (game.getRinkTop() + game.getRinkBottom())/2);

        myNetSegment = new AILine(me.getNetFront(),
                me.getNetBottom(),
                me.getNetFront(),
                me.getNetTop());
        myGoalieSegment = new AILine(
                myGoalie.getX(),
                me.getNetBottom() - myGoalie.getRadius(),
                myGoalie.getX(),
                me.getNetTop() + myGoalie.getRadius());
        myNetCenter = new AIPoint(
                me.getNetFront(),
                (game.getRinkTop() + game.getRinkBottom())/2);

        double widthNoScoreZone = 2*HOCKEYIST_RADIUS + 2*HOCKEYIST_RADIUS;
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
            hisPreGoalieSegment.translate(HOCKEYIST_RADIUS, 0);
            myPreGoalieSegment.translate(-HOCKEYIST_RADIUS, 0);
        } else {
            hisNoScoreZone = rightNoScoreZone;
            myNoScoreZone = leftNoScoreZone;
            hisZone = rightZone;
            myZone = leftZone;
            hisPreGoalieSegment.translate(-HOCKEYIST_RADIUS, 0);
            myPreGoalieSegment.translate(HOCKEYIST_RADIUS, 0);
        }

        // goalie zone...
        centralZone = new AIRectangle(
                rink.origin.x,
                he.getNetTop(),
                rink.size.x,
                he.getNetBottom() - he.getNetTop());

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

        moves = new HashMap<Long, AIMove>();
        role = new AIRole();
        test = new Test();
        passScore = new PassScore();
        strikeScore = new StrikeScore();
        parabola = new Parabola();
        centralDefender = new CentralDefender();
        swingStrikeScore = new SwingStrikeScore();
    }

    public boolean isInitialized() {
        return game != null;
    }


    long myGuy = -1;

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

        for (AIHockeyist hockeyist : teammates) {
            role.setHockeyist(hockeyist);
            AIMove move;
            if (puckOwner == null) {
                move = role.onFreePuck();
                if (hockeyist == farthest(puck.getLocation(), (Iterable)teammates)) {
                    //move = centralDefender.onFreePack(hockeyist);
                }
            } else if (puckOwner.isTeammate()) {
                if (puckOwner == hockeyist) {

                    move = strikeScore.move(hockeyist);
                    move = swingStrikeScore.move(hockeyist);
                    if (!move.isValid()) {
                        move = passScore.move(hockeyist);
                        if (!move.isValid()) {
                            move = parabola.move(hockeyist);
                            if (!move.isValid()) {
                                move = AIGo.goTo(hockeyist, myNetCenter);
                            }
                        }
                    }
                } else {
                    move = role.onOffence();
                    //if (!myZone.isInside(puck.getLocation())) move = centralDefender.onOffence(hockeyist);
                }
            } else {
                move = role.onDefence();
                //move = centralDefender.onDefence(hockeyist);
            }
            moves.put(hockeyist.getId(), move);

            if (myGuy == -1 || myGuy == hockeyist.getId()) {
                myGuy = hockeyist.getId();
                move = test.onTestPuckFriction(hockeyist);
                moves.put(myGuy, move);
            } else {
                move = role.onFreePuck();
                moves.put(hockeyist.id, move);
            }
        }
        dataCollector.collectPuckData(puck, puckOwner);
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



    private boolean isPuckOwner(AIHockeyist hockeyist) {
        return hockeyist == puckOwner;
    }

    private AILine getHisNetSegment() {
        return hisNetSegment;
    }

    private AILine getHisGoalieSegment() {
        return hisGoalieSegment;
    }

    private AIHockeyist getHisGoalie() {
        return hisGoalie;
    }

    /** returns -PI, PI orient orientAngle to strike
        deviation should be positive
        can return NaN easy*/
    double scoreAngle(AIPoint bar,
                      AIPoint otherBar,
                      AIPoint origin,
                      double deviation) {

        double strikeAngle = deviation;
        // otherwise we will use reflection from bar
        if (AI.angle(origin, bar, otherBar) >= PI/2) {
            // orientAngle is in 0, PI/2 range
            // can't use reflection
            // task is to compute
            // will get value (0, PI)
            double hb = origin.distance(bar);
            double pb = PUCK_RADIUS;
            strikeAngle = asin(pb / hb);
        }
        // unable to strike any good
        if (strikeAngle >= PI/2) {
            return Double.NaN;
        }

        double barAngle = AI.orientAngle(AIPoint.difference(bar, origin));
        AILine net = new AILine(bar, otherBar);
        // [-PI, PI], [-PI, PI], [0, PI]
        for (Double angle : new Double[]{AI.orientAngle(barAngle + strikeAngle),
                                         AI.orientAngle(barAngle - strikeAngle)}) {
            if (AI.isSegmentIntersectedByRay(net, origin, angle)) {
                return angle;
            }
        }
        return Double.NaN;
    }

    // deviation is KNOWN
    double scoreAngle(AILine netSegment, AIPoint origin) {
        AIPoint bar = netSegment.farthestPoint(origin);
        AIPoint otherBar = otherBar(netSegment, bar);
        double angle = scoreAngle(bar, otherBar, origin, 2*ANGLE_DEVIATION);
        double barAngle = AI.orientAngle(AIPoint.difference(bar, origin));
        double a = AI.orientAngle(angle, barAngle);
        return AI.orientAngle(angle + a);
    }


    class PuckAfterDistance {
        PuckAfterDistance(double ticks, double speed) {
            this.ticks = ticks;
            this.speed = speed;
        }
        double ticks;
        double speed;
    }

    // should return speed and number of ticks
    PuckAfterDistance puckAfterDistance(double distance, double startSpeed) {
        double D = startSpeed*startSpeed - 2*distance*PUCK_FRICTION_FACTOR;
        if (D < 0) return null;
        double t_0 = (-startSpeed - sqrt(D))/(-PUCK_FRICTION_FACTOR);
        double t_1 = (-startSpeed + sqrt(D))/(-PUCK_FRICTION_FACTOR);
        // t_0 is when he goes back
        return new PuckAfterDistance(t_1, startSpeed - PUCK_FRICTION_FACTOR*t_1);
    }



    // should make big table of values. like distance of stadium
    // [distance, speed]
    // also would be good to return end speed
    double puckTime(double distance, double startSpeed) {
        return distance/startSpeed;
    }




    // should be a table
    double hockeyistTime(double distance, double startSpeed) {
        return distance/startSpeed;
    }

    // how much distance will run before stopping
    double stopDistance(
            double startSpeed,
            double negativeAcceleration) {
        double t = -startSpeed/negativeAcceleration;
        if (t < 0) throw new RuntimeException("");
        return startSpeed*t + negativeAcceleration*t*t/2;
    }

    // put inside puck info
    boolean canGoalieIntercept(AIHockeyist goalie,
                               AIPoint location,
                               double angle,
                               double startSpeed) {

        if (goalie == null) return false;
        AILine goalieSegment = null;
        AILine preGoalieSegment = null;
        if (goalie == hisGoalie) {
            goalieSegment = hisGoalieSegment;
            preGoalieSegment = hisPreGoalieSegment;
        } else {
            goalieSegment = myGoalieSegment;
            preGoalieSegment = myPreGoalieSegment;
        }


        AILine puckLine = new AILine(location, angle);
        // can't do anything if he is already on line
        if (puckLine.fromPointDistance(goalie.getLocation()) <
                                    PUCK_RADIUS + HOCKEYIST_RADIUS) return true;
        // before going into goalie zone
        if (!AI.isValueBetween(
                goalieSegment.one.y,
                goalieSegment.two.y,
                location.y)) {
            // correct start speed and location
            AIPoint point = goalieSegment.nearestPoint(location);
            AILine line = new AILine(point, 0);
            point = line.intersection(puckLine);
            PuckAfterDistance params = puckAfterDistance(location.distance(point), startSpeed);
            startSpeed = params.speed;
            location = point;
        }
        // first consider goalie line as most dangerous
        AIPoint pointGoalie = puckLine.intersection(goalieSegment);
        PuckAfterDistance params = puckAfterDistance(location.distance(pointGoalie), startSpeed);
        // need future goalie location
        double goalieY = goalie.getY() + params.ticks*GOALIE_SPEED * signum(pointGoalie.y - goalie.getY());
        if (pointGoalie.distance(goalie.getX(), goalieY) < PUCK_RADIUS + HOCKEYIST_RADIUS ||
                !AI.isValueBetween(goalieSegment.one.y, goalieSegment.two.y, goalieY)) return true;
        // now consider pre goalie line
        AIPoint pointPre = puckLine.intersection(preGoalieSegment);
        params = puckAfterDistance(location.distance(pointPre), startSpeed);
        // need future goalie location
        double yPre = goalie.getY() + params.ticks * GOALIE_SPEED * signum(pointPre.y - goalie.getY());
        if (pointPre.distance(goalie.getX(), yPre) < PUCK_RADIUS + HOCKEYIST_RADIUS ||
                !AI.isValueBetween(goalieSegment.one.y, goalieSegment.two.y, goalieY)) return true;

        // could consider post goalie line location
        return false;
    }


    double strikePuckSpeed(AIHockeyist hockeyist, double strikePower) {
        double speed = AIPoint.ZERO.distance(hockeyist.getSpeed());
        return 20*strikePower + speed*cos(hockeyist.getAngle() - AI.orientAngle(hockeyist.getSpeed()));
    }

    double passPuckSpeed(AIHockeyist hockeyist, double passPower, double passAngle) {
        return 15 * passPower + hockeyist.getSpeedScalar() *
                cos(hockeyist.getAngle() + passAngle - AI.orientAngle(hockeyist.getSpeed()));
    }


    private AIPoint nearestBar(AIPoint point, AILine netSegment) {
        double one = point.distance(netSegment.one);
        double two = point.distance(netSegment.two);
        return one > two ? netSegment.two : netSegment.one;
    }

    private AIPoint nearestBar(AIUnit unit, AILine netSegment) {
        double one = unit.getLocation().distance(netSegment.one);
        double two = unit.getLocation().distance(netSegment.two);
        return one > two ? netSegment.two : netSegment.one;
    }

    private AIPoint farthestBar(AIUnit unit, AILine netSegment) {
        double one = unit.getLocation().distance(netSegment.one);
        double two = unit.getLocation().distance(netSegment.two);
        return one < two ? netSegment.two : netSegment.one;
    }


    private AIUnit farthest(AIPoint source, Iterable<AIUnit> units) {
        double minDistance = Double.MIN_VALUE;
        double distance;
        AIUnit minUnit = null;
        for (AIUnit u : units) {
            if ((distance = u.distanceTo(source)) > minDistance) {
                minUnit = u;
                minDistance = distance;
            }
        }
        return minUnit;
    }

    private AIPoint otherBar(AILine net, AIPoint bar) {
        return net.one == bar ? net.two : net.one;
    }

    private AIUnit nearest(AIPoint source, Iterable<AIUnit> units) {
        double minDistance = Double.MAX_VALUE;
        double distance;
        AIUnit minUnit = null;
        for (AIUnit u : units) {
            if ((distance = u.distanceTo(source)) < minDistance) {
                minUnit = u;
                minDistance = distance;
            }
        }
        return minUnit;
    }

    private AIPoint nearest(AIUnit unit, Iterable<AIPoint> points) {
        double minDistance = Double.MAX_VALUE;
        double distance;
        AIPoint minUnit = null;
        for (AIPoint u : points) {
            if ((distance = unit.distanceTo(u)) < minDistance) {
                minUnit = u;
                minDistance = distance;
            }
        }
        return minUnit;
    }


    public class AIHockeyist extends AIUnit {
        private long id;
        private boolean teammate;
        private ActionType lastAction;
        private Integer lastActionTick;

        AIHockeyist(Hockeyist hockeyist) {
            super(hockeyist);
            id = hockeyist.getId();
            teammate = hockeyist.isTeammate();
            lastAction = hockeyist.getLastAction();
            lastActionTick = hockeyist.getLastActionTick();
        }

        public boolean isTeammate() {
            return teammate;
        }

        public long getId() {
            return id;
        }

        public boolean isInRange(AIUnit unit) {
            return distanceTo(unit) < ACCESS_DISTANCE && abs(angleTo(unit)) < ACCESS_ANGLE_BIAS;
        }

        public boolean didPass(AIPoint point) {
            return AI.isValueBetween(point.x, teammate ? he.getNetFront() : me.getNetFront(), getX()) &&
                    AI.angle(getSpeed(), AIPoint.difference(point, getLocation())) < PI/2;
        }
        public ActionType getLastAction() {
            return lastAction;
        }
        public Integer getLastActionTick() {
            return lastActionTick;
        }

        public AIPoint getNextLocation() {
            return AIPoint.sum(getLocation(), getSpeed());
        }
    }

    public class AIRole {

        List<AIPoint> scoreLocations;
        List<AIPoint> middleLocations;
        AIHockeyist hockeyist;

        void setHockeyist(AIHockeyist hockeyist) {
            this.hockeyist = hockeyist;
        }

        public AIRole() {
            scoreLocations = new ArrayList<AIPoint>(2);
            middleLocations = new ArrayList<AIPoint>(2);
            double dx = HOCKEYIST_RADIUS + 2*puck.getRadius() + 180;
            double x = hisGoalie.getX() + signum(hisGoalie.getX() - he.getNetFront()) * dx;
            scoreLocations.add(new AIPoint(x, he.getNetBottom() + 2*PUCK_RADIUS));
            scoreLocations.add(new AIPoint(x, he.getNetTop() - 2*PUCK_RADIUS));

            middleLocations.add(new AIPoint(rink.getWidth()/2, rink.getTop() + HOCKEYIST_RADIUS));
            middleLocations.add(new AIPoint(rink.getWidth()/2, rink.getBottom() - HOCKEYIST_RADIUS));
        }

        public AIMove onOffence() {
            if (isPuckOwner(hockeyist)) {
                return onOldAttack();
                        //onOwnPuck();
            } else {
                AIUnit u = nearest(puck.getLocation(), (Iterable)opponents);
                return AIGo.goTo(hockeyist, u.getLocation());
            }
        }

        public AIMove onDefence() {
            AIMove move = AIGo.goTo(hockeyist, puck.getLocation());
            if (hockeyist.isInRange(puck)) {
                move.setAction(ActionType.STRIKE);
            }
            for (AIHockeyist h : opponents) {
                if (hockeyist.isInRange(h)) {
                    move.setAction(ActionType.STRIKE);
                    break;
                }
            }
            return move;
        }

        public AIMove onFreePuck() {
            AIMove move = AIGo.goTo(hockeyist, puck.getLocation());
            if (hockeyist.isInRange(puck)) {
                move.setAction(ActionType.TAKE_PUCK);
            }
            return move;
        }

        protected AIMove onOldAttack() {
            AIMove move = new AIMove();
            if (puckOwner == hockeyist) {
                AIPoint teammate = nearest(hisNetSegment.one, (Iterable)teammates).getLocation();
                // LOL
                teammate.x -= HOCKEYIST_RADIUS;
                double tAngle = hockeyist.angleTo(teammate);
                if (abs(tAngle) < PASS_ANGLE_BIAS) {
                    move = AIGo.goTo(hockeyist, teammate);

                    move.setPassAngle(tAngle);
                    move.setPassPower(1);
                    move.setAction(ActionType.PASS);
                } else {
                    move = AIGo.goTo(hockeyist, teammate);
                }
                return move;

//
//                double selfAngle = hockeyist.getAngle();
//                double angle;
//                AIPoint bar = farthestBar(hockeyist, hisNetSegment);
//                angle = scoreAngle(bar, otherBar(hisNetSegment, bar), hockeyist.getLocation(), 0.01);
//                if (!canGoalieIntercept(hisGoalie, hisGoalieSegment, hockeyist.getLocation(), angle, passPuckSpeed(hockeyist, 1, AI.orientAngle(hockeyist.getAngle(), angle)))) {
//                    double x = cos(angle) + hockeyist.getX();
//                    double y = sin(angle) + hockeyist.getY();
//                    double d = hockeyist.angleTo(x, y);
//                    if (d < PASS_ANGLE_BIAS) {
//                        move.setPassAngle(d);
//                        move.setPassPower(1);
//                        move.setAction(ActionType.PASS);
//                    } else {
//                        move.setTurn(d);
//                        move.setSpeedUp(-1);
//                    }
//                    return move;
//                }
//                // find some cool location
//                if (isInPosition(hockeyist, scoreLocations.get(0))) {
//                    move.setTurn(hockeyist.angleTo(scoreLocations.get(1)));
//                } else if (isInPosition(hockeyist, scoreLocations.get(1))) {
//                    move.setTurn(hockeyist.angleTo(scoreLocations.get(0)));
//                } else {
//                    move.setTurn(hockeyist.angleTo(AI.nearestPoint(hockeyist.getLocation(), scoreLocations)));
//                }
//                move.setSpeedUp(1);
//                return move;
            } else {
                return onOffence();
            }
        }
    }

    private class SwingStrikeScore {
        List<AIPoint> turningLocations = new ArrayList<AIPoint>(2);
        double noSpeedUpRadius = 40;

        SwingStrikeScore() {
            // top first
            turningLocations.add(new AIPoint(center.x, rink.origin.y));
            turningLocations.add(new AIPoint(center.x, rink.origin.y + rink.size.y));


        }

        AIMove move(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (puckOwner != hockeyist ||
                    (hisZone.isInside(hockeyist.getLocation()) &&
                     centralZone.isInside(hockeyist.getLocation())) ||
                        hisNoScoreZone.isInside(hockeyist.getLocation())) {
                if (hockeyist.getLastAction() == ActionType.SWING) {
                    move.setAction(ActionType.CANCEL_STRIKE);
                    return move;
                } else {
                    return AIGo.goTo(hockeyist, center);
                }

            }
            if (myZone.isInside(hockeyist.getLocation())) {
                move = AIGo.goTo(hockeyist, nearest(hockeyist, turningLocations));
            }
            if (hisZone.isInside(hockeyist.getLocation())) {
                AIPoint bar = farthestBar(hockeyist, hisNetSegment);
                double angle = AI.orientAngle(hockeyist.getAngle(), scoreAngle(hisNetSegment, hockeyist.getLocation()));
                move.setTurn(angle);

                double speedAngle = hockeyist.angleTo(AIPoint.sum(hockeyist.getLocation(), hockeyist.getSpeed()));
                if (abs(angle) < 0.01 && (hockeyist.getSpeedScalar() < 0.04 || abs(speedAngle) < 0.01)) {
                    move.setAction(ActionType.SWING);

                    if (hockeyist.getLastAction() == ActionType.SWING && !canGoalieIntercept(
                            hisGoalie,
                            hockeyist.getLocation(),
                            hockeyist.getAngle(),
                            strikePuckSpeed(hockeyist, 0.75 + 0.25*hockeyist.lastActionTick/20))) {
                        move.setAction(ActionType.STRIKE);
                    }
                } else {
                    move.setAction(ActionType.CANCEL_STRIKE);
                }
            }
            return move;
        }

    }

    private class StrikeScore {


        AIMove move(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (puckOwner != hockeyist) {
                move.setValid(false);
                return move;
            }
            AIPoint bar = farthestBar(hockeyist, hisNetSegment);
            double angleForGoalie = scoreAngle(
                    bar,
                    otherBar(hisNetSegment, bar),
                    hockeyist.getLocation(),
                    2*ANGLE_DEVIATION);
            double barAngle = AI.orientAngle(AIPoint.difference(bar, hockeyist.getLocation()));
            double a = AI.orientAngle(barAngle, angleForGoalie);
            double angleForHockeyist = AI.orientAngle(barAngle + a/2);
            if (abs(AI.orientAngle(angleForHockeyist, hockeyist.getAngle())) < DEGREE) {
                if (!canGoalieIntercept(hisGoalie,
                        hockeyist.getLocation(), angleForGoalie, strikePuckSpeed(hockeyist, 0.75))) {
                    move.setAction(ActionType.STRIKE);
                    return move;
                }
            }
            move.setValid(false);
            return move;
        }

    }


    private class PassScore {

        AIMove move(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (puckOwner != hockeyist) {
                move.setValid(false);
                return move;
            }
            AIPoint bar = farthestBar(hockeyist, hisNetSegment);
            // 2 degrees
            double angleForGoalie = scoreAngle(bar,
                    otherBar(hisNetSegment, bar),
                    hockeyist.getLocation(),
                    2*ANGLE_DEVIATION);//2*ANGLE_DEVIATION);
            double barAngle = AI.orientAngle(AIPoint.difference(bar, hockeyist.getLocation()));
            double a = AI.orientAngle(barAngle, angleForGoalie);
            double angleForHockeyist = AI.orientAngle(barAngle + a/2);
            double oneAngle = AI.orientAngle(hockeyist.getAngle() - PASS_ANGLE_BIAS);
            double twoAngle = AI.orientAngle(hockeyist.getAngle() + PASS_ANGLE_BIAS);
            if (AI.isAngleBetween(oneAngle, twoAngle, angleForHockeyist)) {
                double hockAngle = hockeyist.getAngle();
                double passAngle = AI.orientAngle(hockAngle, angleForHockeyist);
                // won't adjust puck location in pass time because of frictional force
                if (!canGoalieIntercept(hisGoalie,
                        hockeyist.getLocation(), angleForGoalie, passPuckSpeed(hockeyist, 1, passAngle))) {
                    move.setPassPower(1);
                    move.setPassAngle(passAngle);
                    move.setAction(ActionType.PASS);
                    return move;
                }
            }
            move.setValid(false);
            return move;
        }
    }


    private class Parabola {
        // x = a * (y-d)^2 + c
        double d;
        double a;
        double c;

        static final double INDENT = HOCKEYIST_RADIUS + 2*PUCK_RADIUS;

        // enter points are same for both sides
        // bottom goes first
        List<AIPoint> enterLocations;
        // depend on current game
        List<AIPoint> scoreLocations;

        Parabola() {
            enterLocations = new ArrayList<AIPoint>(2);
            enterLocations.add(new AIPoint(
                    center.x,
                    min(game.getRinkBottom() - INDENT, getBottomY(center.x))));
            enterLocations.add(new AIPoint(
                    center.x,
                    max(game.getRinkTop() + INDENT, getTopY(center.x))));

            scoreLocations = new ArrayList<AIPoint>(2);
            double dx = 2*HOCKEYIST_RADIUS + 2*puck.getRadius() + 170;
            double x = hisGoalie.getX() + signum(hisGoalie.getX() - he.getNetFront()) * dx;
            // can play with this coefficients
            scoreLocations.add(new AIPoint(x, he.getNetBottom() + 2*PUCK_RADIUS));
            scoreLocations.add(new AIPoint(x, he.getNetTop() - 2*PUCK_RADIUS));


            List<AIPoint> net = new ArrayList<AIPoint>(2);
            // need farthest one for current
            net.add(nearestBar(scoreLocations.get(1), hisNetSegment));
            net.add(nearestBar(scoreLocations.get(0), hisNetSegment));

            AIPoint v_0 = AIPoint.difference(scoreLocations.get(0), net.get(0));
            AIPoint v_1 = AIPoint.difference(scoreLocations.get(1), net.get(1));

            // derivatives should be opposite to each other
            double derivative_0 = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v_0)).x;
            double derivative_1 = new AILine(0, 1, 1, 1).intersection(new AILine(AIPoint.ZERO, v_1)).x;

            if (abs(derivative_0 + derivative_1) > AI.COMPUTATION_BIAS) {
                throw new RuntimeException("Derivatives should be equal");
            }

            // coefficients should work for either side
            d = (scoreLocations.get(0).y + scoreLocations.get(1).y)/2;
            a = derivative_0/(2*(scoreLocations.get(0).y - d));
            c = scoreLocations.get(0).x - a*pow(scoreLocations.get(0).y - d, 2);
        }

        double getX(double y) {
            return (y - d)*(y - d)*a + c;
        }

        double getY(double x, double sign) {
            return sign*sqrt((x - c)/a) + d;
        }
        // can return NaN
        double getTopY(double x) {
            return max(getY(x, -1), game.getRinkTop() + INDENT);
        }

        double getBottomY(double x) {
            return min(getY(x, 1), game.getRinkBottom() - INDENT);
        }

        AIMove move(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (myZone.isInside(hockeyist.getLocation())) {
                // would be find to have this with angle
                return AIGo.goTo(hockeyist, nearest(hockeyist, enterLocations));
            } else {
                // if hockeyist inside no score zone or to far from control point we should withdraw
                if (hisNoScoreZone.isInside(hockeyist.getLocation()) || centralZone.isInside(hockeyist.getLocation())) {
                    move.setValid(false);
                    return move;
                }
                // later should reinitialize for future point
                double x = hockeyist.getX();
                double y = hockeyist.getY();
                double yBottom = getBottomY(x);
                double yTop = getTopY(x);
                AIPoint bar = farthestBar(hockeyist, hisNetSegment);
                // using speed vector aren't quite working well
                // hisGoalie can go down...
                x = hockeyist.getX() - signum(center.x - hisNetCenter.x) * 10;
                if (AI.isValueBetween(
                        hisNetCenter.x,
                        scoreLocations.get(0).x,
                        hockeyist.getX())) {
                    return AIGo.goTo(hockeyist, bar);
                }
                ArrayList<AIPoint> locations = new ArrayList<AIPoint>(2);
                locations.add(new AIPoint(x, getBottomY(x)));
                locations.add(new AIPoint(x, getTopY(x)));
                AIPoint aim = nearest(hockeyist, locations);
                return AIGo.goTo(hockeyist, aim);
            }

        }
    }

    private class CentralDefender {
//        AIMove onOffence(AIHockeyist hockeyist) {
//            double angle = AI.orientAngle(AIPoint.difference(puck.getLocation(), center));
//            return AIGo.goToStop(
//                    hockeyist,
//                    center,
//                    angle);
//        }
//
//        AIMove onFreePack(AIHockeyist hockeyist) {
//            if (hisNoScoreZone.isInside(puck.getLocation())) {
//                double angle = AI.orientAngle(AIPoint.difference(puck.getLocation(), center));
//                return AIGo.goToStop(
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
//                return AIGo.goToStop(
//                        hockeyist,
//                        center,
//                        angle);
//            } else {
//                return role.onDefence();
//            }
//        }
    }



    private class Circle {

    }

    private class Test {
        boolean visitedOrigin = false;

        AIMove onTest(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (!visitedOrigin) {
                if (hockeyist.distanceTo(rink.origin) < 50) {
                    move.setSpeedUp(0);
                    if (hockeyist.getSpeedScalar() < 0.001) {
                        move.setTurn(hockeyist.angleTo(rink.getWidth(), hockeyist.getY()));
                    }
                    if (abs(hockeyist.angleTo(rink.getWidth(), hockeyist.getY())) < 0.0001) {
                        visitedOrigin = true;
                    }
                } else {
                    move = AIGo.goTo(hockeyist, rink.origin);
                }
            } else if (visitedOrigin) {
                if (hockeyist.getX() > (rink.getLeft() + rink.getRight())/2) {
                    move.setTurn(hockeyist.angleTo(AIPoint.sum(rink.origin, rink.size)));
                } else {
                    move.setSpeedUp(1);
                }
            }
            return move;
        }

        AIMove onTestGoStop(AIHockeyist hockeyist) {
            return AIGo.goToStop(hockeyist, AIPoint.sum(rink.origin, new AIPoint(200, 400)));
        }

        AIMove onTestGoToStopWithAngle(AIHockeyist hockeyist) {
            AIPoint location = new AIPoint(rink.origin.x + 6*HOCKEYIST_RADIUS + PUCK_RADIUS, hisNetSegment.one.y + PUCK_RADIUS);
            double angle = AI.orientAngle(AIPoint.difference(farthestBar(hockeyist, hisNetSegment), location)) - 0.02;
            AIMove move = AIGo.goToStop(hockeyist, location);
            if (hockeyist.isInRange(puck)) {
                move.setAction(ActionType.STRIKE);
                return move;
            }
            if (!(hockeyist.getSpeedScalar() < 1 && hockeyist.distanceTo(location) <= HOCKEYIST_RADIUS+2 && abs(hockeyist.angleTo(farthestBar(hockeyist, hisNetSegment))) < 0.01)) {
                // move already set
                move.setAction(ActionType.CANCEL_STRIKE);
                return move;
            } else {
                move.setAction(ActionType.SWING);
            }
            return move;
        }

        AIMove onTestHockeyistFriction(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (!visitedOrigin) {
                double d = hockeyist.angleTo(rink.origin);
                move.setTurn(d);
                move.setSpeedUp(1);
                if (hockeyist.distanceTo(rink.origin) < 2*HOCKEYIST_RADIUS) {
                    move.setSpeedUp(0);
                    if (hockeyist.getSpeedScalar() < 0.1) {
                        move.setTurn(hockeyist.angleTo(game.getRinkRight(), hockeyist.getY()));
                        if (hockeyist.angleTo(game.getRinkRight(), hockeyist.getY()) < DEGREE) {
                            visitedOrigin = true;
                        }
                    }
                }
            } else {
                move.setSpeedUp(1);
            }
            if (visitedOrigin && hockeyist.getX() > center.x) {
                move.setSpeedUp(0);
                if (hockeyist.getSpeedScalar() > 0.009) {
                    System.out.println(String.format("%.2f,%.2f;", hockeyist.getSpeedScalar(), hockeyist.distanceTo(center.x, hockeyist.getY())));
                }
            }
            return move;
        }


        AIMove onTestPuckFriction(AIHockeyist hockeyist) {
            AIMove move = new AIMove();
            if (hockeyist == puckOwner) {
                if (hockeyist.distanceTo(rink.origin) < 3 * HOCKEYIST_RADIUS) {
                    if (hockeyist.getSpeedScalar() < 0.04) {
                        double angle = hockeyist.angleTo(AIPoint.sum(rink.origin, rink.size));
                        if (abs(angle) < 0.01) {
                            if (hockeyist.getLastAction() == ActionType.SWING && currentTick - hockeyist.getLastActionTick() > 20) {
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
                    move = AIGo.goTo(hockeyist, rink.origin);
                    move = AIGo.goTo(hockeyist, rink.origin);
                }
            } else {
                move = AIGo.goTo(hockeyist, puck.getLocation());
                move = AIGo.goTo(hockeyist, puck.getLocation());
                if (hockeyist.isInRange(puck)) {
                    move.setAction(ActionType.TAKE_PUCK);
                }
            }
            return move;
        }
    }

}
