import model.ActionType;

/**
 * Created by antoshkaplus on 9/22/14.
 */
public class AIInterceptPuck implements AIRole {

    private long hockeyistId;

    AIInterceptPuck(long hockeyistId) {
        this.hockeyistId = hockeyistId;
    }

    @Override
    public AIMove move() {
        AIManager manager = AIManager.getInstance();
        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
        AIMove move = new AIMove();
        if (!manager.isHisPuck()) {
            move.setValid(false);
            return move;
        }
        AIPuck puck = manager.getPuck();
        move = AIGo.to(hockeyist, puck);
        if (hockeyist.isInStickRange(puck)) {
            if (manager.isInMyScoreZone(puck)) {
                move.setAction(ActionType.STRIKE);
            } else {
                move.setAction(ActionType.TAKE_PUCK);
            }
        }
        return move;
    }
}
