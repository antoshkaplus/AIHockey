import model.ActionType;

/**
 * Created by antoshkaplus on 9/21/14.
 */
public class AITakePuck implements AIRole {

    private long hockeyistId;

    AITakePuck(long hockeyistId) {
        this.hockeyistId = hockeyistId;
    }


    @Override
    public AIMove move() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
        AIPuck puck = manager.getPuck();
        AIMove move = AIGo.to(hockeyist, puck);
        if (hockeyist.isInStickRange(puck)) {
            move.setAction(ActionType.TAKE_PUCK);
        }
        return move;
    }


}
