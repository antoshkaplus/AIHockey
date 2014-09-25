import java.util.*;

import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/24/14.
 */
public class AIMyPuckStrategy implements AIStrategy {

    AIManager manager = AIManager.getInstance();

    Map<Long, AIMove> moves;
    Map<Long, AIRole> roles;



    @Override
    public void init() {
        moves = new TreeMap<Long, AIMove>();
        roles = new TreeMap<Long, AIRole>();
        AIHockeyist puckOwner = manager.getPuckOwner();
        roles.put(puckOwner.getId(), getPuckOwnerRole());
        for (AIHockeyist h : manager.getTeammates()) {
            if (h == puckOwner) {
                continue;
            }
            roles.put(h.getId(), new AIDefendPuck(h.getId()));
        }
    }

    @Override
    public void update() {
//        AIHockeyist puckOwner = manager.getPuckOwner();
//        AIRectangle myZone = manager.getMyZone();
//        int currentTick = manager.getCurrentTick();
//        if (myZone.isInside(puckOwner.getLocation()) && currentTick%100 == 0) init();
        for (Map.Entry<Long, AIRole> p : roles.entrySet()) {
            // check for invalid move actually
            moves.put(p.getKey(), p.getValue().move());
        }

//        AIManager manager = AIManager.getInstance();
//        for (AIHockeyist hockeyist : manager.getTeammates()) {
//            if (puckOwner == hockeyist) {
//                move = sideStraightAttack.move(hockeyist);
//                if (!move.isValid()) {
//                    move = AIGo.to(hockeyist, AIPoint.middle(center, myNet.getNetCenter()));
//                }
//                correctMove(hockeyist, move);
//            } else {
//                move = defendNet.move(hockeyist);
//                if (!move.isValid()) {
//                    move = defendPuck.move(hockeyist);
//                }
//            }
//            moves.put(hockeyist.getId(), move);
//        }
    }

    @Override
    public AIMove getMove(long teammateId) {
        return moves.get(teammateId);
    }

    private AIRole getPuckOwnerRole() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist h = manager.getPuckOwner();
        AINet net = manager.getHisNet();
        long hId = h.getId();
        AIPoint c = net.getNetCenter();
        AIRole[] roles;
        if (c.distance(AIUnit.nearestUnit(c, (Collection)manager.getOpponents()).getLocation()) < 100) {
            roles = new AIRole[] {
                new AISideStraightAttack(hId, AISideStraightAttack.Orientation.BOTTOM),
                new AISideStraightAttack(hId, AISideStraightAttack.Orientation.TOP)
            };
        } else {
            roles = new AIRole[] {
                new AIParabolaAttack(hId, AIParabolaAttack.Orientation.BOTTOM),
                new AIParabolaAttack(hId, AIParabolaAttack.Orientation.TOP),
                new AISideStraightAttack(hId, AISideStraightAttack.Orientation.BOTTOM),
                new AISideStraightAttack(hId, AISideStraightAttack.Orientation.TOP)
            };
        }

        // need to compute best vector to follow or opposite one
        AIRole role = null;
        double bestDirection = puckOwnerBestDirection();
        double bestTurn = h.angleTo(bestDirection);
        double directionDiff = 10000;
        for (AIRole r : roles) {
            AIMove m = r.move();
            if (!m.isValid()) continue;
            // interface should be using can interrupt method.. if we already have some role
            // priority of pass or strike or swing is highest
            // we get basically speed,
            if (abs(m.getTurn() - bestTurn) < directionDiff) {
                role = r;
                directionDiff = abs(m.getTurn() - bestTurn);
            }
        }
        if (role == null) throw new RuntimeException("bitch has no valid role");
        return role;
    }

    // return angle... speed aren't matter that much right now
    // of course high speed is good
    private double puckOwnerBestDirection() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist h = manager.getPuckOwner();
        List<AIPoint> evade = new ArrayList<AIPoint>();
        double effectiveRadius = 300;
        for (AIHockeyist opp : manager.getOpponents()) {
            AIUnit.LocationTicks lt = h.predictCollision(opp);
            AIPoint p = lt == null ? null : lt.location;
            if (p != null && h.distanceTo(p) < effectiveRadius) {
                evade.add(p);
            }
        }
        AIPoint direction = new AIPoint();
        for (AIPoint e : evade) {
            direction.translate(e);
        }
        return AI.orientAngle(direction);
    }
}
