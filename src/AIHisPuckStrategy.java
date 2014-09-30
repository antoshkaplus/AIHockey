import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by antoshkaplus on 9/24/14.
 */
public class AIHisPuckStrategy implements AIStrategy {

    Map<Long, AIMove> moves;
    Map<Long, AIRole> roles;

    AIManager manager = AIManager.getInstance();

    @Override
    public void init() {
        moves = new TreeMap<Long, AIMove>();
        roles = new TreeMap<Long, AIRole>();
        for (AIHockeyist h : manager.getTeammates()) {
            roles.put(h.getId(), new AIInterceptPuckOwnerDefence(h.getId()));
        }
    }

    @Override
    public void update() {
        for (AIHockeyist h : manager.getTeammates()) {
            //AIRole role = new AIInterceptPuck(hockeyist.getId());
            AIMove m = roles.get(h.getId()).move();
            if (!m.isValid()) {
                roles.put(h.getId(), new AIAgainstPuckOwnerDefence(h.getId()));
                m = roles.get(h.getId()).move();
            }
            moves.put(h.getId(), m);
              // his puck
//            if (puck.nearestUnit((Collection)teammates) != hockeyist) {
//                move = defendNet.move(hockeyist);
//            }
//            if (!move.isValid()) {
//                // move = defendPuck.move(hockeyist);
//                move = interceptPuck.move(hockeyist);
//            }
        }
    }

    @Override
    public AIMove getMove(long teammateId) {
        return moves.get(teammateId);
    }
}
