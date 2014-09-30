import model.ActionType;

import javax.management.relation.Role;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by antoshkaplus on 9/24/14.
 */
public class AINeuralPuckStrategy implements AIStrategy {

    AIManager manager = AIManager.getInstance();

    Map<Long, AIMove> moves;
    Map<Long, AIRole> roles;

    @Override
    public void init() {
        moves = new TreeMap<Long, AIMove>();
        roles = new TreeMap<Long, AIRole>();
        for (AIHockeyist h : manager.getTeammates()) {
            roles.put(h.getId(), getRole(h));
        }
    }

    @Override
    public void update() {
        init();
        for (Map.Entry<Long, AIRole> r : roles.entrySet()) {
            AIMove m = r.getValue().move();
            if (!m.isValid()) {
                throw new RuntimeException();
            }
            moves.put(r.getKey(), m);

        }
    }

    @Override
    public AIMove getMove(long teammateId) {
        return moves.get(teammateId);
    }

    private AIRole getRole(AIHockeyist h) {
        AIManager manager = AIManager.getInstance();
        AIPuck puck = manager.getPuck();
        AIRectangle offenceZone = manager.getOffenceZone();
        if (offenceZone.isInside(puck.getLocation()) &&
            puck.nearestUnit((Collection) manager.getTeammates()) != h) {
            return new AIDefendNet(h.getId());
        } else {
            return new AITakePuck(h.getId());
        }
    }
}
