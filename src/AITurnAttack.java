
import static java.lang.Math.*;

/**
 * Created by antoshkaplus on 9/25/14.
 *
 * first in your sub attack classes use Attack move
 * then use Turn move.... try it out with 10
 */
public class AITurnAttack implements AIRole {

    private AIAttack attack;

    private long hockeyistId;
    private AIManager manager = AIManager.getInstance();

    AITurnAttack(long hockeyistId) {
        this.hockeyistId = hockeyistId;
        attack = new AIAttack(hockeyistId);
    }

    @Override
    public AIMove move() {
        AIMove m = attack.move();
        if (m.isValid()) {
            return m;
        }
        AIHockeyist h = manager.getTeammate(hockeyistId);
        AINet net = manager.getHisNet();
        m = new AIMove();
        for (int i = 1; i < 20; ++i) {
            AIHockeyist hAfter = AIHockeyist.hockeyistAfterTicks(h, i);
            double angle = net.bestScoreAngle(hAfter.getLocation(), 0.7*hAfter.getPuckAngleDeviation());
            double turnAngle = hAfter.angleTo(angle);
            if (abs(turnAngle)/h.getMaxTurnPerTick() <= i) {
                AIHockeyist hTurn = AIHockeyist.hockeyistAfterTurnTicks(h, i, turnAngle);
                if (net.canScoreStrike(hTurn)) {
                    m.setTurn(turnAngle);
                    return m;
                }
            }
            hAfter = AIHockeyist.hockeyistAfterTicks(h, i + 10);
            angle = net.bestScoreAngle(hAfter.getLocation(), 0.7*hAfter.getPuckAngleDeviation());
            turnAngle = hAfter.angleTo(angle);
            if (abs(turnAngle)/h.getMaxTurnPerTick() <= i) {
                AIHockeyist hTurn = AIHockeyist.hockeyistAfterTurnTicks(h, i, turnAngle);
                AIHockeyist hSwing = AIHockeyist.hockeyistAfterSwingTicks(hTurn, 10);
                if (net.canScoreStrike(hSwing)) {
                    m.setTurn(turnAngle);
                    return m;
                }
            }
        }
        m.setValid(false);
        return m;
    }
}
