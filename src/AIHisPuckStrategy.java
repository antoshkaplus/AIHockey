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
            roles.put(h.getId(), new AIInterceptPuck(h.getId()));
        }


    }

    @Override
    public void update() {
        for (AIHockeyist h : manager.getTeammates()) {
            //AIRole role = new AIInterceptPuck(hockeyist.getId());
            moves.put(h.getId(), roles.get(h.getId()).move());
//            // his puck
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
