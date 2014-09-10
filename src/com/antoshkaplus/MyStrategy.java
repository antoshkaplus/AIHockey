package com.antoshkaplus;

import com.antoshkaplus.model.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

//import model.*;


import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {

    private static final double ANGLE_ERROR = 2 * PI / 180;
    private static final double GOALIE_SPEED = 6;
    private static final double AVERAGE_PUCK_SPEED = 15;
    private static final double ACCESS_DISTANCE = 120;
    private static final double ACCESS_ANGLE_BIAS = PI/12;

    private static final double PASS_ANGLE_BIAS = PI/3;

    private static final double MAX_SPEED_UP = 1;

    boolean isBetween(double one, double two, double between) {
        return (one < between && between < two) ||
                (one > between && between > two);
    }

    double angle(Point2D p_one, Point2D p_center, Point2D p_two) {
        double a = p_one.distance(p_center);
        double b = p_two.distance(p_center);
        double c = p_one.distance(p_two);
        return Math.acos((a*a + b*b - c*c)/(2*a*b));
    }

    double positiveMod(double number, double d) {
        return ((number % d) + d) % d;
    }

    double sumAngles(double absAngle, double relAngle) {
        return positiveMod(absAngle + relAngle, 2 * PI);
    }

    double strikeAngle(Point2D bar, Point2D otherBar, Hockeyist hock) {
        Point2D p_hock = new Point2D.Double(hock.getX(), hock.getY());
        double strikeAngle = 0;
        // otherwise we will use reflection from bar
        if (angle(p_hock, bar, otherBar) >= PI/2) {
            // angle is in 0,PI/2 range
            // can't use reflection
            double hb = hock.getDistanceTo(bar.getX(), bar.getY());
            double pb = puck.getRadius();
            strikeAngle = asin(pb / hb);
        }
        strikeAngle += ANGLE_ERROR;
        // find angle relative to hockeyist
        double barAngle = sumAngles(hock.getAngleTo(bar.getX(), bar.getY()), hock.getAngle());
        double trySum = sumAngles(barAngle, strikeAngle);
        Point2D p_sum = new Point2D.Double(cos(trySum), sin(trySum));
        double tryDiff = sumAngles(barAngle, -strikeAngle);
        Point2D p_diff = new Point2D.Double(cos(tryDiff), sin(tryDiff));

        return  otherBar.distance(p_sum) < otherBar.distance(p_diff) ?
                trySum : tryDiff;
    }

    double botStrikeAngle(Hockeyist hockeyist) {
        return strikeAngle(new Point2D.Double(he.getNetFront(), he.getNetBottom()),
                new Point2D.Double(he.getNetFront(), he.getNetTop()), hockeyist);
    }

    double topStrikeAngle(Hockeyist hockeyist) {
        return strikeAngle(new Point2D.Double(he.getNetFront(), he.getNetTop()),
                new Point2D.Double(he.getNetFront(), he.getNetBottom()), hockeyist);
    }

    Point2D intersection(Line2D line_0, Line2D line_1) {
        double  x_0 = line_0.getX1(),
                x_1 = line_0.getX2(),
                y_0 = line_0.getY1(),
                y_1 = line_0.getY2(),

                x_2 = line_1.getX1(),
                x_3 = line_1.getX2(),
                y_2 = line_1.getY1(),
                y_3 = line_1.getY2();
        double  a = x_0*y_1 - y_0*x_1,
                b = x_2*y_3 - y_2*x_3,
                c = (x_0 - x_1)*(y_2 - y_3) - (y_0 - y_1)*(x_2 - x_3);
        return new Point2D.Double((a*(x_2 - x_3) - (x_0 - x_1)*b)/c, (a*(y_2 - y_3) - (y_0 - y_1)*b)/c);
    }

    Point2D getPoint(Unit u) {
        return new Point2D.Double(u.getX(), u.getY());
    }


    // we will use startSpeed because don't know yet will i ever swing
    // assume current puck
    boolean canOpponentGoalieIntercept(double startSpeed, double angle) {
        if (hisGoalie == null) return false;
        Point2D unit = new Point2D.Double(cos(angle), sin(angle));
        Point2D puck = new Point2D.Double(this.puck.getX(), this.puck.getY());
        Line2D line_puck = new Line2D.Double(puck, sum(puck, unit));
        Line2D line_goalie = new Line2D.Double(
                hisGoalie.getX(),
                he.getNetBottom(),
                hisGoalie.getX(),
                he.getNetTop());
        Point2D p = intersection(line_puck, line_goalie);
        Point2D g = getPoint(hisGoalie);
        double t = targetTicks(puck, p, startSpeed);
        double d_g = t * GOALIE_SPEED;
        double d_pg = p.distance(g);
        return this.puck.getRadius() + d_g + hisGoalie.getRadius() > d_pg;
    }

    Point2D sum(Point2D p_0, Point2D p_1) {
        Point2D p = (Point2D)p_0.clone();
        p.setLocation(
                p_0.getX() + p_1.getX(),
                p_0.getY() + p_1.getY());
        return p;
    }

    Point2D difference(Point2D p_0, Point2D p_1) {
        Point2D p = (Point2D)p_0.clone();
        p.setLocation(
                p_0.getX() - p_1.getX(),
                p_0.getY() - p_1.getY());
        return p;
    }

    double dotProduct(Point2D p_0, Point2D p_1) {
        return p_0.getX()*p_1.getX() + p_0.getY()*p_1.getY();
    }

    double targetTicks(Point2D source, Point2D target, double speed) {
        return source.distance(target)/speed;
    }

    boolean isInRange(Hockeyist hock, Unit unit) {
        return hock.getDistanceTo(unit) < ACCESS_DISTANCE && abs(hock.getAngleTo(unit)) < ACCESS_ANGLE_BIAS;
    }

    boolean canTakePuck(Hockeyist hock) {
        return isInRange(hock, puck);
    }

    boolean canStrikeOpponent(Hockeyist hockeyist) {
        try {
            for (Hockeyist hock : opponents) {
                if(hock == null) {
                    System.out.println("lol");

                }
                if (hock != hisGoalie && isInRange(hockeyist, hock)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("lol");
        }
        return false;
    }

    double collisionTicks(Point2D p_0, Point2D v_0, double r_0,
                          Point2D p_1, Point2D v_1, double r_1) {
        Point2D p_d = difference(p_0, p_1);
        Point2D v_d = difference(v_0, v_1);
        double pp = dotProduct(p_d, p_d);
        double pv = 2*dotProduct(p_d, v_d);
        double vv = dotProduct(v_d, v_d);
        double rr = Math.pow(r_0 + r_1, 2);
        double dd = pv*pv - 4*(pp - rr)*vv;
        if (dd < 0) return -1;
        dd = Math.sqrt(dd);
        // will take nearest in time collision
        return Math.min((-pv - dd)/(2*vv), (-pv + dd)/(2*vv));
    }

    Hockeyist nearestOpponentNotGoalie(Unit source) {
        double minDist = Double.MAX_VALUE;
        Hockeyist minHock = null;
        for (Hockeyist hock : opponents) {
            if (hock == hisGoalie) {
                continue;
            }
            if (hock.getDistanceTo(source) < minDist) {
                minHock = hock;
                minDist = hock.getDistanceTo(source);
            }
        }
        return minHock;
    }

    boolean isInPosition(Hockeyist hock, Point2D position) {
        return hock.getDistanceTo(position.getX(), position.getY()) < hock.getRadius();
    }

    void turnToPosition(Point2D p) {
        move.setTurn(self.getAngleTo(p.getX(), p.getY()));
    }

    Point2D nearest(Unit u, Iterable<Point2D> ps) {
        double minDist = Double.MAX_VALUE;
        Point2D minP = null;
        for (Point2D p : ps) {
            if (u.getDistanceTo(p.getX(), p.getY()) < minDist) {
                minDist = u.getDistanceTo(p.getX(), p.getY());
                minP = p;
            }
        }
        return minP;
    }

    private void offence() {
        if (puck.getOwnerHockeyistId() == self.getId()) {
            double selfAngle = self.getAngle();
            double angle;
            angle = topStrikeAngle(self);
            if (!canOpponentGoalieIntercept(AVERAGE_PUCK_SPEED, angle)) {
                double x = cos(angle) + self.getX();
                double y = sin(angle) + self.getY();
                double d = self.getAngleTo(x, y);
                if (d < PASS_ANGLE_BIAS) {
                    move.setPassAngle(d);
                    move.setPassPower(MAX_SPEED_UP);
                    move.setAction(ActionType.PASS);
                } else {
                    move.setTurn(d);
                }
                return;
            }
            angle = botStrikeAngle(self);
            if (!canOpponentGoalieIntercept(AVERAGE_PUCK_SPEED, angle)) {
                double x = cos(angle) + self.getX();
                double y = sin(angle) + self.getY();
                double d = self.getAngleTo(x, y);
                if (d < PASS_ANGLE_BIAS) {
                    move.setPassAngle(d);
                    move.setPassPower(MAX_SPEED_UP);
                    move.setAction(ActionType.PASS);
                } else {
                    move.setTurn(d);
                }
                return;
            }
            // find some cool location
            if (isInPosition(self, scorePositions.get(0))) {
                turnToPosition(scorePositions.get(1));
            } else if (isInPosition(self, scorePositions.get(1))) {
                turnToPosition(scorePositions.get(0));
            } else {
                turnToPosition(nearest(self, scorePositions));
            }
            move.setSpeedUp(MAX_SPEED_UP);

        } else {
            if (canStrikeOpponent(self)) {
                move.setAction(ActionType.STRIKE);
            }
            Hockeyist near = nearestOpponentNotGoalie(puckOwner);
            move.setTurn(self.getAngleTo(near));
            move.setSpeedUp(MAX_SPEED_UP);
        }
    }

    private void defence() {
        move.setTurn(self.getAngleTo(puck));
        move.setSpeedUp(MAX_SPEED_UP);
        if (isInRange(self, puck)) {
            move.setAction(ActionType.STRIKE);
        } else if (canStrikeOpponent(self)) {
            move.setAction(ActionType.STRIKE);
        }
    }

    private void freePuck() {
        move.setTurn(self.getAngleTo(puck));
        move.setSpeedUp(MAX_SPEED_UP);
        if (isInRange(self, puck)) {
            move.setAction(ActionType.TAKE_PUCK);
        } else if (canStrikeOpponent(self)) {
            move.setAction(ActionType.STRIKE);
        }
    }

    void init(Hockeyist self, World world, Game game, Move move) {
        this.move = move;
        this.world = world;
        this.self = self;
        this.game = game;
        this.puck = world.getPuck();
        this.me = world.getMyPlayer();
        this.he = world.getOpponentPlayer();

        int size = world.getHockeyists().length/2;
        opponents = new Hockeyist[size];
        teammates = new Hockeyist[size];
        int i_his = 0;
        int i_my = 0;
        puckOwner = null;
        hisGoalie = null;
        myGoalie = null;
        //System.out.println(world.getHockeyists().length);
        for (Hockeyist hock : world.getHockeyists()) {
            if (hock.isTeammate()) {
                teammates[i_my++] = hock;
                if (hock.getType() == HockeyistType.GOALIE) {
                    myGoalie = hock;
                }
            } else {
                if (hock.getType() == HockeyistType.GOALIE) {
                    hisGoalie = hock;
                }
                opponents[i_his++] = hock;
            }
            if (hock.getId() == puck.getOwnerHockeyistId()) {
                puckOwner = hock;
            }
        }

        if (!initOutput) {
            this.myRightNet = me.getNetFront() > he.getNetFront();
            double factor = myRightNet ? 1 : -1;
            scorePositions = new ArrayList<Point2D>();
            double dx = self.getRadius() + 2*puck.getRadius() + 50;
            double x = hisGoalie.getX() + factor * dx;
            scorePositions.add(new Point2D.Double(x, he.getNetBottom() - self.getRadius()));
            scorePositions.add(new Point2D.Double(x, he.getNetTop() + self.getRadius()));


            Point2D p;
            p = scorePositions.get(0);
            System.out.println(p.getX() + ", " + p.getY());
            p = scorePositions.get(1);
            System.out.println(p.getX() + ", " + p.getY());
            initOutput = true;
        }
    }


    @Override
    public void move(Hockeyist self, World world, Game game, Move move) {
        init(self, world, game, move);
        if (puckOwner == null) {
            freePuck();
        } else if (puckOwner.isTeammate()) {
            offence();
        } else {
            defence();
        }
    }

    List<Point2D> scorePositions;

    boolean initOutput = false;



    Puck puck;
    World world;
    Game game;
    Player he;
    Player me;

    boolean myRightNet;

    Hockeyist[] opponents;
    Hockeyist[] teammates;

    Hockeyist puckOwner;
    Hockeyist self;
    Hockeyist hisGoalie;
    Hockeyist myGoalie;
    Move move;
}
