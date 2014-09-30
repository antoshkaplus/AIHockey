import model.ActionType;

import java.util.Collection;

import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/26/14.
 */
public class AIAgainstPuckOwnerDefence implements AIRole {

    private long hockeyistId;

    AIAgainstPuckOwnerDefence(long hockeyistId) {
        this.hockeyistId = hockeyistId;
    }

    @Override
    public AIMove move() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist po = manager.getPuckOwner();
        AIHockeyist h = manager.getTeammate(hockeyistId);
        AIRectangle offenceZone = manager.getOffenceZone();
        AIPuck puck = manager.getPuck();

        AIPoint poLoc = po.getLocation();
        AIPoint center = manager.getCenter();

        AIPoint want = new AIPoint();
        want.set(poLoc.x, poLoc.y + 2*AIHockeyist.RADIUS * signum(center.y - poLoc.y));
        AIHockeyist poNext = new AIHockeyist(po);
        poNext.setLocation(want);
        AIMove m = AIGo.pursue(h, poNext);
        if (h.isInStickRange(puck)) {
            if (manager.isInMyScoreZone(po)) {
                m.setAction(ActionType.STRIKE);
            } else {
                m.setAction(ActionType.STRIKE);
                return m;
                // taking puck aren't working out
//                if (manager.isInOffenceZone(po)) {
//                    m.setAction(ActionType.TAKE_PUCK);
//                } else {
//                    for (AIHockeyist hh : manager.getTeammates()) {
//                        if (h == hh) continue;
//                        if (hh.isInStickRange(puck) && hh.getRemainingCooldownTicks() == 0) {
//                            m.setAction(ActionType.TAKE_PUCK);
//                        }
//                    }
//
//                }
            }
        }
        return m;
    }
}
