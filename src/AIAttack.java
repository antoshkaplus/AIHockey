import model.ActionType;

/**
 * Created by antoshkaplus on 9/24/14.
 */
public class AIAttack implements AIRole {

    protected long hockeyistId;

    // need to make valid back to true somehow
    private boolean validSwing = true;
    // between 10 or 20
    private int swingTicks = 0;

    // doesn't have any special type
    AIAttack(long hockeyistId) {
        this.hockeyistId = hockeyistId;
    }

    @Override
    public AIMove move() {
        // everywhere check action possibility
        // or just once

        AIManager manager = AIManager.getInstance();
        AINet hisNet = manager.getHisNet();
        AIHockeyist hockeyist = manager.getTeammate(hockeyistId);
        int currentTick = manager.getCurrentTick();
        AIMove move = new AIMove();
        // need this after swing cancel
        if (hockeyist.getLastAction() == ActionType.SWING) {
            // trying turn off swing after invalidation
            if (!validSwing) {
                move.setAction(ActionType.CANCEL_STRIKE);
                return move;
            } else {
                if (hisNet.canScoreStrike(hockeyist) &&
                        currentTick - hockeyist.getLastActionTick() > 10) {
                    move.setAction(ActionType.STRIKE);
                    return move;
                }

                for (int st = swingTicks; st < swingTicks + 5; ++st) {
                    AIHockeyist futureHockeyist =
                            AIHockeyist.hockeyistAfterSwingTicks(
                                    hockeyist, st);
                    if (hisNet.canScoreStrike(futureHockeyist)) {
                        swingTicks = st;
                        --swingTicks;
                        // return clear move
                        return move;
                    }
                }
                move.setAction(ActionType.CANCEL_STRIKE);
                validSwing = false;
                return move;

            }

        }
        if (hisNet.canScoreStrike(hockeyist) && !manager.canOpponentIntercept(hockeyist)) {
            move.setAction(ActionType.STRIKE);
            return move;
        }
        double angle = hisNet.bestScoreAngle(hockeyist.getLocation(), 0.5*hockeyist.getPuckAngleDeviation());
        double passAngle = AI.orientAngle(hockeyist.getAngle(), angle);
        if (hisNet.canScorePass(hockeyist, passAngle) && !manager.canOpponentIntercept(hockeyist.getLocation(), angle)) {
            move.setPassAngle(passAngle);
            move.setPassPower(1);
            move.setAction(ActionType.PASS);
            return move;
        }

        // lets try swing now
        // here should carry out avoidance of bitches


        if (manager.canOpponentIntercept(hockeyist)) {
            if (canScoreAfterSwing(20)) {
                swingTicks = 20;
                validSwing = true;
                move.setAction(ActionType.SWING);
                return move;
            }
        } else {
            if (canScoreAfterSwing(10)) {
                swingTicks = 10;
                validSwing = true;
                move.setAction(ActionType.SWING);
                return move;
            }
        }

        move.setValid(false);
        return move;
    }

    boolean canScoreAfterSwing(int ticks) {
        AIManager manager = AIManager.getInstance();
        AIHockeyist futureHockeyist =
                AIHockeyist.hockeyistAfterSwingTicks(
                        manager.getTeammate(hockeyistId), ticks);
        AINet net = AIManager.getInstance().getHisNet();
        return net.canScoreStrike(futureHockeyist);

    }
}
