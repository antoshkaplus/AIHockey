import model.ActionType;

import java.util.Collection;
import java.util.Objects;

/**
 * Created by antoshkaplus on 9/20/14.
 */
public class AIDefendPuck implements AIRole {

    @Override
    public AIMove move(AIHockeyist hockeyist) {
        AIManager manager = AIManager.getInstance();
        AIPuck puck = manager.getPuck();
        Collection<AIHockeyist> opponents = manager.getOpponents();
        AIHockeyist n = (AIHockeyist)puck.nearestUnit((Collection)opponents);
        AIMove move = AIGo.to(hockeyist, n);
        if (hockeyist.isInStickRange(n)) {
            move.setAction(ActionType.STRIKE);
        }
        return move;
    }
}
