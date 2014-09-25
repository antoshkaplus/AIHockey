import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 9/17/14.
 */
public class AIDataCollector {
    private BufferedWriter puckWriter;
    private BufferedWriter hockeyistWriter;
    List<AIPoint> locations = new ArrayList<AIPoint>();
    List<Double> speed = new ArrayList<Double>();

    AIDataCollector() { }

    public BufferedWriter getPuckWriter() {
        try {
            if (puckWriter == null) {
                puckWriter = new BufferedWriter(new FileWriter("puck.txt", false));
            }
        } catch (Exception e) {}
        return puckWriter;
    }

    public BufferedWriter getHockeyistWriter() {
        try {
            if (hockeyistWriter == null) {
                hockeyistWriter = new BufferedWriter(new FileWriter("hockeyist.txt", false));
            }
        } catch (Exception e) {}
        return hockeyistWriter;
    }

    void collectPuckData(AIPuck puck, AIHockeyist puckOwner) {
        if (puckOwner == null) {
            if (locations.size() < 2) {
                locations.add(new AIPoint(puck.getLocation()));
                speed.add(puck.getSpeedScalar());
            } else {
                // lets see where the point is and etc
                if (!AI.isBetween(
                        locations.get(0),
                        puck.getLocation(),
                        locations.get(locations.size() / 2)) ||
                    AI.angle(
                        puck.getSpeed(),
                        AIPoint.difference(
                                puck.getLocation(),
                                locations.get(0))) > 0.01) {
                    if (locations.size() > 2) {
                        writeResults();
                    }
                    reset();
                } else {
                    addState(puck);
                }
            }
        } else if (!locations.isEmpty()) {
            if (locations.size() > 2) {
                // write chain to file
                writeResults();
            }
            reset();
        }
    }


    void addState(AIPuck puck) {
        locations.add(new AIPoint(puck.getLocation()));
        speed.add(puck.getSpeedScalar());
    }

    void reset() {
        locations.clear();
        speed.clear();
    }

    void writeResults() {
        BufferedWriter writer = getPuckWriter();
        try {
            for (int i = 0; i < locations.size(); ++i) {
                writer.write(String.format("%1$.2f", speed.get(i))  + "," +
                             String.format("%1$.2f", locations.get(0).distance(locations.get(i))) + ";");
            }
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.print("Can't write to file");
        }
    }
}
