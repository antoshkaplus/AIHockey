
import model.Game;
import model.Hockeyist;
import model.Move;
import model.World;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

//import model.*;


import static java.lang.StrictMath.*;

public final class MyStrategy implements Strategy {
    @Override
    public void move(Hockeyist self, World world, Game game, Move move) {
        AIManager manager = AIManager.getInstance();
        manager.update(world, game);
        Move m = manager.move(self.getId());
        move.setAction(m.getAction());
        move.setTurn(m.getTurn());
        move.setSpeedUp(m.getSpeedUp());
        move.setPassAngle(m.getPassAngle());
        move.setPassPower(m.getPassPower());
        move.setTeammateIndex(m.getTeammateIndex());
    }



    //
//
//    double collisionTicks(Point2D p_0, Point2D v_0, double r_0,
//                          Point2D p_1, Point2D v_1, double r_1) {
//        Point2D p_d = difference(p_0, p_1);
//        Point2D v_d = difference(v_0, v_1);
//        double pp = dotProduct(p_d, p_d);
//        double pv = 2*dotProduct(p_d, v_d);
//        double vv = dotProduct(v_d, v_d);
//        double rr = Math.pow(r_0 + r_1, 2);
//        double dd = pv*pv - 4*(pp - rr)*vv;
//        if (dd < 0) return -1;
//        dd = Math.sqrt(dd);
//        // will take nearest in time collision
//        return Math.min((-pv - dd)/(2*vv), (-pv + dd)/(2*vv));
//    }
//    private AIHockeyist lookForPass() {
//        double orientAngle = self.getAngle();
//        double oneBound = orientAngle - PASS_ANGLE_BIAS;
//        double twoBound = orientAngle + PASS_ANGLE_BIAS;
//        for (AIHockeyist hock : teammates) {
//            if (hock == myGoalie || hock == self) continue;
//            double h = d2.orientAngle(orientAngle + self.getAngleTo(hock));
//            double hOne = D2.orientAngle(d2.unitVector(oneBound), d2.unitVector(h));
//            double hTwo = D2.orientAngle(d2.unitVector(twoBound), d2.unitVector(h));
//            double one_two = D2.orientAngle(d2.unitVector(oneBound), d2.unitVector(twoBound));
//            if (hOne + hTwo - one_two < D2.COMPUTATION_BIAS) {
//                return hock;
//            }
//        }
//        return null;
//    }
}
