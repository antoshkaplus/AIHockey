import model.ActionType;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by antoshkaplus on 9/20/14.
 */
public class AIDefendPuck implements AIRole {
    private long hockeyistId;

    AIDefendPuck(long hockeyistId) {
        this.hockeyistId = hockeyistId;
    }

    @Override
    public AIMove move() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
        AIPuck puck = manager.getPuck();
        Collection<AIHockeyist> opponents = manager.getOpponents();
        AIHockeyist n = (AIHockeyist)puck.nearestUnit((Collection)opponents);
        // being one step ahead
        AIMove move = AIGo.to(hockeyist, n);
        if (hockeyist.isInStickRange(n)) {
            move.setAction(ActionType.STRIKE);
        }
        return move;
    }
}
