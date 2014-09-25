import model.Move;

import java.util.Iterator;

/**
 * Created by antoshkaplus on 9/11/14.
 */
public class AIMove extends Move {
    boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    void setValid(boolean valid) {
        this.valid = valid;
    }

//    static AIMove joinMoves(Iterable<AIMove> moves, Iterable<Double> weights) {
//        double weightSum = 0;
//        for (Double w : weights) {
//            weightSum += w;
//        }
//        AIMove move = new AIMove();
//        Iterator<Double> w_it = weights.iterator();
//        for (AIMove m : moves) {
//            Double w = w_it.next();
//            move.setTurn();
//        }
//    }
}
