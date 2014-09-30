import model.ActionType;

import java.util.Collection;

/**
 * Created by antoshkaplus on 9/26/14.
 */
public class AIInterceptPuckOwnerDefence implements AIRole {

    private long hockeyistId;

    AIInterceptPuckOwnerDefence(long hockeyistId) {
        this.hockeyistId = hockeyistId;
    }

    @Override
    public AIMove move() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist h = manager.getTeammate(hockeyistId);
        AIHockeyist po = manager.getPuckOwner();
        if (h.distanceTo(po) < 2 * AIHockeyist.RADIUS + AIPuck.RADIUS) {
            AIMove m = new AIMove();
            m.setValid(false);
            return m;
        }
        AINet net = manager.getMyNet();
        AIPoint center = manager.getCenter();
        AIHockeyist poNext = po;
        if (po.isMovingAwayFrom(net.getNetCenter())) {
            AIPoint pr = AI.projection(po.getSpeed(), AIPoint.difference(net.getNetCenter(), center));
            poNext = new AIHockeyist(po);
            poNext.setSpeed(pr);
        }
        AIMove m = AIGo.pursue(h, poNext);
        if (h.isAnyInStickRange((Collection)manager.getOpponents())) {
            m.setAction(ActionType.STRIKE);
        }
        return m;
    }
}
