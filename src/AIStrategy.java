/**
 * Created by antoshkaplus on 9/24/14.
 */
public interface AIStrategy {

    public void init();
    public void update();
    public AIMove getMove(long teammateId);

}
