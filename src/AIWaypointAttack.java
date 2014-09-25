import model.ActionType;

import java.util.List;

/**
 * Created by antoshkaplus on 9/23/14.
 *
 * we have some special point and we will stop there (ba rely move)
 * then we will aim and fire
 */
public class AIWaypointAttack extends AIAttack {

    // right now we won't use it

    AIPoint waypoint;

    List<AIPoint> sideCentralWaypoints;
    List<AIPoint> offenceFaceOffWaypoints;

    // don't really know should I use radius here
    AIWaypointAttack(long hockeyistId, AIPoint waypoint, double radius) {
        super(hockeyistId);
    }

    @Override
    public AIMove move() {
        AIMove move = new AIMove();



        if (waypoint == null) {
            // should find anything waypoint or something


        }

        // find every dude... find vectors to escape collisions
        // for every possible waypoint find how good it's considering current
        // angle, location and escaping collision with opponents

        // after defining new waypoint go there
        // but use some kind of weights from collision vectors
        // depending of collision distance + distance to waypoint...
        // weights on both sides
        // then normilaize



        // what if current waypoint is good enough

        // what if we arrived to waypoint // what next


        // how will we score


        // pass score (not very often)
        // strike
        // swing + strike
        // full swing + strike
        // stop + turn + strike
        // stop + turn + swing + strike
        // stop + turn + full swing + strike




        // toilet possibility



        return move;

    }


}
