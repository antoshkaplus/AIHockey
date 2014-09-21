import model.ActionType;

/**
 * Created by antoshkaplus on 9/21/14.
 */
public class AITakePuck implements AIRole {

    @Override
    public AIMove move(AIHockeyist hockeyist) {
        AIManager manager = AIManager.getInstance();
        AIPuck puck = manager.getPuck();

        AIMove move = AIGo.to(hockeyist, puck);
        if (hockeyist.isInStickRange(puck)) {
            move.setAction(ActionType.TAKE_PUCK);
        }
        return move;
    }


}
