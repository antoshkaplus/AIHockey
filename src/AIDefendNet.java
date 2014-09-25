/**
 * Created by antoshkaplus on 9/21/14.
 */
public class AIDefendNet implements AIRole {
    private long hockeyistId;

    AIInterceptPuck interceptPuck;
    AIDefendPuck defendPuck;
    AITakePuck takePuck;

    AIDefendNet(long hockeyistId) {
        this.hockeyistId = hockeyistId;
        defendPuck = new AIDefendPuck(hockeyistId);
        interceptPuck = new AIInterceptPuck(hockeyistId);
        takePuck = new AITakePuck(hockeyistId);

    }

    @Override
    public AIMove move() {
        AIMove move = new AIMove();

        AIManager manager = AIManager.getInstance();
        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
        AIPuck puck = manager.getPuck();
        AIRectangle myZone = manager.getMyZone();
        AIRectangle offenceZone = manager.getOffenceZone();
        AIPoint myNetCenter = manager.getMyNet().getNetCenter();
        if (myZone.isInside(puck.getLocation())) {
            if (manager.isHisPuck()) {
                return interceptPuck.move();
            } else if (manager.isNeutralPuck()) {
                return takePuck.move();
            } else {
                return defendPuck.move();
            }
        }
        AIPoint p_0 = new AIPoint(myNetCenter);
        p_0.scale(1./3);
        AIPoint p_1 = new AIPoint(puck.getLocation());
        p_1.scale(2./3);

        AIPoint target = AIPoint.sum(p_0, p_1);
        return AIGo.to(hockeyist, target);
    }
}
