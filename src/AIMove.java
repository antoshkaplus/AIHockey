import model.Move;

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
}
